package nachos.userprog;

import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;

/**
 * UThread 是一种 KThread，它除了能执行 Nachos 内核代码之外，还能在一个用户
 * 进程中执行用户程序代码。
 */
public class UThread extends KThread {
	/**
	 * 分配（创建）一个新的 UThread。
	 */
	public UThread(UserProcess process) {
		super();

		setTarget(new Runnable() {
			public void run() {
				runProgram();
			}
		});

		this.process = process;
	}

	private void runProgram() {
		process.initRegisters();
		process.restoreState();

		Machine.processor().run();

		Lib.assertNotReached();
	}

	/**
	 * 在把处理器让给另一个线程之前保存状态。
	 */
	protected void saveState() {
		process.saveState();

		for (int i = 0; i < Processor.numUserRegisters; i++)
			userRegisters[i] = Machine.processor().readRegister(i);

		super.saveState();
	}

	/**
	 * 在再次获得处理器之前恢复状态。
	 */
	protected void restoreState() {
		super.restoreState();

		for (int i = 0; i < Processor.numUserRegisters; i++)
			Machine.processor().writeRegister(i, userRegisters[i]);

		process.restoreState();
	}

	/**
	 * 用户寄存器组的存储空间。
	 *
	 * <p>
	 * 一个能运行用户代码的线程实际上有<i>两</i>组 CPU 寄存器：一组用于执行用户
	 * 代码时的状态，另一组用于执行内核代码时的状态。当这个线程没有在运行时，
	 * 它的用户态状态就保存在这里。
	 */
	public int userRegisters[] = new int[Processor.numUserRegisters];

	/**
	 * 这个线程所属的进程。
	 */
	public UserProcess process;
}
