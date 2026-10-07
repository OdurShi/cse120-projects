// PART OF THE MACHINE SIMULATION. DO NOT CHANGE.

package nachos.machine;

/**
 * 一个链路层数据包。
 *
 * @see nachos.machine.NetworkLink
 */
public class Packet {
	/**
	 * 用指定的参数分配（创建）一个要发送的新数据包。
	 *
	 * @param dstLink 目标链路地址。
	 * @param srcLink 源链路地址。
	 * @param contents 数据包的内容。
	 */
	public Packet(int dstLink, int srcLink, byte[] contents)
			throws MalformedPacketException {
		// 确保参数有效
		if (dstLink < 0 || dstLink >= linkAddressLimit || srcLink < 0
				|| srcLink >= linkAddressLimit
				|| contents.length > maxContentsLength)
			throw new MalformedPacketException();

		this.dstLink = dstLink;
		this.srcLink = srcLink;
		this.contents = contents;

		packetBytes = new byte[headerLength + contents.length];

		packetBytes[0] = NetworkLink.networkID;
		packetBytes[1] = (byte) dstLink;
		packetBytes[2] = (byte) srcLink;
		packetBytes[3] = (byte) contents.length;

		// 要是 java 有子数组我就用了。不过 System.arraycopy 也还行……
		System.arraycopy(contents, 0, packetBytes, headerLength,
				contents.length);
	}

	/**
	 * 用从网络上收到的指定字节数组，分配（创建）一个新数据包。
	 *
	 * @param packetBytes 组成这个数据包的字节。
	 */
	public Packet(byte[] packetBytes) throws MalformedPacketException {
		this.packetBytes = packetBytes;

		// 确保 header 有效
		if (packetBytes.length < headerLength
				|| packetBytes[0] != NetworkLink.networkID
				|| packetBytes[1] < 0 || packetBytes[1] >= linkAddressLimit
				|| packetBytes[2] < 0 || packetBytes[2] >= linkAddressLimit
				|| packetBytes[3] < 0
				|| packetBytes[3] > packetBytes.length - 4)
			throw new MalformedPacketException();

		dstLink = packetBytes[1];
		srcLink = packetBytes[2];

		contents = new byte[packetBytes[3]];
		System.arraycopy(packetBytes, headerLength, contents, 0,
				contents.length);
	}

	/** 这个数据包，表示为可以在网络上发送的字节数组。 */
	public byte[] packetBytes;

	/** 这个数据包的目标链路地址。 */
	public int dstLink;

	/** 这个数据包的源链路地址。 */
	public int srcLink;

	/** 这个数据包的内容，不包括链路层 header。 */
	public byte[] contents;

	/**
	 * 链路层数据包 header 的字节数。header 的格式如下：
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
	 * <td>network ID（用于检测冲突）</td>
	 * </tr>
	 * <tr>
	 * <td>1</td>
	 * <td>1</td>
	 * <td>目标链路地址</td>
	 * </tr>
	 * <tr>
	 * <td>2</td>
	 * <td>1</td>
	 * <td>源链路地址</td>
	 * </tr>
	 * <tr>
	 * <td>3</td>
	 * <td>1</td>
	 * <td>内容的长度</td>
	 * </tr>
	 * </table>
	 */
	public static final int headerLength = 4;

	/**
	 * 网络上可以发送或接收的数据包的最大长度（以字节为单位）。
	 */
	public static final int maxPacketLength = 32;

	/**
	 * 内容字节的最大数目（不包括 header）。注意它就等于
	 * <tt>maxPacketLength - headerLength</tt>。
	 */
	public static final int maxContentsLength = maxPacketLength - headerLength;

	/**
	 * Nachos 链路地址的上限。所有链路地址都在 <tt>0</tt> 到
	 * <tt>linkAddressLimit - 1</tt> 之间。
	 */
	public static final int linkAddressLimit = 128;
}
