package nachos.network;

import nachos.machine.*;
import nachos.threads.*;

/**
 * 一组消息队列，每个本地端口一个。<tt>PostOffice</tt> 直接与网络硬件交互。
 * 由于网络硬件的特性，可以保证消息永远不会损坏，但消息可能会丢失。
 *
 * <p>
 * post office 使用一个「邮递员」（postal worker）线程来等待从网络到达的消息，
 * 并把它们放进相应的队列中。这件事不能在接收中断处理函数中完成，因为每个队列
 * （用 <tt>SynchList</tt> 实现）都受一个 lock 保护。
 */
public class PostOffice {
	/**
	 * 用一个 <tt>SynchList</tt> 数组分配（创建）一个新的 post office。向网络
	 * 硬件注册中断处理函数，并启动「邮递员」线程。
	 */
	public PostOffice() {
		messageReceived = new Semaphore(0);
		messageSent = new Semaphore(0);
		sendLock = new Lock();

		queues = new SynchList[MailMessage.portLimit];
		for (int i = 0; i < queues.length; i++)
			queues[i] = new SynchList();

		Runnable receiveHandler = new Runnable() {
			public void run() {
				receiveInterrupt();
			}
		};
		Runnable sendHandler = new Runnable() {
			public void run() {
				sendInterrupt();
			}
		};
		Machine.networkLink().setInterruptHandlers(receiveHandler, sendHandler);

		KThread t = new KThread(new Runnable() {
			public void run() {
				postalDelivery();
			}
		});

		t.fork();
	}

	/**
	 * 在指定端口上取一条消息，如有必要就等待。
	 *
	 * @param port 等待消息的端口。
	 *
	 * @return 收到的消息。
	 */
	public MailMessage receive(int port) {
		Lib.assertTrue(port >= 0 && port < queues.length);

		Lib.debug(dbgNet, "waiting for mail on port " + port);

		MailMessage mail = (MailMessage) queues[port].removeFirst();

		if (Lib.test(dbgNet))
			System.out.println("got mail on port " + port + ": " + mail);

		return mail;
	}

	/**
	 * 等待传入的消息，然后把它们放进正确的邮箱。
	 */
	private void postalDelivery() {
		while (true) {
			messageReceived.P();

			Packet p = Machine.networkLink().receive();

			MailMessage mail;

			try {
				mail = new MailMessage(p);
			}
			catch (MalformedPacketException e) {
				continue;
			}

			if (Lib.test(dbgNet))
				System.out.println("delivering mail to port " + mail.dstPort
						+ ": " + mail);

			// 原子地把消息加入邮箱，并唤醒一个正在等待的线程
			queues[mail.dstPort].add(mail);
		}
	}

	/**
	 * 当一个数据包已经到达、可以从网络链路中取出时调用。
	 */
	private void receiveInterrupt() {
		messageReceived.V();
	}

	/**
	 * 把一条消息发送到远程机器上的一个邮箱。
	 */
	public void send(MailMessage mail) {
		if (Lib.test(dbgNet))
			System.out.println("sending mail: " + mail);

		sendLock.acquire();

		Machine.networkLink().send(mail.packet);
		messageSent.P();

		sendLock.release();
	}

	/**
	 * 当一个数据包已经发送完毕、可以向网络链路排入下一个数据包时调用。注意，
	 * 即使上一个数据包被丢弃了，这个方法也会被调用。
	 */
	private void sendInterrupt() {
		messageSent.V();
	}

	private SynchList[] queues;

	private Semaphore messageReceived; // 有消息可以取出时被 V

	private Semaphore messageSent; // 可以排入新消息时被 V

	private Lock sendLock;

	private static final char dbgNet = 'n';
}
