// PART OF THE MACHINE SIMULATION. DO NOT CHANGE.

package nachos.machine;

import nachos.security.*;

import java.io.IOException;

/**
 * 一个使用 System.in 和 System.out 的文本 console。
 */
public class StandardConsole implements SerialConsole {
	/**
	 * 分配（创建）一个新的标准 console。
	 *
	 * @param privilege 封装了对 Nachos 机器的特权访问。
	 */
	public StandardConsole(Privilege privilege) {
		System.out.print(" console");

		this.privilege = privilege;

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
	}

	public final void setInterruptHandlers(Runnable receiveInterruptHandler,
			Runnable sendInterruptHandler) {
		this.receiveInterruptHandler = receiveInterruptHandler;
		this.sendInterruptHandler = sendInterruptHandler;
	}

	private void scheduleReceiveInterrupt() {
		privilege.interrupt.schedule(Stats.ConsoleTime, "console read",
				receiveInterrupt);
	}

	/**
	 * 尝试从这个 console 背后的对象读取一个字节。
	 *
	 * @return 读到的字节；如果没有可用的数据，返回 -1。
	 */
	protected int in() {
		try {
			if (System.in.available() <= 0)
				return -1;

			return System.in.read();
		}
		catch (IOException e) {
			return -1;
		}
	}

	private int translateCharacter(int c) {
		// 把 win32 的 0x0D 0x0A 序列转换成单个换行符
		if (c == 0x0A && prevCarriageReturn) {
			prevCarriageReturn = false;
			return -1;
		}
		prevCarriageReturn = (c == 0x0D);

		// 非 ASCII 字符视为无效
		if (c >= 0x80)
			return -1;
		// 退格字符
		else if (c == 0x04 || c == 0x08 || c == 0x19 || c == 0x1B || c == 0x7F)
			return '\b';
		// 如果在普通 ASCII 范围内，不需要做任何处理
		else if (c >= 0x20)
			return c;
		// 换行字符
		else if (c == 0x0A || c == 0x0D)
			return '\n';
		// 其他一切都视为无效
		else
			return -1;
	}

	private void receiveInterrupt() {
		Lib.assertTrue(incomingKey == -1);

		incomingKey = translateCharacter(in());
		if (incomingKey == -1) {
			scheduleReceiveInterrupt();
		}
		else {
			privilege.stats.numConsoleReads++;

			if (receiveInterruptHandler != null)
				receiveInterruptHandler.run();
		}
	}

	public final int readByte() {
		int key = incomingKey;

		if (incomingKey != -1) {
			incomingKey = -1;
			scheduleReceiveInterrupt();
		}

		return key;
	}

	private void scheduleSendInterrupt() {
		privilege.interrupt.schedule(Stats.ConsoleTime, "console write",
				sendInterrupt);
	}

	/**
	 * 向这个 console 背后的对象写一个字节。
	 *
	 * @param value 要写的字节。
	 */
	protected void out(int value) {
		System.out.write(value);
		System.out.flush();
	}

	private void sendInterrupt() {
		Lib.assertTrue(outgoingKey != -1);

		out(outgoingKey);
		outgoingKey = -1;

		privilege.stats.numConsoleWrites++;

		if (sendInterruptHandler != null)
			sendInterruptHandler.run();
	}

	public final void writeByte(int value) {
		if (outgoingKey == -1)
			scheduleSendInterrupt();

		outgoingKey = value & 0xFF;
	}

	private Privilege privilege = null;

	private Runnable receiveInterrupt;

	private Runnable sendInterrupt;

	private Runnable receiveInterruptHandler = null;

	private Runnable sendInterruptHandler = null;

	private int incomingKey = -1;

	private int outgoingKey = -1;

	private boolean prevCarriageReturn = false;
}
