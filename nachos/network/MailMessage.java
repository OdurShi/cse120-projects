package nachos.network;

import nachos.machine.*;

/**
 * 一条邮件消息（mail message）。包括一个数据包 header、一个邮件 header，以及
 * 实际的有效载荷（payload）。
 *
 * @see nachos.machine.Packet
 */
public class MailMessage {
	/**
	 * 用指定的参数分配（创建）一条要发送的新邮件消息。
	 *
	 * @param dstLink 目标链路地址。
	 * @param dstPort 目标端口。
	 * @param srcLink 源链路地址。
	 * @param srcPort 源端口。
	 * @param contents 数据包的内容。
	 */
	public MailMessage(int dstLink, int dstPort, int srcLink, int srcPort,
			byte[] contents) throws MalformedPacketException {
		// 确保参数有效
		if (dstPort < 0 || dstPort >= portLimit || srcPort < 0
				|| srcPort >= portLimit || contents.length > maxContentsLength)
			throw new MalformedPacketException();

		this.dstPort = (byte) dstPort;
		this.srcPort = (byte) srcPort;
		this.contents = contents;

		byte[] packetContents = new byte[headerLength + contents.length];

		packetContents[0] = (byte) dstPort;
		packetContents[1] = (byte) srcPort;

		System.arraycopy(contents, 0, packetContents, headerLength,
				contents.length);

		packet = new Packet(dstLink, srcLink, packetContents);
	}

	/**
	 * 用从网络上收到的指定数据包，分配（创建）一条新的邮件消息。
	 *
	 * @param packet 包含这条邮件消息的数据包。
	 */
	public MailMessage(Packet packet) throws MalformedPacketException {
		this.packet = packet;

		// 确保 header 有效
		if (packet.contents.length < headerLength || packet.contents[0] < 0
				|| packet.contents[0] >= portLimit || packet.contents[1] < 0
				|| packet.contents[1] >= portLimit)
			throw new MalformedPacketException();

		dstPort = packet.contents[0];
		srcPort = packet.contents[1];

		contents = new byte[packet.contents.length - headerLength];
		System.arraycopy(packet.contents, headerLength, contents, 0,
				contents.length);
	}

	/**
	 * 返回消息 header 的字符串表示。
	 */
	public String toString() {
		return "from (" + packet.srcLink + ":" + srcPort + ") to ("
				+ packet.dstLink + ":" + dstPort + "), " + contents.length
				+ " bytes";
	}

	/** 这条消息，表示为一个可以通过网络链路发送的数据包。 */
	public Packet packet;

	/** 这条消息在目标机器上使用的端口。 */
	public int dstPort;

	/** 这条消息在源机器上使用的端口。 */
	public int srcPort;

	/** 这条消息的内容，不包括邮件消息 header。 */
	public byte[] contents;

	/**
	 * 邮件 header 的字节数。header 的格式如下：
	 *
	 * <table>
	 * <tr>
	 * <td>偏移</td>
	 * <td>大小</td>
	 * <td>值</td>
	 * </tr>
	 * <tr>
	 * <td>0</td>
	 * <td>1</td>
	 * <td>目标端口</td>
	 * </tr>
	 * <tr>
	 * <td>1</td>
	 * <td>1</td>
	 * <td>源端口</td>
	 * </tr>
	 * </table>
	 */
	public static final int headerLength = 2;

	/** 单条消息中可以包含的最大有效载荷（真正的数据）。 */
	public static final int maxContentsLength = Packet.maxContentsLength
			- headerLength;

	/**
	 * 邮件端口的上限。所有端口都在 <tt>0</tt> 到 <tt>portLimit - 1</tt> 之间。
	 */
	public static final int portLimit = 128;
}
