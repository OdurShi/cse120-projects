package nachos.threads;

import nachos.machine.*;

/**
 * <i>communicator</i> 让线程之间可以同步地交换 32 位的消息。可以有多个线程
 * 在等待 <i>speak</i>（说），也可以有多个线程在等待 <i>listen</i>（听）。但
 * 永远不应该出现 speaker 和 listener 同时都在等待的情况，因为这时这两个线程
 * 就可以配成一对了。
 */
public class Communicator {
	/**
	 * 分配（创建）一个新的 communicator。
	 */
	public Communicator() {
	}

	/**
	 * 等待某个线程通过这个 communicator 来 listen，然后把 <i>word</i> 传给
	 * 这个 listener。
	 *
	 * <p>
	 * 直到这个线程和一个正在 listen 的线程配上对之后才返回。恰好只能有一个
	 * listener 收到 <i>word</i>。
	 *
	 * @param word 要传递的整数。
	 */
	public void speak(int word) {
	}

	/**
	 * 等待某个线程通过这个 communicator 来 speak，然后返回那个线程传给
	 * <tt>speak()</tt> 的 <i>word</i>。
	 *
	 * @return 传递过来的整数。
	 */
	public int listen() {
		return 0;
	}
}
