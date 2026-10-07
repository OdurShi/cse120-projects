package nachos.threads;

import nachos.machine.*;

import java.util.TreeSet;
import java.util.HashSet;
import java.util.Iterator;

/**
 * 根据线程优先级来选择线程的 scheduler。
 *
 * <p>
 * priority scheduler 给每个线程关联一个优先级。下一个出队的线程，其优先级
 * 总是不低于其他任何等待线程的优先级。和 round-robin scheduler 一样，在所有
 * 具有相同（最高）优先级的线程中，出队的是等待时间最长的那个线程。
 *
 * <p>
 * 本质上，priority scheduler 以 round-robin 的方式把访问权交给所有最高优先级
 * 的线程，而忽略其他所有线程。如果总是有更高优先级的线程在等待，就有可能让
 * 某个线程饿死（starve）。
 *
 * <p>
 * priority scheduler 必须部分地解决优先级反转（priority inversion）问题；
 * 具体来说，优先级必须能通过 lock 和 join 进行捐赠（donate）。
 */
public class PriorityScheduler extends Scheduler {
	/**
	 * 分配（创建）一个新的 priority scheduler。
	 */
	public PriorityScheduler() {
	}

	/**
	 * 分配（创建）一个新的 priority 线程队列。
	 *
	 * @param transferPriority 如果这个队列应当把等待线程的优先级转移给持有
	 * 该队列的线程，则为 <tt>true</tt>。
	 * @return 一个新的 priority 线程队列。
	 */
	public ThreadQueue newThreadQueue(boolean transferPriority) {
		return new PriorityQueue(transferPriority);
	}

	public int getPriority(KThread thread) {
		Lib.assertTrue(Machine.interrupt().disabled());

		return getThreadState(thread).getPriority();
	}

	public int getEffectivePriority(KThread thread) {
		Lib.assertTrue(Machine.interrupt().disabled());

		return getThreadState(thread).getEffectivePriority();
	}

	public void setPriority(KThread thread, int priority) {
		Lib.assertTrue(Machine.interrupt().disabled());

		Lib.assertTrue(priority >= priorityMinimum
				&& priority <= priorityMaximum);

		getThreadState(thread).setPriority(priority);
	}

	public boolean increasePriority() {
		boolean intStatus = Machine.interrupt().disable();
		boolean ret = true;

		KThread thread = KThread.currentThread();

		int priority = getPriority(thread);
		if (priority == priorityMaximum)
			ret = false;
		else
			setPriority(thread, priority + 1);

		Machine.interrupt().restore(intStatus);
		return ret;
	}

	public boolean decreasePriority() {
		boolean intStatus = Machine.interrupt().disable();
		boolean ret = true;

		KThread thread = KThread.currentThread();

		int priority = getPriority(thread);
		if (priority == priorityMinimum)
			ret = false;
		else
			setPriority(thread, priority - 1);

		Machine.interrupt().restore(intStatus);
		return ret;
	}

	/**
	 * 新线程的默认优先级。不要修改这个值。
	 */
	public static final int priorityDefault = 1;

	/**
	 * 线程可以拥有的最低优先级。不要修改这个值。
	 */
	public static final int priorityMinimum = 0;

	/**
	 * 线程可以拥有的最高优先级。不要修改这个值。
	 */
	public static final int priorityMaximum = 7;

	/**
	 * 返回指定线程的调度状态（scheduling state）。
	 *
	 * @param thread 要返回其调度状态的线程。
	 * @return 指定线程的调度状态。
	 */
	protected ThreadState getThreadState(KThread thread) {
		if (thread.schedulingState == null)
			thread.schedulingState = new ThreadState(thread);

		return (ThreadState) thread.schedulingState;
	}

	/**
	 * 按优先级对线程排序的 <tt>ThreadQueue</tt>。
	 */
	protected class PriorityQueue extends ThreadQueue {
		PriorityQueue(boolean transferPriority) {
			this.transferPriority = transferPriority;
		}

		public void waitForAccess(KThread thread) {
			Lib.assertTrue(Machine.interrupt().disabled());
			getThreadState(thread).waitForAccess(this);
		}

		public void acquire(KThread thread) {
			Lib.assertTrue(Machine.interrupt().disabled());
			getThreadState(thread).acquire(this);
		}

		public KThread nextThread() {
			Lib.assertTrue(Machine.interrupt().disabled());
			// 请实现我
			return null;
		}

		public boolean cancelThread(KThread thread) {
			Lib.assertTrue(Machine.interrupt().disabled());
			// 请实现我
			return false;
		}

		/**
		 * 返回 <tt>nextThread()</tt> 将会返回的下一个线程，但不修改这个队列的
		 * 状态。
		 *
		 * @return <tt>nextThread()</tt> 将会返回的下一个线程。
		 */
		protected ThreadState pickNextThread() {
			// 请实现我
			return null;
		}

		public void print() {
			Lib.assertTrue(Machine.interrupt().disabled());
			// 请实现我（如果你愿意的话）
		}

		/**
		 * 如果这个队列应当把等待线程的优先级转移给持有该队列的线程，则为
		 * <tt>true</tt>。
		 */
		public boolean transferPriority;
	}

	/**
	 * 一个线程的调度状态。它应当包括这个线程的优先级、它的有效优先级
	 * （effective priority）、它所持有的所有对象，以及它正在等待的队列（如果
	 * 有的话）。
	 *
	 * @see nachos.threads.KThread#schedulingState
	 */
	protected class ThreadState {
		/**
		 * 分配（创建）一个新的 <tt>ThreadState</tt> 对象，并把它关联到指定的
		 * 线程。
		 *
		 * @param thread 这个状态所属的线程。
		 */
		public ThreadState(KThread thread) {
			this.thread = thread;

			setPriority(priorityDefault);
		}

		/**
		 * 返回所关联线程的优先级。
		 *
		 * @return 所关联线程的优先级。
		 */
		public int getPriority() {
			return priority;
		}

		/**
		 * 返回所关联线程的有效优先级。
		 *
		 * @return 所关联线程的有效优先级。
		 */
		public int getEffectivePriority() {
			// 请实现我
			return priority;
		}

		/**
		 * 把所关联线程的优先级设为指定的值。
		 *
		 * @param priority 新的优先级。
		 */
		public void setPriority(int priority) {
			if (this.priority == priority)
				return;

			this.priority = priority;

			// 请实现我
		}

		/**
		 * 当在指定的 priority queue 上调用 <tt>waitForAccess(thread)</tt>
		 * （其中 <tt>thread</tt> 是所关联的线程）时，这个方法会被调用。因此，
		 * 所关联的线程正在等待访问由 <tt>waitQueue</tt> 守护的资源。只有当所
		 * 关联的线程无法立即获得访问权时，才会调用这个方法。
		 *
		 * @param waitQueue 所关联的线程现在正在等待的队列。
		 *
		 * @see nachos.threads.ThreadQueue#waitForAccess
		 */
		public void waitForAccess(PriorityQueue waitQueue) {
			// 请实现我
		}

		/**
		 * 当所关联的线程已经获得了对 <tt>waitQueue</tt> 所守护之物的访问权时，
		 * 这个方法会被调用。这可能是因为在 <tt>waitQueue</tt> 上调用了
		 * <tt>acquire(thread)</tt>（其中 <tt>thread</tt> 是所关联的线程），
		 * 也可能是因为在 <tt>waitQueue</tt> 上调用了 <tt>nextThread()</tt>。
		 *
		 * @see nachos.threads.ThreadQueue#acquire
		 * @see nachos.threads.ThreadQueue#nextThread
		 */
		public void acquire(PriorityQueue waitQueue) {
			// 请实现我
		}

		/** 与这个对象相关联的线程。 */
		protected KThread thread;

		/** 所关联线程的优先级。 */
		protected int priority;
	}
}
