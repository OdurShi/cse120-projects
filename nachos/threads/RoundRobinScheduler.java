package nachos.threads;

import nachos.machine.*;

import java.util.LinkedList;
import java.util.Iterator;

/**
 * round-robin scheduler 用 FIFO 队列（由链表实现）来记录等待的线程。当一个
 * 线程开始等待访问权时，它被追加到链表的末尾。下一个获得访问权的线程总是
 * 链表中的第一个线程。这样访问权就是按先来先服务（first-come first-serve）
 * 的方式分配的。
 */
public class RoundRobinScheduler extends Scheduler {
	/**
	 * 分配（创建）一个新的 round-robin scheduler。
	 */
	public RoundRobinScheduler() {
	}

	/**
	 * 分配（创建）一个新的 FIFO 线程队列。
	 *
	 * @param transferPriority 被忽略。round robin scheduler 没有优先级。
	 * @return 一个新的 FIFO 线程队列。
	 */
	public ThreadQueue newThreadQueue(boolean transferPriority) {
		return new FifoQueue();
	}

	private class FifoQueue extends ThreadQueue {
		/**
		 * 把一个线程加到等待队列的末尾。
		 *
		 * @param thread 要追加到队列中的线程。
		 */
		public void waitForAccess(KThread thread) {
			Lib.assertTrue(Machine.interrupt().disabled());

			waitQueue.add(thread);
		}

		/**
		 * 从队列的开头移除一个线程。
		 *
		 * @return 队列中的第一个线程；如果队列为空，返回 <tt>null</tt>。
		 */
		public KThread nextThread() {
			Lib.assertTrue(Machine.interrupt().disabled());

			if (waitQueue.isEmpty())
				return null;

			return (KThread) waitQueue.removeFirst();
		}

                /**
		 * 通过把队列中的线程移除来取消它。
		 *
		 * @return 如果线程在队列中并且已被移除，返回 <tt>true</tt>；否则返回
		 * <tt>false</tt>。
		 */
		public boolean cancelThread(KThread thread) {
			Lib.assertTrue(Machine.interrupt().disabled());

			return waitQueue.remove(thread);
		}

		/**
		 * 指定的线程没有通过 <tt>waitForAccess()</tt> 或 <tt>nextThread()</tt>
		 * 就获得了独占访问权。断言（assert）此时没有线程在等待访问权。
		 */
		public void acquire(KThread thread) {
			Lib.assertTrue(Machine.interrupt().disabled());

			Lib.assertTrue(waitQueue.isEmpty());
		}

		/**
		 * 打印队列的内容。
		 */
		public void print() {
			Lib.assertTrue(Machine.interrupt().disabled());

			for (Iterator i = waitQueue.iterator(); i.hasNext();)
				System.out.print((KThread) i.next() + " ");
			System.out.println();
		}

		private LinkedList<KThread> waitQueue = new LinkedList<KThread>();
	}
}
