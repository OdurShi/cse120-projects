// PART OF THE MACHINE SIMULATION. DO NOT CHANGE.

package nachos.machine;

import java.io.EOFException;

/**
 * COFF（common object file format，通用目标文件格式）加载器。
 */
public class Coff {
	/**
	 * 分配（创建）一个新的 Coff 对象。
	 */
	protected Coff() {
		file = null;
		entryPoint = 0;
		sections = null;
	}

	/**
	 * 加载指定文件中的 COFF 可执行文件。
	 *
	 * <p>
	 * 注意：
	 * <ol>
	 * <li>如果构造函数成功返回，这个文件就归这个加载器所有，之后不应再访问它。
	 * <li>autograder 期望你使用这个加载器类。不要通过其他任何机制加载
	 * section。
	 * <li>这个加载器会通过断言 read() 操作花费了非零的模拟时间，来验证这个文件
	 * 确实由文件系统支撑。不要提供由模拟缓存支撑的文件（这个限制的主要目的，
	 * 是防止在处理 page fault 时 section 被瞬间加载完成）。
	 * </ol>
	 *
	 * @param file 包含可执行文件的文件。
	 * @exception EOFException 如果可执行文件已损坏。
	 */
	public Coff(OpenFile file) throws EOFException {
		this.file = file;

		Coff coff = Machine.autoGrader().createLoader(file);

		if (coff != null) {
			this.entryPoint = coff.entryPoint;
			this.sections = coff.sections;
		}
		else {
			byte[] headers = new byte[headerLength + aoutHeaderLength];

			if (file.length() < headers.length) {
				Lib.debug(dbgCoff, "\tfile is not executable");
				throw new EOFException();
			}

			Lib.strictReadFile(file, 0, headers, 0, headers.length);

			int magic = Lib.bytesToUnsignedShort(headers, 0);
			int numSections = Lib.bytesToUnsignedShort(headers, 2);
			int optionalHeaderLength = Lib.bytesToUnsignedShort(headers, 16);
			int flags = Lib.bytesToUnsignedShort(headers, 18);
			entryPoint = Lib.bytesToInt(headers, headerLength + 16);

			if (magic != 0x0162) {
				Lib.debug(dbgCoff, "\tincorrect magic number");
				throw new EOFException();
			}
			if (numSections < 2 || numSections > 10) {
				Lib.debug(dbgCoff, "\tbad section count");
				throw new EOFException();
			}
			if ((flags & 0x0003) != 0x0003) {
				Lib.debug(dbgCoff, "\tbad header flags");
				throw new EOFException();
			}

			int offset = headerLength + optionalHeaderLength;

			sections = new CoffSection[numSections];
			for (int s = 0; s < numSections; s++) {
				int sectionEntryOffset = offset + s * CoffSection.headerLength;
				try {
					sections[s] = new CoffSection(file, this,
							sectionEntryOffset);
				}
				catch (EOFException e) {
					Lib.debug(dbgCoff, "\terror loading section " + s);
					throw e;
				}
			}
		}
	}

	/**
	 * 返回可执行文件中 section 的个数。
	 *
	 * @return 可执行文件中 section 的个数。
	 */
	public int getNumSections() {
		return sections.length;
	}

	/**
	 * 返回一个可以用来访问指定 section 的对象。有效的 section 编号是从
	 * <tt>0</tt> 到 <tt>getNumSections() - 1</tt>。
	 *
	 * @param sectionNumber 要选择的 section。
	 * @return 一个可以用来访问指定 section 的对象。
	 */
	public CoffSection getSection(int sectionNumber) {
		Lib.assertTrue(sectionNumber >= 0 && sectionNumber < sections.length);

		return sections[sectionNumber];
	}

	/**
	 * 返回程序的入口点（entry point）。在运行程序之前，PC 寄存器应该被初始化
	 * 为这个值。
	 *
	 * @return 程序的入口点。
	 */
	public int getEntryPoint() {
		Lib.assertTrue(file != null);

		return entryPoint;
	}

	/**
	 * 关闭可执行文件，并释放这个加载器分配的所有资源。
	 */
	public void close() {
		file.close();

		sections = null;
	}

	private OpenFile file;

	/** 程序第一条指令的虚拟地址。 */
	protected int entryPoint;

	/** 这个 COFF 可执行文件中的 section。 */
	protected CoffSection sections[];

	private static final int headerLength = 20;

	private static final int aoutHeaderLength = 28;

	private static final char dbgCoff = 'c';
}
