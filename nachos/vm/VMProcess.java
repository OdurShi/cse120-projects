package nachos.vm;

import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;
import nachos.vm.*;

/**
 * 一个支持按需分页（demand-paging）的 <tt>UserProcess</tt>。
 */
public class VMProcess extends UserProcess {
	/**
	 * 分配（创建）一个新进程。
	 */
	public VMProcess() {
		super();
	}

	/**
	 * 保存这个进程的状态，为 context switch 做准备。由
	 * <tt>UThread.saveState()</tt> 调用。
	 */
	public void saveState() {
		super.saveState();
	}

	/**
	 * 在 context switch 之后恢复这个进程的状态。由
	 * <tt>UThread.restoreState()</tt> 调用。
	 */
	public void restoreState() {
		super.restoreState();
	}

	/**
	 * 为这个进程初始化页表，使可执行文件可以按需分页加载。
	 *
	 * @return 如果成功，返回 <tt>true</tt>。
	 */
	protected boolean loadSections() {
		return super.loadSections();
	}

	/**
	 * 释放 <tt>loadSections()</tt> 分配的所有资源。
	 */
	protected void unloadSections() {
		super.unloadSections();
	}

	/**
	 * 处理一个用户异常。由 <tt>UserKernel.exceptionHandler()</tt> 调用。参数
	 * <i>cause</i> 指明发生的是哪种异常；参见 <tt>Processor.exceptionZZZ</tt>
	 * 系列常量。
	 *
	 * @param cause 发生的用户异常。
	 */
	public void handleException(int cause) {
		Processor processor = Machine.processor();

		switch (cause) {
		default:
			super.handleException(cause);
			break;
		}
	}

	private static final int pageSize = Processor.pageSize;

	private static final char dbgProcess = 'a';

	private static final char dbgVM = 'v';
}
