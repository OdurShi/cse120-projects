// PART OF THE MACHINE SIMULATION. DO NOT CHANGE.

package nachos.machine;

import nachos.security.*;

/**
 * 串行 console 可以用来发送和接收字符。一次只能发送一个字符，一次也只能
 * 接收一个字符。
 */

public interface SerialConsole {
	/**
	 * 设置这个 console 的接收中断处理函数和发送中断处理函数。
	 *
	 * <p>
	 * 每当又有一个字节到达、可以用 <tt>readByte()</tt> 读取时，接收中断处理
	 * 函数就会被调用。
	 *
	 * <p>
	 * 每当一个用 <tt>writeByte()</tt> 发送的字节发送完毕时，发送中断处理函数
	 * 就会被调用。这表示可以发送下一个字节了。
	 *
	 * @param receiveInterruptHandler 有字节到达时要调用的回调函数。
	 * @param sendInterruptHandler 可以发送下一个字节时要调用的回调函数。
	 */
	public void setInterruptHandlers(Runnable receiveInterruptHandler,
			Runnable sendInterruptHandler);

	/**
	 * 返回接收到的下一个无符号字节（范围是 <tt>0</tt> 到 <tt>255</tt>）。
	 *
	 * @return 读到的下一个字节；如果没有可用的字节，返回 -1。
	 */
	public int readByte();

	/**
	 * 发送下一个字节。如果已经有一个字节正在发送，结果是未定义的。
	 *
	 * @param value 要发送的字节（高 24 位会被忽略）。
	 */
	public void writeByte(int value);
}
