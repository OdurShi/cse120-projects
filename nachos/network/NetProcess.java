package nachos.network;

import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;
import nachos.vm.*;

/**
 * 一个支持网络 syscall 的 <tt>VMProcess</tt>。
 */
public class NetProcess extends VMProcess {
	/**
	 * 分配（创建）一个新进程。
	 */
	public NetProcess() {
		super();
	}

	private static final int syscallConnect = 11, syscallAccept = 12;

	/**
	 * 处理一个 syscall 异常。由 <tt>handleException()</tt> 调用。参数
	 * <i>syscall</i> 指明用户执行的是哪个 syscall：
	 *
	 * <table>
	 * <tr>
	 * <td>syscall 编号</td>
	 * <td>syscall 原型</td>
	 * </tr>
	 * <tr>
	 * <td>11</td>
	 * <td><tt>int  connect(int host, int port);</tt></td>
	 * </tr>
	 * <tr>
	 * <td>12</td>
	 * <td><tt>int  accept(int port);</tt></td>
	 * </tr>
	 * </table>
	 *
	 * @param syscall syscall 编号。
	 * @param a0 syscall 的第一个参数。
	 * @param a1 syscall 的第二个参数。
	 * @param a2 syscall 的第三个参数。
	 * @param a3 syscall 的第四个参数。
	 * @return 要返回给用户的值。
	 */
	public int handleSyscall(int syscall, int a0, int a1, int a2, int a3) {
		switch (syscall) {
		default:
			return super.handleSyscall(syscall, a0, a1, a2, a3);
		}
	}
}
