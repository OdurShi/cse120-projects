package nachos.threads;

import nachos.machine.*;

/**
 * <tt>Semaphore</tt>（信号量）是一种同步原语，它有一个无符号的值。
 * semaphore 只有两种操作：
 *
 * <ul>
 * <li><tt>P()</tt>：等待，直到 semaphore 的值大于零，然后把它减一。
 * <li><tt>V()</tt>：把 semaphore 的值加一；如果可能，唤醒一个在 <tt>P()</tt>
 * 中等待的线程。
 * </ul>
 *
 * <p>
 * 注意，这个 API 不允许线程直接读取 semaphore 的值。即使你读到了这个值，你
 * 也只知道它过去是多少，而不知道它现在是多少：因为在你拿到这个值的时候，可能
 * 已经发生了一次 context switch，其他线程可能已经调用了 <tt>P()</tt> 或
 * <tt>V()</tt>，所以真实的值现在可能已经不一样了。
 */
public class Semaphore {
	/**
	 * 分配（创建）一个新的 semaphore。
	 *
	 * @param initialValue 这个 semaphore 的初始值。
	 */
	public Semaphore(int initialValue) {
		value = initialValue;
	}

	/**
	 * 原子地等待这个 semaphore 变为非零，然后把它减一。
	 */
	public void P() {
		boolean intStatus = Machine.interrupt().disable();

		if (value == 0) {
			waitQueue.waitForAccess(KThread.currentThread());
			KThread.sleep();
		}
		else {
			value--;
		}

		Machine.interrupt().restore(intStatus);
	}

	/**
	 * 原子地把这个 semaphore 加一，并最多唤醒一个在这个 semaphore 上 sleep
	 * 的其他线程。
	 */
	public void V() {
		boolean intStatus = Machine.interrupt().disable();

		KThread thread = waitQueue.nextThread();
		if (thread != null) {
			thread.ready();
		}
		else {
			value++;
		}

		Machine.interrupt().restore(intStatus);
	}

	private static class PingTest implements Runnable {
		PingTest(Semaphore ping, Semaphore pong) {
			this.ping = ping;
			this.pong = pong;
		}

		public void run() {
			for (int i = 0; i < 10; i++) {
				ping.P();
				pong.V();
			}
		}

		private Semaphore ping;

		private Semaphore pong;
	}

	/**
	 * 测试这个模块是否正常工作。
	 */
	public static void selfTest() {
		Semaphore ping = new Semaphore(0);
		Semaphore pong = new Semaphore(0);

		new KThread(new PingTest(ping, pong)).setName("ping").fork();

		for (int i = 0; i < 10; i++) {
			ping.V();
			pong.P();
		}
	}

	private int value;

	private ThreadQueue waitQueue = ThreadedKernel.scheduler
			.newThreadQueue(false);
}
