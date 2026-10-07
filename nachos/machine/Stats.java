// PART OF THE MACHINE SIMULATION. DO NOT CHANGE.

package nachos.machine;

import nachos.machine.*;

/**
 * 维护 Nachos 运行时统计信息的对象。
 */
public final class Stats {
	/**
	 * 分配（创建）一个新的统计对象。
	 */
	public Stats() {
	}

	/**
	 * 打印这个对象中的统计信息。
	 */
	public void print() {
		System.out.println("Ticks: total " + totalTicks + ", kernel "
				+ kernelTicks + ", user " + userTicks);
		System.out.println("Disk I/O: reads " + numDiskReads + ", writes "
				+ numDiskWrites);
		System.out.println("Console I/O: reads " + numConsoleReads
				+ ", writes " + numConsoleWrites);
		System.out.println("Paging: page faults " + numPageFaults
				+ ", TLB misses " + numTLBMisses);
		System.out.println("Swap: COFF reads " + numCOFFReads
				+ ", swap reads " + numSwapReads
			        + ", swap writes " + numSwapWrites
			        + ", swap skips " + numSwapSkips);
		System.out.println("Network I/O: received " + numPacketsReceived
				+ ", sent " + numPacketsSent);
	}

	/**
	 * 自 Nachos 启动以来经过的模拟时间总量。
	 */
	public long totalTicks = 0;

	/**
	 * Nachos 在内核态（kernel mode）下花费的模拟时间总量。
	 */
	public long kernelTicks = 0;

	/**
	 * Nachos 在用户态（user mode）下花费的模拟时间总量。
	 */
	public long userTicks = 0;

	/** Nachos 从模拟磁盘读取的扇区总数。 */
	public int numDiskReads = 0;

	/** Nachos 写入模拟磁盘的扇区总数。 */
	public int numDiskWrites = 0;

	/** Nachos 从 console 读取的字符总数。 */
	public int numConsoleReads = 0;

	/** Nachos 写到 console 的字符总数。 */
	public int numConsoleWrites = 0;

	/** 发生的 page fault 总数。 */
	public int numPageFaults = 0;

	/** 发生的 TLB miss 总数。 */
	public int numTLBMisses = 0;

        /** 从 COFF 文件读取的页面总数。 */
        public int numCOFFReads = 0;

        /** 从 swap 读取的页面总数。 */
        public int numSwapReads = 0;

        /** 写入 swap 的页面总数。 */
        public int numSwapWrites = 0;

        /** 通过 clean page 优化而省去的写操作总数。 */
        public int numSwapSkips = 0;

    /** Nachos 发送到网络的数据包总数。 */
	public int numPacketsSent = 0;

	/** Nachos 从网络接收的数据包总数。 */
	public int numPacketsReceived = 0;

	/**
	 * 每执行一条用户指令后，模拟时间前进的量。
	 */
	public static final int UserTick = 1;

	/**
	 * 每次开启中断后，模拟时间前进的量。
	 */
	public static final int KernelTick = 10;

	/**
	 * 磁盘旋转 360 度所需的模拟时间。
	 */
	public static final int RotationTime = 500;

	/**
	 * 磁盘寻道所需的模拟时间。
	 */
	public static final int SeekTime = 500;

	/**
	 * console 处理一个字符所需的模拟时间。
	 */
	public static final int ConsoleTime = 100;

	/**
	 * 网络处理一个数据包所需的模拟时间。
	 */
	public static final int NetworkTime = 100;

	/**
	 * 两次 timer 中断之间的平均模拟时间。
	 */
	public static final int TimerTicks = 500;

	/**
	 * 电梯移动一层楼所需的模拟时间。
	 */
	public static final int ElevatorTicks = 2000;
}
