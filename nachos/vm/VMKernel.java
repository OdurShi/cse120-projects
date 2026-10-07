package nachos.vm;

import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;
import nachos.vm.*;

/**
 * 一个可以支持多个按需分页（demand-paging）用户进程的内核。
 */
public class VMKernel extends UserKernel {
	/**
	 * 分配（创建）一个新的 VM kernel。
	 */
	public VMKernel() {
		super();
	}

	/**
	 * 初始化这个内核。
	 */
	public void initialize(String[] args) {
		super.initialize(args);
	}

	/**
	 * 测试这个内核。
	 */
	public void selfTest() {
		super.selfTest();
	}

	/**
	 * 开始运行用户程序。
	 */
	public void run() {
		super.run();
	}

	/**
	 * 终止这个内核。永远不会返回。
	 */
	public void terminate() {
		super.terminate();
	}

	// 一些 dummy 变量，让 javac 更"聪明"（顺带把这些类也编译进来）
	private static VMProcess dummy1 = null;

	private static final char dbgVM = 'v';
}
