package nachos.threads;

import nachos.machine.*;

/**
 * 通过关闭 interrupt 来实现同步的 condition variable（条件变量）。
 *
 * <p>
 * 你必须实现这个类。
 *
 * @see nachos.threads.Condition
 */
public class Condition2 {
	/**
	 * 分配（创建）一个新的 condition variable。
	 *
	 * @param conditionLock 与这个 condition variable 关联的 lock。
	 * 当前线程在使用 <tt>sleep()</tt>、<tt>wake()</tt> 或 <tt>wakeAll()</tt> 时，
	 * 必须持有这个 lock。
	 */
	public Condition2(Lock conditionLock) {
		this.conditionLock = conditionLock;
	}

	/**
	 * 原子地释放关联的 lock，并在这个 condition variable 上 sleep，直到另一个
	 * 线程用 <tt>wake()</tt> 把它唤醒。当前线程必须持有关联的 lock。在
	 * <tt>sleep()</tt> 返回之前，线程会自动重新获取这个 lock。
	 */
	public void sleep() {
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());

		conditionLock.release();

		conditionLock.acquire();
	}

	/**
	 * 最多唤醒一个在这个 condition variable 上 sleep 的线程。当前线程必须
	 * 持有关联的 lock。
	 */
	public void wake() {
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());
	}

	/**
	 * 唤醒所有在这个 condition variable 上 sleep 的线程。当前线程必须持有
	 * 关联的 lock。
	 */
	public void wakeAll() {
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());
	}

        /**
	 * 原子地释放关联的 lock，并在这个 condition variable 上 sleep，直到
	 * (1) 另一个线程用 <tt>wake()</tt> 把它唤醒，或者 (2) 指定的
	 * <i>timeout</i> 时间已经过去。当前线程必须持有关联的 lock。在
	 * <tt>sleep()</tt> 返回之前，线程会自动重新获取这个 lock。如果 timeout
	 * 为负数或 0，则立即返回。
	 */
        public void sleepFor(long timeout) {
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());
	}

        private Lock conditionLock;
}
