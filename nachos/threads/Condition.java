package nachos.threads;

import nachos.machine.*;

import java.util.LinkedList;

/**
 * 基于 semaphore 实现的 condition variable（条件变量）。
 *
 * <p>
 * condition variable 是一种同步原语。它没有值（这一点不同于 semaphore 或
 * lock），但线程仍然可以在它上面排队。
 *
 * <p>
 * <ul>
 *
 * <li><tt>sleep()</tt>：原子地释放 lock 并让出 CPU，直到被唤醒；然后重新
 * 获取 lock。
 *
 * <li><tt>wake()</tt>：如果可能，唤醒一个在这个 condition variable 上
 * sleep 的线程。
 *
 * <li><tt>wakeAll()</tt>：唤醒所有在这个 condition variable 上 sleep 的线程。
 *
 * </ul>
 *
 * <p>
 * 每个 condition variable 都关联着某个 lock。多个 condition variable 可以
 * 关联同一个 lock。这三个 condition variable 操作都只能在持有关联的 lock
 * 时使用。
 *
 * <p>
 * 在 Nachos 中，condition variable 被假定遵循 <i>Mesa-style</i> 语义。当
 * <tt>wake()</tt> 或 <tt>wakeAll()</tt> 唤醒另一个线程时，被唤醒的线程只是被
 * 放到 ready list 上，由被唤醒的线程自己负责重新获取 lock（这个重新获取在
 * <tt>sleep()</tt> 里处理）。
 *
 * <p>
 * 相比之下，有些 condition variable 的实现遵循 <i>Hoare-style</i> 语义：调用
 * <tt>wake()</tt> 的线程会把 lock 和 CPU 交给被唤醒的线程，被唤醒的线程立即
 * 运行，等它退出临界区（critical section）时，再把 lock 和 CPU 还给唤醒者。
 *
 * <p>
 * 使用 Mesa-style 语义的后果是：在被唤醒的线程获得运行机会之前，其他线程可能
 * 已经获取了 lock 并修改了数据结构。Mesa-style 语义的好处是实现起来容易得多。
 */
public class Condition {
	/**
	 * 分配（创建）一个新的 condition variable。
	 *
	 * @param conditionLock 与这个 condition variable 关联的 lock。
	 * 当前线程在使用 <tt>sleep()</tt>、<tt>wake()</tt> 或 <tt>wakeAll()</tt> 时，
	 * 必须持有这个 lock。
	 */
	public Condition(Lock conditionLock) {
		this.conditionLock = conditionLock;

		waitQueue = new LinkedList<Semaphore>();
	}

	/**
	 * 原子地释放关联的 lock，并在这个 condition variable 上 sleep，直到另一个
	 * 线程用 <tt>wake()</tt> 把它唤醒。当前线程必须持有关联的 lock。在
	 * <tt>sleep()</tt> 返回之前，线程会自动重新获取这个 lock。
	 *
	 * <p>
	 * 这个实现借助 semaphore 来完成：为每个等待的线程分配一个 semaphore。
	 * 唤醒者会对这个 semaphore 调用 <tt>V()</tt>，所以即使 lock 在调用
	 * <tt>P()</tt> 之前就已经释放了，sleep 的线程也不会错过唤醒。
	 */
	public void sleep() {
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());

		Semaphore waiter = new Semaphore(0);
		waitQueue.add(waiter);

		conditionLock.release();
		waiter.P();
		conditionLock.acquire();
	}

	/**
	 * 最多唤醒一个在这个 condition variable 上 sleep 的线程。当前线程必须
	 * 持有关联的 lock。
	 */
	public void wake() {
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());

		if (!waitQueue.isEmpty())
			((Semaphore) waitQueue.removeFirst()).V();
	}

	/**
	 * 唤醒所有在这个 condition variable 上 sleep 的线程。当前线程必须持有
	 * 关联的 lock。
	 */
	public void wakeAll() {
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());

		while (!waitQueue.isEmpty())
			wake();
	}

	private Lock conditionLock;

	private LinkedList<Semaphore> waitQueue;
}
