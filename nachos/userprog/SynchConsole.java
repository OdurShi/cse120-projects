package nachos.userprog;

import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;

/**
 * 为机器的 console 提供一个简单的、同步的接口。也可以通过 <tt>OpenFile</tt>
 * 对象来访问这个接口。
 */
public class SynchConsole {
	/**
	 * 分配（创建）一个新的 <tt>SynchConsole</tt>。
	 *
	 * @param console 要使用的底层串行 console。
	 */
	public SynchConsole(SerialConsole console) {
		this.console = console;

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
		console.setInterruptHandlers(receiveHandler, sendHandler);
	}

	/**
	 * 返回接收到的下一个无符号字节（范围是 <tt>0</tt> 到 <tt>255</tt>）。如果
	 * 还没有字节到达，根据 <i>block</i> 的值，要么阻塞直到有字节到达，要么立即
	 * 返回。
	 *
	 * @param block 如果没有可用的字节时 <tt>readByte()</tt> 应该等待，则为
	 * <tt>true</tt>。
	 * @return 读到的下一个字节；如果 <tt>block</tt> 为 <tt>false</tt> 并且没有
	 * 可用的字节，返回 -1。
	 */
	public int readByte(boolean block) {
		int value;
		boolean intStatus = Machine.interrupt().disable();
		readLock.acquire();

		if (block || charAvailable) {
			charAvailable = false;
			readWait.P();

			value = console.readByte();
			Lib.assertTrue(value != -1);
		}
		else {
			value = -1;
		}

		readLock.release();
		Machine.interrupt().restore(intStatus);
		return value;
	}

	/**
	 * 返回一个 <tt>OpenFile</tt>，可以用来像读文件一样读取这个 console。
	 *
	 * @return 一个可以读取这个 console 的文件。
	 */
	public OpenFile openForReading() {
		return new File(true, false);
	}

	private void receiveInterrupt() {
		charAvailable = true;
		readWait.V();
	}

	/**
	 * 发送一个字节。阻塞直到发送完成。
	 *
	 * @param value 要发送的字节（高 24 位会被忽略）。
	 */
	public void writeByte(int value) {
		writeLock.acquire();
		console.writeByte(value);
		writeWait.P();
		writeLock.release();
	}

	/**
	 * 返回一个 <tt>OpenFile</tt>，可以用来像写文件一样写这个 console。
	 *
	 * @return 一个可以写这个 console 的文件。
	 */
	public OpenFile openForWriting() {
		return new File(false, true);
	}

	private void sendInterrupt() {
		writeWait.V();
	}

	private boolean charAvailable = false;

	private SerialConsole console;

	private Lock readLock = new Lock();

	private Lock writeLock = new Lock();

	private Semaphore readWait = new Semaphore(0);

	private Semaphore writeWait = new Semaphore(0);

	private class File extends OpenFile {
		File(boolean canRead, boolean canWrite) {
			super(null, "SynchConsole");

			this.canRead = canRead;
			this.canWrite = canWrite;
		}

		public void close() {
			canRead = canWrite = false;
		}

		public int read(byte[] buf, int offset, int length) {
			if (!canRead)
				return 0;

			int i;
			for (i = 0; i < length; i++) {
				int value = SynchConsole.this.readByte(false);
				if (value == -1)
					break;

				buf[offset + i] = (byte) value;
			}

			return i;
		}

		public int write(byte[] buf, int offset, int length) {
			if (!canWrite)
				return 0;

			for (int i = 0; i < length; i++)
				SynchConsole.this.writeByte(buf[offset + i]);

			return length;
		}

		private boolean canRead, canWrite;
	}
}
