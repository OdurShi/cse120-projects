package nachos.network;

import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;
import nachos.vm.*;
import nachos.network.*;

/**
 * 一个支持网络的内核。
 */
public class NetKernel extends VMKernel {
	/**
	 * 分配（创建）一个新的网络内核。
	 */
	public NetKernel() {
		super();
	}

	/**
	 * 初始化这个内核。
	 */
	public void initialize(String[] args) {
		super.initialize(args);

		postOffice = new PostOffice();
	}

	/**
	 * 测试网络。创建一个 server 线程，在端口 1 上监听 ping 并发送回复。然后
	 * ping 一到两台主机。注意，这个测试假定网络是可靠的（即网络的可靠性为
	 * 1.0）。
	 */
	public void selfTest() {
		super.selfTest();

		KThread serverThread = new KThread(new Runnable() {
			public void run() {
				pingServer();
			}
		});

		serverThread.fork();

		System.out.println("Press any key to start the network test...");
		console.readByte(true);

		int local = Machine.networkLink().getLinkAddress();

		// 先 ping 本机
		ping(local);

		// 如果本机是 0 或 1，就 ping 另一台
		if (local <= 1)
			ping(1 - local);
	}

	private void ping(int dstLink) {
		int srcLink = Machine.networkLink().getLinkAddress();

		System.out.println("PING " + dstLink + " from " + srcLink);

		long startTime = Machine.timer().getTime();

		MailMessage ping;

		try {
			ping = new MailMessage(dstLink, 1, Machine.networkLink()
					.getLinkAddress(), 0, new byte[0]);
		}
		catch (MalformedPacketException e) {
			Lib.assertNotReached();
			return;
		}

		postOffice.send(ping);

		MailMessage ack = postOffice.receive(0);

		long endTime = Machine.timer().getTime();

		System.out.println("time=" + (endTime - startTime) + " ticks");
	}

	private void pingServer() {
		while (true) {
			MailMessage ping = postOffice.receive(1);

			MailMessage ack;

			try {
				ack = new MailMessage(ping.packet.srcLink, ping.srcPort,
						ping.packet.dstLink, ping.dstPort, ping.contents);
			}
			catch (MalformedPacketException e) {
				// 永远不应该发生……
				continue;
			}

			postOffice.send(ack);
		}
	}

	/**
	 * 开始运行用户程序。
	 */
	public void run() {
		super.run();
	}

	/**
	 * 终止这个内核。永远不会返回。
	 */
	public void terminate() {
		super.terminate();
	}

	private PostOffice postOffice;

	// 一些 dummy 变量，让 javac 更"聪明"（顺带把这些类也编译进来）
	private static NetProcess dummy1 = null;
}
