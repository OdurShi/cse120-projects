package nachos.threads;

import nachos.machine.*;

/**
 * <tt>Lock</tt> 是一种同步原语，它有两种状态：<i>busy</i>（被占用）和
 * <i>free</i>（空闲）。lock 只允许两种操作：
 *
 * <ul>
 * <li><tt>acquire()</tt>：原子地等待，直到 lock 变成 <i>free</i>，然后把它
 * 设为 <i>busy</i>。
 * <li><tt>release()</tt>：把 lock 设为 <i>free</i>，如果可能，唤醒一个正在
 * 等待的线程。
 * </ul>
 *
 * <p>
 * 另外，只有获取了 lock 的那个线程才能释放它。和 semaphore 一样，这个 API
 * 不允许你读取 lock 的状态（因为这个值在你读取之后可能马上就变了）。
 */
public class Lock {
	/**
	 * 分配（创建）一个新的 lock。lock 的初始状态是 <i>free</i>。
	 */
	public Lock() {
	}

	/**
	 * 原子地获取这个 lock。当前线程不能已经持有这个 lock。
	 */
	public void acquire() {
		Lib.assertTrue(!isHeldByCurrentThread());

		boolean intStatus = Machine.interrupt().disable();
		KThread thread = KThread.currentThread();

		if (lockHolder != null) {
			waitQueue.waitForAccess(thread);
			KThread.sleep();
		}
		else {
			waitQueue.acquire(thread);
			lockHolder = thread;
		}

		Lib.assertTrue(lockHolder == thread);

		Machine.interrupt().restore(intStatus);
	}

	/**
	 * 原子地释放这个 lock，让其他线程可以获取它。
	 */
	public void release() {
		Lib.assertTrue(isHeldByCurrentThread());

		boolean intStatus = Machine.interrupt().disable();

		if ((lockHolder = waitQueue.nextThread()) != null)
			lockHolder.ready();

		Machine.interrupt().restore(intStatus);
	}

	/**
	 * 检查当前线程是否持有这个 lock。
	 *
	 * @return 如果当前线程持有这个 lock，返回 true。
	 */
	public boolean isHeldByCurrentThread() {
		return (lockHolder == KThread.currentThread());
	}

	private KThread lockHolder = null;

	private ThreadQueue waitQueue = ThreadedKernel.scheduler
			.newThreadQueue(true);
}
