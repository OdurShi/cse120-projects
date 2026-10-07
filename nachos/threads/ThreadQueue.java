package nachos.threads;

/**
 * 对某种访问受限的资源进行访问调度。线程队列可以用来让多个线程共享这种受限
 * 的访问权。
 *
 * <p>
 * Nachos 中访问受限的例子包括：
 *
 * <ol>
 * <li>线程使用处理器的权利。同一时刻只能有一个线程在处理器上运行。
 *
 * <li>线程获取某个特定 lock 的权利。同一时刻一个 lock 只能被一个线程持有。
 *
 * <li>当 semaphore 为 0 时，线程从 <tt>Semaphore.P()</tt> 返回的权利。当另一个
 * 线程调用 <tt>Semaphore.V()</tt> 时，只有一个在 <tt>Semaphore.P()</tt> 中
 * 等待的线程可以被唤醒。
 *
 * <li>线程在 condition variable 上 sleep 时被唤醒的权利。当另一个线程调用
 * <tt>Condition.wake()</tt> 时，只有一个在该 condition variable 上 sleep 的
 * 线程可以被唤醒。
 *
 * <li>线程从 <tt>KThread.join()</tt> 返回的权利。在目标线程结束之前，线程
 * 不允许从 <tt>join()</tt> 返回。
 * </ol>
 *
 * 以上这些情况都涉及受限访问，因为在每种情况下，让所有线程同时拥有访问权
 * 都不一定可行（或不一定正确）。其中有些情况涉及具体的资源（例如处理器或
 * lock）；另一些则更加抽象（例如在 semaphore、condition variable 或 join
 * 上等待）。
 *
 * <p>
 * 所有线程队列的方法都必须在<b>关闭中断</b>的情况下调用。
 */
public abstract class ThreadQueue {
	/**
	 * 通知这个线程队列：指定的线程正在等待访问权。只有当该线程无法立即获得
	 * 访问权时（例如该线程想获取一个 lock，但另一个线程已经持有这个 lock），
	 * 才应调用这个方法。
	 *
	 * <p>
	 * 一个线程不能同时等待多个资源的访问权。例如，一个正在等待 lock 的线程
	 * 不能同时还在等待在处理器上运行；如果一个线程在等待 lock，它应该处于
	 * sleep 状态。
	 *
	 * <p>
	 * 不过，取决于具体的对象，一个线程在拥有某个对象访问权的同时去等待另一个
	 * 对象的访问权，可能是可以接受的。例如，一个线程可以在持有一个 lock 的
	 * 同时尝试获取另一个 lock。但要注意，在等待其他任何东西的访问权时，不能
	 * 占用处理器。
	 *
	 * @param thread 正在等待访问权的线程。
	 */
	public abstract void waitForAccess(KThread thread);

	/**
	 * 通知这个线程队列：另一个线程可以获得访问权了。选出并返回下一个获得
	 * 访问权的线程；如果没有线程在等待，则返回 <tt>null</tt>。
	 *
	 * <p>
	 * 如果这个受限访问对象会转移优先级，并且还有其他线程在等待访问权，那么
	 * 它们会把优先级捐赠给被返回的线程。
	 *
	 * @return 下一个获得访问权的线程；如果没有线程在等待，返回 <tt>null</tt>。
	 */
	public abstract KThread nextThread();

        /**
	 * 通过把队列中的线程移除来取消它。
	 *
	 * @return 如果线程在队列中并且已被移除，返回 <tt>true</tt>；否则返回
	 * <tt>false</tt>。
	 */
        public abstract boolean cancelThread(KThread thread);

	/**
	 * 通知这个线程队列：某个线程没有经过 <tt>request()</tt> 和
	 * <tt>nextThread()</tt> 就获得了访问权。例如，如果一个线程获取了一个没有
	 * 其他线程在等待的 lock，它就应该调用这个方法。
	 *
	 * <p>
	 * 对于从 <tt>nextThread()</tt> 返回的线程，不应调用这个方法。
	 *
	 * @param thread 获得了访问权、但不是从 <tt>nextThread()</tt> 返回的线程。
	 */
	public abstract void acquire(KThread thread);

	/**
	 * 打印所有正在等待访问权的线程，顺序不定。
	 */
	public abstract void print();
}
