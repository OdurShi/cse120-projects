// PART OF THE MACHINE SIMULATION. DO NOT CHANGE.

package nachos.security;

import nachos.machine.*;
import nachos.threads.KThread;

import java.util.LinkedList;
import java.util.Iterator;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.security.PrivilegedActionException;

/**
 * 一种允许对 Nachos 机器进行特权访问的能力（capability）。
 *
 * <p>
 * 有些特权操作由 Nachos security manager 守护：
 * <ol>
 * <li>创建线程
 * <li>在 test 目录中写入/删除文件
 * <li>以特定的状态码退出
 * </ol>
 * 这些操作只能通过 <tt>doPrivileged()</tt> 执行。
 *
 * <p>
 * 有些特权操作需要一个 capability：
 * <ol>
 * <li>安排（schedule）中断
 * <li>推进模拟时间
 * <li>访问机器统计信息
 * <li>安装 console
 * <li>清空模拟处理器的流水线
 * <li>批准 TCB 操作
 * </ol>
 * 这些操作可以直接用一个 <tt>Privilege</tt> 对象来执行。
 *
 * <p>
 * Nachos 内核<i>永远</i>不应该能够直接执行这些特权操作。如果你在某处发现了
 * 漏洞，请告诉相关人员。
 */
public abstract class Privilege {
	/**
	 * 分配（创建）一个新的 <tt>Privilege</tt> 对象。注意，在机器设备把它填好
	 * 之前，这个对象本身并不封装任何特权访问。
	 */
	public Privilege() {
	}

	/**
	 * 以特权执行指定的操作。
	 *
	 * @param action 要执行的操作。
	 */
	public abstract void doPrivileged(Runnable action);

	/**
	 * 以特权执行指定的 <tt>PrivilegedAction</tt>。
	 *
	 * @param action 要执行的操作。
	 * @return 该操作的返回值。
	 */
	public abstract Object doPrivileged(PrivilegedAction action);

	/**
	 * 以特权执行指定的 <tt>PrivilegedExceptionAction</tt>。
	 *
	 * @param action 要执行的操作。
	 * @return 该操作的返回值。
	 */
	public abstract Object doPrivileged(PrivilegedExceptionAction action)
			throws PrivilegedActionException;

	/**
	 * 以指定的状态退出 Nachos。
	 *
	 * @param exitStatus Nachos 进程的退出状态。
	 */
	public abstract void exit(int exitStatus);

	/**
	 * 添加一个 <tt>exit()</tt> 通知处理函数。exit() 会调用这个处理函数。
	 *
	 * @param handler 通知处理函数。
	 */
	public void addExitNotificationHandler(Runnable handler) {
		exitNotificationHandlers.add(handler);
	}

	/**
	 * 调用每一个通过 <tt>addExitNotificationHandler()</tt> 添加的
	 * <tt>exit()</tt> 通知处理函数。由 <tt>exit()</tt> 调用。
	 */
	protected void invokeExitNotificationHandlers() {
		for (Iterator i = exitNotificationHandlers.iterator(); i.hasNext();) {
			try {
				((Runnable) i.next()).run();
			}
			catch (Throwable e) {
				System.out.println("exit() notification handler failed");
			}
		}
	}

	private LinkedList<Runnable> exitNotificationHandlers = new LinkedList<Runnable>();

	/** Nachos 运行时统计信息。 */
	public Stats stats = null;

	/** 提供对 <tt>Machine</tt> 某些私有方法的访问。 */
	public MachinePrivilege machine = null;

	/** 提供对 <tt>Interrupt</tt> 某些私有方法的访问。 */
	public InterruptPrivilege interrupt = null;

	/** 提供对 <tt>Processor</tt> 某些私有方法的访问。 */
	public ProcessorPrivilege processor = null;

	/** 提供对 <tt>TCB</tt> 某些私有方法的访问。 */
	public TCBPrivilege tcb = null;

	/**
	 * 一个提供对 <tt>Machine</tt> 某些私有方法访问的接口。
	 */
	public interface MachinePrivilege {
		/**
		 * 安装一个硬件 console。
		 *
		 * @param console 新的硬件 console。
		 */
		public void setConsole(SerialConsole console);
	}

	/**
	 * 一个提供对 <tt>Interrupt</tt> 某些私有方法访问的接口。
	 */
	public interface InterruptPrivilege {
		/**
		 * 安排一个中断在将来的某个时间发生。
		 *
		 * @param when 距离中断发生还有多少个 tick。
		 * @param type 所安排中断类型的名字。
		 * @param handler 要调用的中断处理函数。
		 */
		public void schedule(long when, String type, Runnable handler);

		/**
		 * 推进模拟时间。
		 *
		 * @param inKernelMode 如果当前线程正在运行内核代码，则为 <tt>true</tt>；
		 * 如果当前线程正在运行 MIPS 用户代码，则为 <tt>false</tt>。
		 */
		public void tick(boolean inKernelMode);
	}

	/**
	 * 一个提供对 <tt>Processor</tt> 某些私有方法访问的接口。
	 */
	public interface ProcessorPrivilege {
		/**
		 * 清空处理器流水线，为切换到内核态做准备。
		 */
		public void flushPipe();
	}

	/**
	 * 一个提供对 <tt>TCB</tt> 某些私有方法访问的接口。
	 */
	public interface TCBPrivilege {
		/**
		 * 把当前 TCB 与指定的 <tt>KThread</tt> 关联起来。
		 * <tt>AutoGrader.runningThread()</tt> 在返回之前<i>必须</i>调用这个
		 * 方法。
		 *
		 * @param thread 当前线程。
		 */
		public void associateThread(KThread thread);

		/**
		 * 授权销毁与指定线程关联的 TCB。
		 *
		 * @param thread 其 TCB 即将被销毁的线程。
		 */
		public void authorizeDestroy(KThread thread);
	}
}
