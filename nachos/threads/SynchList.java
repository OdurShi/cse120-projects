package nachos.threads;

import java.util.LinkedList;
import nachos.machine.*;
import nachos.threads.*;

/**
 * 一个同步（线程安全）的队列。
 */
public class SynchList {
	/**
	 * 分配（创建）一个新的同步队列。
	 */
	public SynchList() {
		list = new LinkedList<Object>();
		lock = new Lock();
		listEmpty = new Condition(lock);
	}

	/**
	 * 把指定的对象加到队列末尾。如果有另一个线程正在 <tt>removeFirst()</tt>
	 * 中等待，就把它唤醒。
	 *
	 * @param o 要添加的对象。不能为 <tt>null</tt>。
	 */
	public void add(Object o) {
		Lib.assertTrue(o != null);

		lock.acquire();
		list.add(o);
		listEmpty.wake();
		lock.release();
	}

	/**
	 * 从队列头部移除一个对象；如有必要，会阻塞直到队列非空。
	 *
	 * @return 从队列头部移除的元素。
	 */
	public Object removeFirst() {
		Object o;

		lock.acquire();
		while (list.isEmpty())
			listEmpty.sleep();
		o = list.removeFirst();
		lock.release();

		return o;
	}

	private static class PingTest implements Runnable {
		PingTest(SynchList ping, SynchList pong) {
			this.ping = ping;
			this.pong = pong;
		}

		public void run() {
			for (int i = 0; i < 10; i++)
				pong.add(ping.removeFirst());
		}

		private SynchList ping;

		private SynchList pong;
	}

	/**
	 * 测试这个模块是否正常工作。
	 */
	public static void selfTest() {
		SynchList ping = new SynchList();
		SynchList pong = new SynchList();

		new KThread(new PingTest(ping, pong)).setName("ping").fork();

		for (int i = 0; i < 10; i++) {
			Integer o = Integer.valueOf(i);
			ping.add(o);
			Lib.assertTrue(pong.removeFirst() == o);
		}
	}

	private LinkedList<Object> list;

	private Lock lock;

	private Condition listEmpty;
}
