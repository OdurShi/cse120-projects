// PART OF THE MACHINE SIMULATION. DO NOT CHANGE.

package nachos.machine;

/**
 * 一个操作系统内核。
 */
public abstract class Kernel {
	/** 全局可访问的内核引用。 */
	public static Kernel kernel = null;

	/**
	 * 分配（创建）一个新的内核。
	 */
	public Kernel() {
		// 确保只创建一个内核
		Lib.assertTrue(kernel == null);
		kernel = this;
	}

	/**
	 * 初始化这个内核。
	 */
	public abstract void initialize(String[] args);

	/**
	 * 测试这个模块能否正常工作。
	 *
	 * <b>警告：</b>我们给你的项目评分时，autograder 不会调用这个方法。你应该在
	 * <tt>initialize()</tt> 中完成所有初始化工作。
	 */
	public abstract void selfTest();

	/**
	 * 开始执行用户程序（如果适用的话）。
	 */
	public abstract void run();

	/**
	 * 终止这个内核。永远不会返回。
	 */
	public abstract void terminate();
}
