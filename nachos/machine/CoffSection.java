// PART OF THE MACHINE SIMULATION. DO NOT CHANGE.

package nachos.machine;

import nachos.security.*;

import java.io.EOFException;
import java.util.Arrays;

/**
 * <tt>CoffSection</tt> 管理 COFF 可执行文件中的单个 section。
 */
public class CoffSection {
	/**
	 * 用指定的参数分配（创建）一个新的 COFF section。
	 *
	 * @param coff 这个 section 所属的 COFF 对象。
	 * @param name 这个 section 的 COFF 名字。
	 * @param executable 如果这个 section 包含代码，则为 <tt>true</tt>。
	 * @param readOnly 如果这个 section 是只读的，则为 <tt>true</tt>。
	 * @param numPages 这个 section 中的虚拟页数。
	 * @param firstVPN 这个 section 使用的第一个虚拟页号。
	 */
	protected CoffSection(Coff coff, String name, boolean executable,
			boolean readOnly, int numPages, int firstVPN) {
		this.coff = coff;
		this.name = name;
		this.executable = executable;
		this.readOnly = readOnly;
		this.numPages = numPages;
		this.firstVPN = firstVPN;

		file = null;
		size = 0;
		contentOffset = 0;
		initialized = true;
	}

	/**
	 * 从可执行文件中加载一个 COFF section。
	 *
	 * @param file 包含可执行文件的文件。
	 * @param headerOffset section header 在可执行文件中的偏移量。
	 *
	 * @exception EOFException 如果发生错误。
	 */
	public CoffSection(OpenFile file, Coff coff, int headerOffset)
			throws EOFException {
		this.file = file;
		this.coff = coff;

		Lib.assertTrue(headerOffset >= 0);
		if (headerOffset + headerLength > file.length()) {
			Lib.debug(dbgCoffSection, "\tsection header truncated");
			throw new EOFException();
		}

		byte[] buf = new byte[headerLength];
		Lib.strictReadFile(file, headerOffset, buf, 0, headerLength);

		name = Lib.bytesToString(buf, 0, 8);
		int vaddr = Lib.bytesToInt(buf, 12);
		size = Lib.bytesToInt(buf, 16);
		contentOffset = Lib.bytesToInt(buf, 20);
		int numRelocations = Lib.bytesToUnsignedShort(buf, 32);
		int flags = Lib.bytesToInt(buf, 36);

		if (numRelocations != 0) {
			Lib.debug(dbgCoffSection, "\tsection needs relocation");
			throw new EOFException();
		}

		switch (flags & 0x0FFF) {
		case 0x0020:
			executable = true;
			readOnly = true;
			initialized = true;
			break;
		case 0x0040:
			executable = false;
			readOnly = false;
			initialized = true;
			break;
		case 0x0080:
			executable = false;
			readOnly = false;
			initialized = false;
			break;
		case 0x0100:
			executable = false;
			readOnly = true;
			initialized = true;
			break;
		default:
			Lib.debug(dbgCoffSection, "\tinvalid section flags: " + flags);
			throw new EOFException();
		}

		if (vaddr % Processor.pageSize != 0 || size < 0 || initialized
				&& (contentOffset < 0 || contentOffset + size > file.length())) {
			Lib.debug(dbgCoffSection, "\tinvalid section addresses: "
					+ "vaddr=" + vaddr + " size=" + size + " contentOffset="
					+ contentOffset);
			throw new EOFException();
		}

		numPages = Lib.divRoundUp(size, Processor.pageSize);
		firstVPN = vaddr / Processor.pageSize;
	}

	/**
	 * 返回用来加载这个可执行文件实例的 COFF 对象。
	 *
	 * @return 这个 section 对应的 COFF 对象。
	 */
	public Coff getCoff() {
		return coff;
	}

	/**
	 * 返回这个 section 的名字。
	 *
	 * @return 这个 section 的名字。
	 */
	public String getName() {
		return name;
	}

	/**
	 * 检查这个 section 是否只读。
	 *
	 * @return 如果这个 section 永远不应该被写入，返回 <tt>true</tt>。
	 */
	public boolean isReadOnly() {
		return readOnly;
	}

	/**
	 * 检查这个 section 是否已初始化。从已初始化的 section 加载一页需要访问
	 * 磁盘，而从未初始化的 section 加载一页只需要把这一页填零。
	 *
	 * @return 如果这个 section 在可执行文件中包含已初始化的数据，返回
	 * <tt>true</tt>。
	 */
	public boolean isInitialzed() {
		return initialized;
	}

	/**
	 * 返回这个 section 以页为单位的长度。
	 *
	 * @return 这个 section 中的页数。
	 */
	public int getLength() {
		return numPages;
	}

	/**
	 * 返回这个 section 使用的第一个虚拟页号。
	 *
	 * @return 这个 section 使用的第一个虚拟页号。
	 */
	public int getFirstVPN() {
		return firstVPN;
	}

	/**
	 * 把这个段中的一页加载到物理内存中。
	 *
	 * @param spn 段内的页号。
	 * @param ppn 要加载到的物理页。
	 */
	public void loadPage(int spn, int ppn) {
		Lib.assertTrue(file != null);

		Lib.assertTrue(spn >= 0 && spn < numPages);
		Lib.assertTrue(ppn >= 0 && ppn < Machine.processor().getNumPhysPages());

		int pageSize = Processor.pageSize;
		byte[] memory = Machine.processor().getMemory();
		int paddr = ppn * pageSize;
		int faddr = contentOffset + spn * pageSize;
		int initlen;

		if (!initialized)
			initlen = 0;
		else if (spn == numPages - 1)
			initlen = size % pageSize == 0 ? pageSize : size % pageSize;
		else
			initlen = pageSize;

		if (initlen > 0)
			Lib.strictReadFile(file, faddr, memory, paddr, initlen);

		Arrays.fill(memory, paddr + initlen, paddr + pageSize, (byte) 0);
		Machine.incrNumCOFFReads();
	}

	/** 这个 section 所属的 COFF 对象。 */
	protected Coff coff;

	/** 这个 section 的 COFF 名字。 */
	protected String name;

	/** 如果这个 section 包含代码，则为 true。 */
	protected boolean executable;

	/** 如果这个 section 是只读的，则为 true。 */
	protected boolean readOnly;

	/** 如果这个 section 包含已初始化的数据，则为 true。 */
	protected boolean initialized;

	/** 这个 section 中的虚拟页数。 */
	protected int numPages;

	/** 这个 section 使用的第一个虚拟页号。 */
	protected int firstVPN;

	private OpenFile file;

	private int contentOffset, size;

	/** COFF section header 的长度。 */
	public static final int headerLength = 40;

	private static final char dbgCoffSection = 'c';
}
