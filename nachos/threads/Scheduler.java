package nachos.threads;

import nachos.machine.*;

/**
 * 协调一组同类的线程队列。
 *
 * @see nachos.threads.ThreadQueue
 */
public abstract class Scheduler {
	/**
	 * 分配（创建）一个新的 scheduler。
	 */
	public Scheduler() {
	}

	/**
	 * 分配（创建）一个新的线程队列。如果 <i>transferPriority</i> 为
	 * <tt>true</tt>，那么在这个新队列上等待的线程会把它们的「优先级」转移给
	 * 当前拥有该队列所守护之物访问权的线程。这是用来部分解决优先级反转
	 * （priority inversion）的机制。
	 *
	 * <p>
	 * 如果没有一个明确可以说是拥有「访问权」的线程（比如 semaphore 和
	 * condition variable 的情况），这个参数应该为 <tt>false</tt>，表示不应该
	 * 转移任何优先级。
	 *
	 * <p>
	 * 处理器是一个特例。把优先级捐赠给一个已经拥有处理器的线程显然没有意义。
	 * 创建处理器的等待队列时，这个参数应该为 <tt>false</tt>。
	 *
	 * <p>
	 * 其他情况下，捐赠优先级是有好处的。例如，lock 有一个明确的拥有者（持有
	 * lock 的线程），而且 lock 总是由获取它的同一个线程释放，所以可以通过把
	 * 一个等待 lock 的高优先级线程的优先级捐赠给持有 lock 的线程来帮助它。
	 * 因此，lock 的队列在创建时应该把这个参数设为 <tt>true</tt>。
	 *
	 * <p>
	 * 类似地，当一个线程在 <tt>join()</tt> 中 sleep、等待目标线程结束时，这个
	 * sleep 的线程应该把它的优先级捐赠给目标线程。因此，join 队列在创建时应该
	 * 把这个参数设为 <tt>true</tt>。
	 *
	 * @param transferPriority 如果拥有访问权的线程应当从在这个队列上等待的
	 * 线程那里获得优先级，则为 <tt>true</tt>。
	 * @return 一个新的线程队列。
	 */
	public abstract ThreadQueue newThreadQueue(boolean transferPriority);

	/**
	 * 获取指定线程的优先级。调用时必须关闭中断。
	 *
	 * @param thread 要获取其优先级的线程。
	 * @return 该线程的优先级。
	 */
	public int getPriority(KThread thread) {
		Lib.assertTrue(Machine.interrupt().disabled());
		return 0;
	}

	/**
	 * 获取当前线程的优先级。等价于
	 * <tt>getPriority(KThread.currentThread())</tt>。
	 *
	 * @return 当前线程的优先级。
	 */
	public int getPriority() {
		return getPriority(KThread.currentThread());
	}

	/**
	 * 获取指定线程的有效优先级（effective priority）。调用时必须关闭中断。
	 *
	 * <p>
	 * 一个线程的有效优先级，是把优先级捐赠考虑进去之后该线程的优先级。
	 *
	 * <p>
	 * 对于 priority scheduler，它是该线程自身优先级与所有通过 lock 或 join
	 * 等待该线程的其他线程的优先级中的最大值。
	 *
	 * <p>
	 * 对于 lottery scheduler，它是该线程自身的彩票数与所有通过 lock 或 join
	 * 等待该线程的其他线程的彩票数之和。
	 *
	 * @param thread 要获取其有效优先级的线程。
	 * @return 该线程的有效优先级。
	 */
	public int getEffectivePriority(KThread thread) {
		Lib.assertTrue(Machine.interrupt().disabled());
		return 0;
	}

	/**
	 * 获取当前线程的有效优先级。等价于
	 * <tt>getEffectivePriority(KThread.currentThread())</tt>。
	 *
	 * @return 当前线程的优先级。
	 */
	public int getEffectivePriority() {
		return getEffectivePriority(KThread.currentThread());
	}

	/**
	 * 设置指定线程的优先级。调用时必须关闭中断。
	 *
	 * @param thread 要设置其优先级的线程。
	 * @param priority 新的优先级。
	 */
	public void setPriority(KThread thread, int priority) {
		Lib.assertTrue(Machine.interrupt().disabled());
	}

	/**
	 * 设置当前线程的优先级。等价于
	 * <tt>setPriority(KThread.currentThread(), priority)</tt>。
	 *
	 * @param priority 新的优先级。
	 */
	public void setPriority(int priority) {
		setPriority(KThread.currentThread(), priority);
	}

	/**
	 * 如果可能，以某种依赖于具体 scheduler 的方式提高当前线程的优先级。
	 *
	 * @return 如果 scheduler 成功提高了当前线程的优先级，返回 <tt>true</tt>。
	 */
	public boolean increasePriority() {
		return false;
	}

	/**
	 * 如果可能，以某种依赖于具体 scheduler 的方式降低当前线程的优先级，降低的
	 * 幅度最好与调用 <tt>increasePriority()</tt> 时提高的幅度相同。
	 *
	 * @return 如果 scheduler 成功降低了当前线程的优先级，返回 <tt>true</tt>。
	 */
	public boolean decreasePriority() {
		return false;
	}
}
