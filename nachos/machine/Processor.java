// PART OF THE MACHINE SIMULATION. DO NOT CHANGE.

package nachos.machine;

import nachos.security.*;

/**
 * <tt>Processor</tt> 类模拟一个支持 R3000 指令集子集的 MIPS 处理器。具体来说，
 * 这个处理器不支持任何协处理器（coprocessor），并且只能在用户态（user mode）
 * 下执行。地址翻译信息通过 API 访问。这个 API 还允许内核设置一个异常处理
 * 函数，在发生任何用户态异常时被调用。
 *
 * <p>
 * <tt>Processor</tt> 的 API 是可重入的（re-entrant），所以一个模拟处理器可以
 * 被多个用户线程共享。
 *
 * <p>
 * 一个 <tt>Processor</tt> 实例还包含用户程序可以访问的物理内存页，其大小由
 * 构造函数固定。
 */
public final class Processor {
	/**
	 * 分配（创建）一个新的 MIPS 处理器，带有指定大小的内存。
	 *
	 * @param privilege 封装了对 Nachos 机器的特权访问。
	 * @param numPhysPages 要挂载的物理内存页数。
	 */
	public Processor(Privilege privilege, int numPhysPages) {
		System.out.print(" processor");

		this.privilege = privilege;
		privilege.processor = new ProcessorPrivilege();

		Class<?> clsKernel = Lib.loadClass(Config.getString("Kernel.kernel"));
		Class<?> clsVMKernel = Lib.tryLoadClass("nachos.vm.VMKernel");

		usingTLB = (clsVMKernel != null && clsVMKernel
				.isAssignableFrom(clsKernel));
		// 允许配置文件中的值覆盖它
		usingTLB = Config.getBoolean ("Processor.usingTLB", usingTLB);

		this.numPhysPages = numPhysPages;

		for (int i = 0; i < numUserRegisters; i++)
			registers[i] = 0;

		mainMemory = new byte[pageSize * numPhysPages];

		if (usingTLB) {
			translations = new TranslationEntry[tlbSize];
			for (int i = 0; i < tlbSize; i++)
				translations[i] = new TranslationEntry();
		}
		else {
			translations = null;
		}
	}

	/**
	 * 设置异常处理函数，每当发生用户异常时都会调用它。
	 *
	 * <p>
	 * 调用异常处理函数时，中断是开启的，CPU 的 cause 寄存器会指明异常的原因
	 * （参见 <tt>exception<i>*</i></tt> 系列常量）。
	 *
	 * @param exceptionHandler 内核的异常处理函数。
	 */
	public void setExceptionHandler(Runnable exceptionHandler) {
		this.exceptionHandler = exceptionHandler;
	}

	/**
	 * 获取异常处理函数，即最近一次调用 <tt>setExceptionHandler()</tt> 时设置的
	 * 那个。
	 *
	 * @return 异常处理函数。
	 */
	public Runnable getExceptionHandler() {
		return exceptionHandler;
	}

	/**
	 * 从当前 PC 开始执行指令。永远不会返回。
	 */
	public void run() {
		Lib.debug(dbgProcessor, "starting program in current thread");

		registers[regNextPC] = registers[regPC] + 4;

		Machine.autoGrader().runProcessor(privilege);

		Instruction inst = new Instruction();

		while (true) {
			try {
				inst.run();
			}
			catch (MipsException e) {
				e.handle();
			}

			privilege.interrupt.tick(false);
		}
	}

	/**
	 * 读取并返回指定 CPU 寄存器的内容。
	 *
	 * @param number 要读取的寄存器。
	 * @return 寄存器的值。
	 */
	public int readRegister(int number) {
		Lib.assertTrue(number >= 0 && number < numUserRegisters);

		return registers[number];
	}

	/**
	 * 把指定的值写入指定的 CPU 寄存器。
	 *
	 * @param number 要写入的寄存器。
	 * @param value 要写入的值。
	 */
	public void writeRegister(int number, int value) {
		Lib.assertTrue(number >= 0 && number < numUserRegisters);

		if (number != 0)
			registers[number] = value;
	}

	/**
	 * 检查这个处理器使用的是软件管理的 TLB，还是单级分页（single-level
	 * paging）。
	 *
	 * <p>
	 * 如果为 <tt>false</tt>，这个处理器直接支持单级分页；请使用
	 * <tt>setPageTable()</tt>。
	 *
	 * <p>
	 * 如果为 <tt>true</tt>，这个处理器有一个软件管理的 TLB；请使用
	 * <tt>getTLBSize()</tt>、<tt>readTLBEntry()</tt> 和
	 * <tt>writeTLBEntry()</tt>。
	 *
	 * <p>
	 * 使用与当前地址翻译机制不符的方法会导致断言失败。
	 *
	 * @return 如果这个处理器有软件管理的 TLB，返回 <tt>true</tt>。
	 */
	public boolean hasTLB() {
		return usingTLB;
	}

	/**
	 * 获取当前页表，即最近一次调用 setPageTable() 时设置的那个。
	 *
	 * @return 当前页表。
	 */
	public TranslationEntry[] getPageTable() {
		Lib.assertTrue(!usingTLB);

		return translations;
	}

	/**
	 * 设置页表指针。之后所有的地址翻译都会使用指定的页表。当前地址空间的大小
	 * 由页表数组的长度决定。
	 *
	 * @param pageTable 要使用的页表。
	 */
	public void setPageTable(TranslationEntry[] pageTable) {
		Lib.assertTrue(!usingTLB);

		this.translations = pageTable;
	}

	/**
	 * 返回这个处理器 TLB 中的条目数。
	 *
	 * @return 这个处理器 TLB 中的条目数。
	 */
	public int getTLBSize() {
		Lib.assertTrue(usingTLB);

		return tlbSize;
	}

	/**
	 * 返回指定的 TLB 条目。
	 *
	 * @param number TLB 中的下标。
	 * @return 指定 TLB 条目的内容。
	 */
	public TranslationEntry readTLBEntry(int number) {
		Lib.assertTrue(usingTLB);
		Lib.assertTrue(number >= 0 && number < tlbSize);

		return new TranslationEntry(translations[number]);
	}

	/**
	 * 填写指定的 TLB 条目。
	 *
	 * <p>
	 * TLB 是全相联（fully associative）的，所以一个条目在 TLB 中的位置不会
	 * 影响任何东西。
	 *
	 * @param number TLB 中的下标。
	 * @param entry TLB 条目的新内容。
	 */
	public void writeTLBEntry(int number, TranslationEntry entry) {
		Lib.assertTrue(usingTLB);
		Lib.assertTrue(number >= 0 && number < tlbSize);

		translations[number] = new TranslationEntry(entry);
	}

	/**
	 * 返回挂载到这个模拟处理器上的物理内存页数。
	 *
	 * @return 物理内存页数。
	 */
	public int getNumPhysPages() {
		return numPhysPages;
	}

	/**
	 * 返回指向物理内存数组的引用。这个数组的大小是
	 * <tt>pageSize * getNumPhysPages()</tt>。
	 *
	 * @return 主存数组。
	 */
	public byte[] getMemory() {
		return mainMemory;
	}

	/**
	 * 把页号和页内偏移拼接成一个地址。
	 *
	 * @param page 页号。必须在 <tt>0</tt> 到
	 * <tt>(2<sup>32</sup> / pageSize) - 1</tt> 之间。
	 * @param offset 页内偏移。必须在 <tt>0</tt> 到 <tt>pageSize - 1</tt> 之间。
	 * @return 由指定页号和偏移组成的 32 位地址。
	 */
	public static int makeAddress(int page, int offset) {
		Lib.assertTrue(page >= 0 && page < maxPages);
		Lib.assertTrue(offset >= 0 && offset < pageSize);

		return (page * pageSize) | offset;
	}

	/**
	 * 从一个 32 位地址中取出页号部分。
	 *
	 * @param address 32 位地址。
	 * @return 地址的页号部分。
	 */
	public static int pageFromAddress(int address) {
		return (int) (((long) address & 0xFFFFFFFFL) / pageSize);
	}

	/**
	 * 从一个地址中取出偏移部分。
	 *
	 * @param address 32 位地址。
	 * @return 地址的偏移部分。
	 */
	public static int offsetFromAddress(int address) {
		return (int) (((long) address & 0xFFFFFFFFL) % pageSize);
	}

	private void finishLoad() {
		delayedLoad(0, 0, 0);
	}

	/**
	 * 使用页表或 TLB，把虚拟地址翻译成物理地址。检查对齐，确保虚拟页有效，
	 * 确保没有写只读页，确保得到的物理页有效，然后返回得到的物理地址。
	 *
	 * @param vaddr 要翻译的虚拟地址。
	 * @param size 这次内存访问的大小（必须是 1、2 或 4）。
	 * @param writing 如果这次内存访问是写操作，则为 <tt>true</tt>。
	 * @return 物理地址。
	 * @exception MipsException 如果发生了翻译错误。
	 */
	private int translate(int vaddr, int size, boolean writing)
			throws MipsException {
		if (Lib.test(dbgProcessor))
			System.out.println("\ttranslate vaddr=0x" + Lib.toHexString(vaddr)
					+ (writing ? ", write" : ", read..."));

		// 检查对齐
		if ((vaddr & (size - 1)) != 0) {
			Lib.debug(dbgProcessor, "\t\talignment error");
			throw new MipsException(exceptionAddressError, vaddr);
		}

		// 从虚拟地址计算虚拟页号和页内偏移
		int vpn = pageFromAddress(vaddr);
		int offset = offsetFromAddress(vaddr);

		TranslationEntry entry = null;

		// 如果不使用 TLB，vpn 就是页表的下标
		if (!usingTLB) {
			if (translations == null || vpn >= translations.length
					|| translations[vpn] == null || !translations[vpn].valid) {
				privilege.stats.numPageFaults++;
				Lib.debug(dbgProcessor, "\t\tpage fault");
				throw new MipsException(exceptionPageFault, vaddr);
			}

			entry = translations[vpn];
		}
		// 否则，在所有 TLB 条目中查找匹配的 vpn
		else {
			for (int i = 0; i < tlbSize; i++) {
				if (translations[i].valid && translations[i].vpn == vpn) {
					entry = translations[i];
					break;
				}
			}
			if (entry == null) {
				privilege.stats.numTLBMisses++;
				Lib.debug(dbgProcessor, "\t\tTLB miss");
				throw new MipsException(exceptionTLBMiss, vaddr);
			}
		}

		// 检查是否在写一个只读页
		if (entry.readOnly && writing) {
			Lib.debug(dbgProcessor, "\t\tread-only exception");
			throw new MipsException(exceptionReadOnly, vaddr);
		}

		// 检查物理页号是否越界
		int ppn = entry.ppn;
		if (ppn < 0 || ppn >= numPhysPages) {
			Lib.debug(dbgProcessor, "\t\tbad ppn");
			throw new MipsException(exceptionBusError, vaddr);
		}

		// 按需设置 used 位和 dirty 位
		entry.used = true;
		if (writing)
			entry.dirty = true;

		int paddr = (ppn * pageSize) + offset;

		if (Lib.test(dbgProcessor))
			System.out.println("\t\tpaddr=0x" + Lib.toHexString(paddr));
		return paddr;
	}

	/**
	 * 读取 <i>vaddr</i> 处 <i>size</i>（1、2 或 4）字节的虚拟内存，并返回
	 * 结果。
	 *
	 * @param vaddr 要读取的虚拟地址。
	 * @param size 要读取的字节数（1、2 或 4）。
	 * @return 读到的值。
	 * @exception MipsException 如果发生了翻译错误。
	 */
	private int readMem(int vaddr, int size) throws MipsException {
		if (Lib.test(dbgProcessor))
			System.out.println("\treadMem vaddr=0x" + Lib.toHexString(vaddr)
					+ ", size=" + size);

		Lib.assertTrue(size == 1 || size == 2 || size == 4);

		int value = Lib.bytesToInt(mainMemory, translate(vaddr, size, false),
				size);

		if (Lib.test(dbgProcessor))
			System.out.println("\t\tvalue read=0x"
					+ Lib.toHexString(value, size * 2));

		return value;
	}

	/**
	 * 把 <i>value</i> 写入从 <i>vaddr</i> 开始的 <i>size</i>（1、2 或 4）
	 * 字节虚拟内存。
	 *
	 * @param vaddr 要写入的虚拟地址。
	 * @param size 要写入的字节数（1、2 或 4）。
	 * @param value 要存储的值。
	 * @exception MipsException 如果发生了翻译错误。
	 */
	private void writeMem(int vaddr, int size, int value) throws MipsException {
		if (Lib.test(dbgProcessor))
			System.out.println("\twriteMem vaddr=0x" + Lib.toHexString(vaddr)
					+ ", size=" + size + ", value=0x"
					+ Lib.toHexString(value, size * 2));

		Lib.assertTrue(size == 1 || size == 2 || size == 4);

		Lib.bytesFromInt(mainMemory, translate(vaddr, size, true), size, value);
	}

	/**
	 * 完成正在进行的延迟加载（delayed load），并安排一个新的延迟加载。
	 *
	 * @param nextLoadTarget 新加载的目标寄存器。
	 * @param nextLoadValue 要加载到新目标中的值。
	 * @param nextLoadMask 指定新目标中哪些位要被覆盖的掩码。如果
	 * <tt>nextLoadMask</tt> 中某一位为 0，那么寄存器 <tt>nextLoadTarget</tt>
	 * 中对应的位不会被写入。
	 */
	private void delayedLoad(int nextLoadTarget, int nextLoadValue,
			int nextLoadMask) {
		// 完成上一次延迟加载（如果目标不是 r0 的话）
		if (loadTarget != 0) {
			int savedBits = registers[loadTarget] & ~loadMask;
			int newBits = loadValue & loadMask;
			registers[loadTarget] = savedBits | newBits;
		}

		// 安排下一次加载
		loadTarget = nextLoadTarget;
		loadValue = nextLoadValue;
		loadMask = nextLoadMask;
	}

	/**
	 * 把 PC 推进到下一条指令。
	 *
	 * <p>
	 * 把 nextPC 寄存器的内容转移到 PC 寄存器，然后把 nextPC 寄存器的值加 4。
	 * 等同于 <tt>advancePC(readRegister(regNextPC)+4)</tt>。
	 *
	 * <p>
	 * 在处理完 syscall 异常之后使用，让处理器继续执行下一条指令。
	 */
	public void advancePC() {
		advancePC(registers[regNextPC] + 4);
	}

	/**
	 * 把 nextPC 寄存器的内容转移到 PC 寄存器，然后写入 nextPC 寄存器。
	 *
	 * @param nextPC nextPC 寄存器的新值。
	 */
	private void advancePC(int nextPC) {
		registers[regPC] = registers[regNextPC];
		registers[regNextPC] = nextPC;
	}

	/** 由 syscall 指令引起。 */
	public static final int exceptionSyscall = 0;

	/** 由访问无效的虚拟页引起。 */
	public static final int exceptionPageFault = 1;

	/** 由访问一个没有被任何 TLB 条目映射的虚拟页引起。 */
	public static final int exceptionTLBMiss = 2;

	/** 由写一个只读虚拟页引起。 */
	public static final int exceptionReadOnly = 3;

	/** 由访问无效的物理页引起。 */
	public static final int exceptionBusError = 4;

	/** 由访问未对齐的虚拟地址引起。 */
	public static final int exceptionAddressError = 5;

	/** 由有符号运算溢出引起。 */
	public static final int exceptionOverflow = 6;

	/** 由尝试执行非法指令引起。 */
	public static final int exceptionIllegalInstruction = 7;

	/** 各个 CPU 异常的名字。 */
	public static final String exceptionNames[] = { "syscall      ",
			"page fault   ", "TLB miss     ", "read-only    ", "bus error    ",
			"address error", "overflow     ", "illegal inst " };

	/** 返回值寄存器 0 的下标。 */
	public static final int regV0 = 2;

	/** 返回值寄存器 1 的下标。 */
	public static final int regV1 = 3;

	/** 参数寄存器 0 的下标。 */
	public static final int regA0 = 4;

	/** 参数寄存器 1 的下标。 */
	public static final int regA1 = 5;

	/** 参数寄存器 2 的下标。 */
	public static final int regA2 = 6;

	/** 参数寄存器 3 的下标。 */
	public static final int regA3 = 7;

	/** 栈指针寄存器的下标。 */
	public static final int regSP = 29;

	/** 返回地址寄存器的下标。 */
	public static final int regRA = 31;

	/** low 寄存器的下标，用于乘法和除法。 */
	public static final int regLo = 32;

	/** high 寄存器的下标，用于乘法和除法。 */
	public static final int regHi = 33;

	/** 程序计数器（PC）寄存器的下标。 */
	public static final int regPC = 34;

	/** 下一个程序计数器（nextPC）寄存器的下标。 */
	public static final int regNextPC = 35;

	/** 异常原因（cause）寄存器的下标。 */
	public static final int regCause = 36;

	/** 异常出错虚拟地址（bad virtual address）寄存器的下标。 */
	public static final int regBadVAddr = 37;

	/** 软件可访问的 CPU 寄存器总数。 */
	public static final int numUserRegisters = 38;

	/** 为这个处理器提供特权。 */
	private Privilege privilege;

	/** 内核可以访问的 MIPS 寄存器。 */
	private int registers[] = new int[numUserRegisters];

	/** 当前正在进行的延迟加载所登记的目标寄存器。 */
	private int loadTarget = 0;

	/** 当前正在进行的延迟加载要修改的位。 */
	private int loadMask;

	/** 当前正在进行的延迟加载要加载的值。 */
	private int loadValue;

	/** 如果使用软件管理的 TLB，则为 <tt>true</tt>。 */
	private boolean usingTLB;

	/** TLB 条目数。 */
	private int tlbSize = 4;

	/**
	 * 一组翻译条目：有 TLB 时是全相联的，没有 TLB 时是直接映射的。
	 */
	private TranslationEntry[] translations;

	/** 页的大小，以字节为单位。 */
	public static final int pageSize = 0x400;

	/** 32 位地址空间中的页数。 */
	public static final int maxPages = (int) (0x100000000L / pageSize);

	/** 内存中的物理页数。 */
	private int numPhysPages;

	/** 供用户程序使用的主存。 */
	private byte[] mainMemory;

	/** 内核的异常处理函数，每次发生用户异常时都会调用。 */
	private Runnable exceptionHandler = null;

	private static final char dbgProcessor = 'p';

	private static final char dbgDisassemble = 'm';

	private static final char dbgFullDisassemble = 'M';

	private class ProcessorPrivilege implements Privilege.ProcessorPrivilege {
		public void flushPipe() {
			finishLoad();
		}
	}

	private class MipsException extends Exception {
		public MipsException(int cause) {
			Lib.assertTrue(cause >= 0 && cause < exceptionNames.length);

			this.cause = cause;
		}

		public MipsException(int cause, int badVAddr) {
			this(cause);

			hasBadVAddr = true;
			this.badVAddr = badVAddr;
		}

		public void handle() {
			writeRegister(regCause, cause);

			if (hasBadVAddr)
				writeRegister(regBadVAddr, badVAddr);

			if (Lib.test(dbgDisassemble) || Lib.test(dbgFullDisassemble))
				System.out.println("exception: " + exceptionNames[cause]);

			finishLoad();

			Lib.assertTrue(exceptionHandler != null);

			// autograder 可能不想让内核知道这个异常
			if (!Machine.autoGrader().exceptionHandler(privilege))
				return;

			exceptionHandler.run();
		}

		private boolean hasBadVAddr = false;

		private int cause, badVAddr;
	}

	private class Instruction {
		public void run() throws MipsException {
			// 学过 152（计算机体系结构）的同学应该看着眼熟吧？
			fetch();
			decode();
			execute();
			writeBack();
		}

		private boolean test(int flag) {
			return Lib.test(flag, flags);
		}

		private void fetch() throws MipsException {
			if ((Lib.test(dbgDisassemble) && !Lib.test(dbgProcessor))
					|| Lib.test(dbgFullDisassemble))
				System.out.print("PC=0x" + Lib.toHexString(registers[regPC])
						+ "\t");

			value = readMem(registers[regPC], 4);
		}

		private void decode() {
			op = Lib.extract(value, 26, 6);
			rs = Lib.extract(value, 21, 5);
			rt = Lib.extract(value, 16, 5);
			rd = Lib.extract(value, 11, 5);
			sh = Lib.extract(value, 6, 5);
			func = Lib.extract(value, 0, 6);
			target = Lib.extract(value, 0, 26);
			imm = Lib.extend(value, 0, 16);

			Mips info;
			switch (op) {
			case 0:
				info = Mips.specialtable[func];
				break;
			case 1:
				info = Mips.regimmtable[rt];
				break;
			default:
				info = Mips.optable[op];
				break;
			}

			operation = info.operation;
			name = info.name;
			format = info.format;
			flags = info.flags;

			mask = 0xFFFFFFFF;
			branch = true;

			// 获取内存访问的大小
			if (test(Mips.SIZEB))
				size = 1;
			else if (test(Mips.SIZEH))
				size = 2;
			else if (test(Mips.SIZEW))
				size = 4;
			else
				size = 0;

			// 获取 nextPC
			nextPC = registers[regNextPC] + 4;

			// 获取 dstReg
			if (test(Mips.DSTRA))
				dstReg = regRA;
			else if (format == Mips.IFMT)
				dstReg = rt;
			else if (format == Mips.RFMT)
				dstReg = rd;
			else
				dstReg = -1;

			// 获取 jtarget
			if (format == Mips.RFMT)
				jtarget = registers[rs];
			else if (format == Mips.IFMT)
				jtarget = registers[regNextPC] + (imm << 2);
			else if (format == Mips.JFMT)
				jtarget = (registers[regNextPC] & 0xF0000000) | (target << 2);
			else
				jtarget = -1;

			// 获取 imm
			if (test(Mips.UNSIGNED)) {
				imm &= 0xFFFF;
			}

			// 获取 addr
			addr = registers[rs] + imm;

			// 获取 src1
			if (test(Mips.SRC1SH))
				src1 = sh;
			else
				src1 = registers[rs];

			// 获取 src2
			if (test(Mips.SRC2IMM))
				src2 = imm;
			else
				src2 = registers[rt];

			if (test(Mips.UNSIGNED)) {
				src1 &= 0xFFFFFFFFL;
				src2 &= 0xFFFFFFFFL;
			}

			if (Lib.test(dbgDisassemble) || Lib.test(dbgFullDisassemble))
				print();
		}

		private void print() {
			if (Lib.test(dbgDisassemble) && Lib.test(dbgProcessor)
					&& !Lib.test(dbgFullDisassemble))
				System.out.print("PC=0x" + Lib.toHexString(registers[regPC])
						+ "\t");

			if (operation == Mips.INVALID) {
				System.out.print("invalid: op=" + Lib.toHexString(op, 2)
						+ " rs=" + Lib.toHexString(rs, 2) + " rt="
						+ Lib.toHexString(rt, 2) + " rd="
						+ Lib.toHexString(rd, 2) + " sh="
						+ Lib.toHexString(sh, 2) + " func="
						+ Lib.toHexString(func, 2) + "\n");
				return;
			}

			int spaceIndex = name.indexOf(' ');
			Lib.assertTrue(spaceIndex != -1
					&& spaceIndex == name.lastIndexOf(' '));

			String instname = name.substring(0, spaceIndex);
			char[] args = name.substring(spaceIndex + 1).toCharArray();

			System.out.print(instname + "\t");

			int minCharsPrinted = 0, maxCharsPrinted = 0;

			for (int i = 0; i < args.length; i++) {
				switch (args[i]) {
				case Mips.RS:
					System.out.print("$" + rs);
					minCharsPrinted += 2;
					maxCharsPrinted += 3;

					if (Lib.test(dbgFullDisassemble)) {
						System.out
								.print("#0x" + Lib.toHexString(registers[rs]));
						minCharsPrinted += 11;
						maxCharsPrinted += 11;
					}
					break;
				case Mips.RT:
					System.out.print("$" + rt);
					minCharsPrinted += 2;
					maxCharsPrinted += 3;

					if (Lib.test(dbgFullDisassemble)
							&& (i != 0 || !test(Mips.DST))
							&& !test(Mips.DELAYEDLOAD)) {
						System.out
								.print("#0x" + Lib.toHexString(registers[rt]));
						minCharsPrinted += 11;
						maxCharsPrinted += 11;
					}
					break;
				case Mips.RETURNADDRESS:
					if (rd == 31)
						continue;
				case Mips.RD:
					System.out.print("$" + rd);
					minCharsPrinted += 2;
					maxCharsPrinted += 3;
					break;
				case Mips.IMM:
					System.out.print(imm);
					minCharsPrinted += 1;
					maxCharsPrinted += 6;
					break;
				case Mips.SHIFTAMOUNT:
					System.out.print(sh);
					minCharsPrinted += 1;
					maxCharsPrinted += 2;
					break;
				case Mips.ADDR:
					System.out.print(imm + "($" + rs);
					minCharsPrinted += 4;
					maxCharsPrinted += 5;

					if (Lib.test(dbgFullDisassemble)) {
						System.out
								.print("#0x" + Lib.toHexString(registers[rs]));
						minCharsPrinted += 11;
						maxCharsPrinted += 11;
					}

					System.out.print(")");
					break;
				case Mips.TARGET:
					System.out.print("0x" + Lib.toHexString(jtarget));
					minCharsPrinted += 10;
					maxCharsPrinted += 10;
					break;
				default:
					Lib.assertTrue(false);
				}
				if (i + 1 < args.length) {
					System.out.print(", ");
					minCharsPrinted += 2;
					maxCharsPrinted += 2;
				}
				else {
					// 最大可能的间隔出现在 tsi，5+1+1=7，
					// 好在小于 8（这样才可行）
					Lib.assertTrue(maxCharsPrinted - minCharsPrinted < 8);
					// 最长的字符串是 stj，加上 -d M 时有 40-42 个字符；
					// 取 48
					while ((minCharsPrinted % 8) != 0) {
						System.out.print(" ");
						minCharsPrinted++;
						maxCharsPrinted++;
					}
					while (minCharsPrinted < 48) {
						System.out.print("\t");
						minCharsPrinted += 8;
					}
				}
			}

			if (Lib.test(dbgDisassemble) && Lib.test(dbgProcessor)
					&& !Lib.test(dbgFullDisassemble))
				System.out.print("\n");
		}

		private void execute() throws MipsException {
			int value;
			int preserved;

			switch (operation) {
			case Mips.ADD:
				dst = src1 + src2;
				break;
			case Mips.SUB:
				dst = src1 - src2;
				break;
			case Mips.MULT:
				dst = src1 * src2;
				registers[regLo] = (int) Lib.extract(dst, 0, 32);
				registers[regHi] = (int) Lib.extract(dst, 32, 32);
				break;
			case Mips.DIV:
				try {
					registers[regLo] = (int) (src1 / src2);
					registers[regHi] = (int) (src1 % src2);
					if (registers[regLo] * src2 + registers[regHi] != src1)
						throw new ArithmeticException();
				}
				catch (ArithmeticException e) {
					throw new MipsException(exceptionOverflow);
				}
				break;

			case Mips.SLL:
				dst = src2 << (src1 & 0x1F);
				break;
			case Mips.SRA:
				dst = src2 >> (src1 & 0x1F);
				break;
			case Mips.SRL:
				dst = src2 >>> (src1 & 0x1F);
				break;

			case Mips.SLT:
				dst = (src1 < src2) ? 1 : 0;
				break;

			case Mips.AND:
				dst = src1 & src2;
				break;
			case Mips.OR:
				dst = src1 | src2;
				break;
			case Mips.NOR:
				dst = ~(src1 | src2);
				break;
			case Mips.XOR:
				dst = src1 ^ src2;
				break;
			case Mips.LUI:
				dst = imm << 16;
				break;

			case Mips.BEQ:
				branch = (src1 == src2);
				break;
			case Mips.BNE:
				branch = (src1 != src2);
				break;
			case Mips.BGEZ:
				branch = (src1 >= 0);
				break;
			case Mips.BGTZ:
				branch = (src1 > 0);
				break;
			case Mips.BLEZ:
				branch = (src1 <= 0);
				break;
			case Mips.BLTZ:
				branch = (src1 < 0);
				break;

			case Mips.JUMP:
				break;

			case Mips.MFLO:
				dst = registers[regLo];
				break;
			case Mips.MFHI:
				dst = registers[regHi];
				break;
			case Mips.MTLO:
				registers[regLo] = (int) src1;
				break;
			case Mips.MTHI:
				registers[regHi] = (int) src1;
				break;

			case Mips.SYSCALL:
				throw new MipsException(exceptionSyscall);

			case Mips.LOAD:
				value = readMem(addr, size);

				if (!test(Mips.UNSIGNED))
					dst = Lib.extend(value, 0, size * 8);
				else
					dst = value;

				break;

			case Mips.LWL:
				value = readMem(addr & ~0x3, 4);

				// LWL 把输入左移，使被寻址的字节位于最高位
				preserved = (3 - (addr & 0x3)) * 8; // 要保留的位数
				mask = -1 << preserved; // 要保留的位在 mask 中为 0
				dst = value << preserved; // 把输入移到正确的位置
				addr &= ~0x3;

				break;

			case Mips.LWR:
				value = readMem(addr & ~0x3, 4);

				// LWR 把输入右移，使被寻址的字节位于最低位
				preserved = (addr & 0x3) * 8; // 要保留的位数
				mask = -1 >>> preserved; // 要保留的位在 mask 中为 0
				dst = value >>> preserved; // 把输入移到正确的位置
				addr &= ~0x3;

				break;

			case Mips.STORE:
				writeMem(addr, size, (int) src2);
				break;

			case Mips.SWL:
				value = readMem(addr & ~0x3, 4);

				// SWL 把最高位字节移到被寻址的位置
				preserved = (3 - (addr & 0x3)) * 8;
				mask = -1 >>> preserved;
				dst = src2 >>> preserved;

				// 合并两个值
				dst = (dst & mask) | (value & ~mask);

				writeMem(addr & ~0x3, 4, (int) dst);
				break;

			case Mips.SWR:
				value = readMem(addr & ~0x3, 4);

				// SWR 把最低位字节移到被寻址的位置
				preserved = (addr & 0x3) * 8;
				mask = -1 << preserved;
				dst = src2 << preserved;

				// 合并两个值
				dst = (dst & mask) | (value & ~mask);

				writeMem(addr & ~0x3, 4, (int) dst);
				break;

			case Mips.UNIMPL:
				System.err.println("Warning: encountered unimplemented inst");

			case Mips.INVALID:
				throw new MipsException(exceptionIllegalInstruction);

			default:
				Lib.assertNotReached();
			}
		}

		private void writeBack() throws MipsException {
			// 如果指令是有符号的，但进位位不等于符号位，就抛出异常
			if (test(Mips.OVERFLOW) && Lib.test(dst, 31) != Lib.test(dst, 32))
				throw new MipsException(exceptionOverflow);

			if (test(Mips.DELAYEDLOAD))
				delayedLoad(dstReg, (int) dst, mask);
			else
				finishLoad();

			if (test(Mips.LINK))
				dst = nextPC;

			if (test(Mips.DST) && dstReg != 0)
				registers[dstReg] = (int) dst;

			if ((test(Mips.DST) || test(Mips.DELAYEDLOAD)) && dstReg != 0) {
				if (Lib.test(dbgFullDisassemble)) {
					System.out.print("#0x" + Lib.toHexString((int) dst));
					if (test(Mips.DELAYEDLOAD))
						System.out.print(" (delayed load)");
				}
			}

			if (test(Mips.BRANCH) && branch) {
				nextPC = jtarget;
			}

			advancePC(nextPC);

			if ((Lib.test(dbgDisassemble) && !Lib.test(dbgProcessor))
					|| Lib.test(dbgFullDisassemble))
				System.out.print("\n");
		}

		// 用于执行单条指令的状态
		int value, op, rs, rt, rd, sh, func, target, imm;

		int operation, format, flags;

		String name;

		int size;

		int addr, nextPC, jtarget, dstReg;

		long src1, src2, dst;

		int mask;

		boolean branch;
	}

	private static class Mips {
		Mips() {
		}

		Mips(int operation, String name) {
			this.operation = operation;
			this.name = name;
		}

		Mips(int operation, String name, int format, int flags) {
			this(operation, name);
			this.format = format;
			this.flags = flags;
		}

		int operation = INVALID;

		String name = "invalid ";

		int format;

		int flags;

		// 操作类型
		static final int INVALID = 0, UNIMPL = 1, ADD = 2, SUB = 3, MULT = 4,
				DIV = 5, SLL = 6, SRA = 7, SRL = 8, SLT = 9, AND = 10, OR = 11,
				NOR = 12, XOR = 13, LUI = 14, MFLO = 21, MFHI = 22, MTLO = 23,
				MTHI = 24, JUMP = 25, BEQ = 26, BNE = 27, BLEZ = 28, BGTZ = 29,
				BLTZ = 30, BGEZ = 31, SYSCALL = 32, LOAD = 33, LWL = 36,
				LWR = 37, STORE = 38, SWL = 39, SWR = 40, MAX = 40;

		static final int IFMT = 1, JFMT = 2, RFMT = 3;

		static final int DST = 0x00000001, DSTRA = 0x00000002,
				OVERFLOW = 0x00000004, SRC1SH = 0x00000008,
				SRC2IMM = 0x00000010, UNSIGNED = 0x00000020, LINK = 0x00000040,
				DELAYEDLOAD = 0x00000080, SIZEB = 0x00000100,
				SIZEH = 0x00000200, SIZEW = 0x00000400, BRANCH = 0x00000800;

		static final char RS = 's', RT = 't', RD = 'd', IMM = 'i',
				SHIFTAMOUNT = 'h', ADDR = 'a', // imm(rs)
				TARGET = 'j', RETURNADDRESS = 'r'; // rd；如果 rd=31 则不显示；
													// 不能放在最后

		static final Mips[] optable = {
				new Mips(), // special 表
				new Mips(), // reg-imm 表
				new Mips(JUMP, "j j", JFMT, BRANCH),
				new Mips(JUMP, "jal j", JFMT, BRANCH | LINK | DST | DSTRA),
				new Mips(BEQ, "beq stj", IFMT, BRANCH),
				new Mips(BNE, "bne stj", IFMT, BRANCH),
				new Mips(BLEZ, "blez sj", IFMT, BRANCH),
				new Mips(BGTZ, "bgtz sj", IFMT, BRANCH),
				new Mips(ADD, "addi tsi", IFMT, DST | SRC2IMM | OVERFLOW),
				new Mips(ADD, "addiu tsi", IFMT, DST | SRC2IMM),
				new Mips(SLT, "slti tsi", IFMT, DST | SRC2IMM),
				new Mips(SLT, "sltiu tsi", IFMT, DST | SRC2IMM | UNSIGNED),
				new Mips(AND, "andi tsi", IFMT, DST | SRC2IMM | UNSIGNED),
				new Mips(OR, "ori tsi", IFMT, DST | SRC2IMM | UNSIGNED),
				new Mips(XOR, "xori tsi", IFMT, DST | SRC2IMM | UNSIGNED),
				new Mips(LUI, "lui ti", IFMT, DST | SRC2IMM | UNSIGNED),
				new Mips(), new Mips(), new Mips(), new Mips(),
				new Mips(BEQ, "beql stj", IFMT, BRANCH),
				new Mips(BNE, "bnel stj", IFMT, BRANCH),
				new Mips(BLEZ, "blezl sj", IFMT, BRANCH),
				new Mips(BGTZ, "bgtzl sj", IFMT, BRANCH), new Mips(),
				new Mips(), new Mips(), new Mips(), new Mips(), new Mips(),
				new Mips(), new Mips(),
				new Mips(LOAD, "lb ta", IFMT, DELAYEDLOAD | SIZEB),
				new Mips(LOAD, "lh ta", IFMT, DELAYEDLOAD | SIZEH),
				new Mips(LWL, "lwl ta", IFMT, DELAYEDLOAD),
				new Mips(LOAD, "lw ta", IFMT, DELAYEDLOAD | SIZEW),
				new Mips(LOAD, "lbu ta", IFMT, DELAYEDLOAD | SIZEB | UNSIGNED),
				new Mips(LOAD, "lhu ta", IFMT, DELAYEDLOAD | SIZEH | UNSIGNED),
				new Mips(LWR, "lwr ta", IFMT, DELAYEDLOAD), new Mips(),
				new Mips(STORE, "sb ta", IFMT, SIZEB),
				new Mips(STORE, "sh ta", IFMT, SIZEH),
				new Mips(SWL, "swl ta", IFMT, 0),
				new Mips(STORE, "sw ta", IFMT, SIZEW), new Mips(), new Mips(),
				new Mips(SWR, "swr ta", IFMT, 0), new Mips(),
				new Mips(UNIMPL, "ll "), new Mips(), new Mips(), new Mips(),
				new Mips(), new Mips(), new Mips(), new Mips(),
				new Mips(UNIMPL, "sc "), new Mips(), new Mips(), new Mips(),
				new Mips(), new Mips(), new Mips(), new Mips(), };

		static final Mips[] specialtable = {
				new Mips(SLL, "sll dth", RFMT, DST | SRC1SH), new Mips(),
				new Mips(SRL, "srl dth", RFMT, DST | SRC1SH),
				new Mips(SRA, "sra dth", RFMT, DST | SRC1SH),
				new Mips(SLL, "sllv dts", RFMT, DST), new Mips(),
				new Mips(SRL, "srlv dts", RFMT, DST),
				new Mips(SRA, "srav dts", RFMT, DST),
				new Mips(JUMP, "jr s", RFMT, BRANCH),
				new Mips(JUMP, "jalr rs", RFMT, BRANCH | LINK | DST),
				new Mips(), new Mips(), new Mips(SYSCALL, "syscall "),
				new Mips(UNIMPL, "break "), new Mips(),
				new Mips(UNIMPL, "sync "), new Mips(MFHI, "mfhi d", RFMT, DST),
				new Mips(MTHI, "mthi s", RFMT, 0),
				new Mips(MFLO, "mflo d", RFMT, DST),
				new Mips(MTLO, "mtlo s", RFMT, 0), new Mips(), new Mips(),
				new Mips(), new Mips(), new Mips(MULT, "mult st", RFMT, 0),
				new Mips(MULT, "multu st", RFMT, UNSIGNED),
				new Mips(DIV, "div st", RFMT, 0),
				new Mips(DIV, "divu st", RFMT, UNSIGNED), new Mips(),
				new Mips(), new Mips(), new Mips(),
				new Mips(ADD, "add dst", RFMT, DST | OVERFLOW),
				new Mips(ADD, "addu dst", RFMT, DST),
				new Mips(SUB, "sub dst", RFMT, DST | OVERFLOW),
				new Mips(SUB, "subu dst", RFMT, DST),
				new Mips(AND, "and dst", RFMT, DST),
				new Mips(OR, "or dst", RFMT, DST),
				new Mips(XOR, "xor dst", RFMT, DST),
				new Mips(NOR, "nor dst", RFMT, DST), new Mips(), new Mips(),
				new Mips(SLT, "slt dst", RFMT, DST),
				new Mips(SLT, "sltu dst", RFMT, DST | UNSIGNED), new Mips(),
				new Mips(), new Mips(), new Mips(), new Mips(), new Mips(),
				new Mips(), new Mips(), new Mips(), new Mips(), new Mips(),
				new Mips(), new Mips(), new Mips(), new Mips(), new Mips(),
				new Mips(), new Mips(), new Mips(), new Mips(), };

		static final Mips[] regimmtable = {
				new Mips(BLTZ, "bltz sj", IFMT, BRANCH),
				new Mips(BGEZ, "bgez sj", IFMT, BRANCH),
				new Mips(BLTZ, "bltzl sj", IFMT, BRANCH),
				new Mips(BGEZ, "bgezl sj", IFMT, BRANCH),
				new Mips(),
				new Mips(),
				new Mips(),
				new Mips(),
				new Mips(),
				new Mips(),
				new Mips(),
				new Mips(),
				new Mips(),
				new Mips(),
				new Mips(),
				new Mips(),
				new Mips(BLTZ, "bltzal sj", IFMT, BRANCH | LINK | DST | DSTRA),
				new Mips(BGEZ, "bgezal sj", IFMT, BRANCH | LINK | DST | DSTRA),
				new Mips(BLTZ, "bltzlal sj", IFMT, BRANCH | LINK | DST | DSTRA),
				new Mips(BGEZ, "bgezlal sj", IFMT, BRANCH | LINK | DST | DSTRA),
				new Mips(), new Mips(), new Mips(), new Mips(), new Mips(),
				new Mips(), new Mips(), new Mips(), new Mips(), new Mips(),
				new Mips(), new Mips() };
	}
}
