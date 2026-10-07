// PART OF THE MACHINE SIMULATION. DO NOT CHANGE.

package nachos.machine;

import nachos.security.*;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.SocketException;

/**
 * 一条全双工（full-duplex）网络链路。向网络上的其他机器有序但不可靠地投递
 * 有大小限制的数据包。同时保证数据包不会损坏。
 *
 * <p>
 * 回顾一下网络协议的一般分层：
 * <ul>
 * <li>会话层/传输层（Session/Transport）
 * <li>网络层（Network）
 * <li>链路层（Link）
 * <li>物理层（Physical）
 * </ul>
 *
 * <p>
 * 物理层向链路层提供比特流接口。这一层非常依赖硬件。
 *
 * <p>
 * 链路层利用物理层，向网络层提供数据包接口。链路层通常提供有大小限制的数据包
 * 的不可靠投递，但保证数据包不会乱序到达。有些链路还能防止数据包损坏。以太网
 * （ethernet）协议就是链路层的一个例子。
 *
 * <p>
 * 网络层的存在是为了把多个网络连接成一个互联网（internet）。网络层提供全局
 * 唯一的地址。路由器（也叫网关）在这一层把数据包在网络之间转发。网络层向同一
 * 互联网上的任何机器提供有大小限制、未损坏的数据包的无序、不可靠投递。最常用的
 * 网络层协议是 IP（Internet Protocol），它被用来连接 Internet。
 *
 * <p>
 * 会话层/传输层向应用程序提供字节流接口。这意味着传输层必须把未损坏的字节按
 * 发送的顺序交给应用程序。字节流必须先建立连接、最后断开连接，并且存在于端口
 * 之间，而不是机器之间。
 *
 * <p>
 * 这个类提供链路层的抽象。由于我们不允许不同的 Nachos 网络之间相互通信，所以
 * Nachos 中不需要网络层。这应该能简化你对会话层/传输层的设计，因为你可以假定
 * 数据包永远不会乱序到达。
 */
public class NetworkLink {
	/**
	 * 分配（创建）一条新的网络链路。
	 *
	 * <p>
	 * <tt>nachos.conf</tt> 指定了网络的可靠性。可靠性的取值在 0 到 1 之间，
	 * 表示任意一个数据包不会被网络丢弃的概率。
	 *
	 * @param privilege 封装了对 Nachos 机器的特权访问。
	 */
	public NetworkLink(Privilege privilege) {
		System.out.print(" network");

		this.privilege = privilege;

		try {
			localHost = InetAddress.getLocalHost();
		}
		catch (UnknownHostException e) {
			localHost = null;
		}

		Lib.assertTrue(localHost != null);

		reliability = Config.getDouble("NetworkLink.reliability");
		Lib.assertTrue(reliability > 0 && reliability <= 1.0);

		socket = null;

		for (linkAddress = 0; linkAddress < Packet.linkAddressLimit; linkAddress++) {
			try {
				socket = new DatagramSocket(portBase + linkAddress, localHost);
				break;
			}
			catch (SocketException e) {
			}
		}

		if (socket == null) {
			System.out.println("");
			System.out.println("Unable to acquire a link address!");
			Lib.assertNotReached();
		}

		System.out.print("(" + linkAddress + ")");

		receiveInterrupt = new Runnable() {
			public void run() {
				receiveInterrupt();
			}
		};

		sendInterrupt = new Runnable() {
			public void run() {
				sendInterrupt();
			}
		};

		scheduleReceiveInterrupt();

		Thread receiveThread = new Thread(new Runnable() {
			public void run() {
				receiveLoop();
			}
		});

		receiveThread.start();
	}

	/**
	 * 返回这条网络链路的地址。
	 *
	 * @return 这条网络链路的地址。
	 */
	public int getLinkAddress() {
		return linkAddress;
	}

	/**
	 * 设置这条链路的接收中断处理函数和发送中断处理函数。
	 *
	 * <p>
	 * 每当有一个数据包到达、可以用 <tt>receive()</tt> 读取时，接收中断处理
	 * 函数就会被调用。
	 *
	 * <p>
	 * 每当一个用 <tt>send()</tt> 发送的数据包发送完毕时，发送中断处理函数就会
	 * 被调用。这表示可以发送下一个数据包了。
	 *
	 * @param receiveInterruptHandler 有数据包到达时要调用的回调函数。
	 * @param sendInterruptHandler 可以发送下一个数据包时要调用的回调函数。
	 */
	public void setInterruptHandlers(Runnable receiveInterruptHandler,
			Runnable sendInterruptHandler) {
		this.receiveInterruptHandler = receiveInterruptHandler;
		this.sendInterruptHandler = sendInterruptHandler;
	}

	private void scheduleReceiveInterrupt() {
		privilege.interrupt.schedule(Stats.NetworkTime, "network recv",
				receiveInterrupt);
	}

	private synchronized void receiveInterrupt() {
		Lib.assertTrue(incomingPacket == null);

		if (incomingBytes != null) {
			if (Machine.autoGrader().canReceivePacket(privilege)) {
				try {
					incomingPacket = new Packet(incomingBytes);

					privilege.stats.numPacketsReceived++;
				}
				catch (MalformedPacketException e) {
				}
			}

			incomingBytes = null;
			notify();

			if (incomingPacket == null)
				scheduleReceiveInterrupt();
			else if (receiveInterruptHandler != null)
				receiveInterruptHandler.run();
		}
		else {
			scheduleReceiveInterrupt();
		}
	}

	/**
	 * 返回接收到的下一个数据包。
	 *
	 * @return 接收到的下一个数据包；如果没有可用的数据包，返回 <tt>null</tt>。
	 */
	public Packet receive() {
		Packet p = incomingPacket;

		if (incomingPacket != null) {
			incomingPacket = null;
			scheduleReceiveInterrupt();
		}

		return p;
	}

	private void receiveLoop() {
		while (true) {
			synchronized (this) {
				while (incomingBytes != null) {
					try {
						wait();
					}
					catch (InterruptedException e) {
					}
				}
			}

			byte[] packetBytes;

			try {
				byte[] buffer = new byte[Packet.maxPacketLength];

				DatagramPacket dp = new DatagramPacket(buffer, buffer.length);

				socket.receive(dp);

				packetBytes = new byte[dp.getLength()];

				System.arraycopy(buffer, 0, packetBytes, 0, packetBytes.length);
			}
			catch (IOException e) {
				return;
			}

			synchronized (this) {
				incomingBytes = packetBytes;
			}
		}
	}

	private void scheduleSendInterrupt() {
		privilege.interrupt.schedule(Stats.NetworkTime, "network send",
				sendInterrupt);
	}

	private void sendInterrupt() {
		Lib.assertTrue(outgoingPacket != null);

		// 根据可靠性随机丢弃数据包
		if (Machine.autoGrader().canSendPacket(privilege)
				&& Lib.random() <= reliability) {
			// 好的，不丢弃
			privilege.doPrivileged(new Runnable() {
				public void run() {
					sendPacket();
				}
			});
		}
		else {
			outgoingPacket = null;
		}

		if (sendInterruptHandler != null)
			sendInterruptHandler.run();
	}

	private void sendPacket() {
		Packet p = outgoingPacket;
		outgoingPacket = null;

		try {
			socket.send(new DatagramPacket(p.packetBytes, p.packetBytes.length,
					localHost, portBase + p.dstLink));

			privilege.stats.numPacketsSent++;
		}
		catch (IOException e) {
		}
	}

	/**
	 * 发送下一个数据包。如果已经有一个数据包正在发送，结果是未定义的。
	 *
	 * @param pkt 要发送的数据包。
	 */
	public void send(Packet pkt) {
		if (outgoingPacket == null)
			scheduleSendInterrupt();

		outgoingPacket = pkt;
	}

	private static final int hash;

	private static final int portBase;

	/**
	 * 这个 JVM 中所有网络链路所连接的网络的地址。它是对运行这个 Nachos 实例的
	 * JVM 的账户名做的哈希。它用来帮助防止其他用户的数据包意外干扰这个网络。
	 */
	public static final byte networkID;

	static {
		hash = System.getProperty("user.name").hashCode();
		portBase = 0x4E41 + Math.abs(hash % 0x4E41);
		networkID = (byte) (hash / 0x4E41);
	}

	private Privilege privilege;

	private Runnable receiveInterrupt;

	private Runnable sendInterrupt;

	private Runnable receiveInterruptHandler = null;

	private Runnable sendInterruptHandler = null;

	private InetAddress localHost;

	private DatagramSocket socket;

	private byte linkAddress;

	private double reliability;

	private byte[] incomingBytes = null;

	private Packet incomingPacket = null;

	private Packet outgoingPacket = null;

	private boolean sendBusy = false;
}
