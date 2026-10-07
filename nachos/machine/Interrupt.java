// PART OF THE MACHINE SIMULATION. DO NOT CHANGE.

package nachos.machine;

import nachos.security.*;

import java.util.TreeSet;
import java.util.Iterator;
import java.util.SortedSet;

/**
 * <tt>Interrupt</tt> 类模拟底层的中断硬件。这个硬件提供一个方法
 * （<tt>setStatus()</tt>）来开启或关闭中断。
 *
 * <p>
 * 为了模拟硬件，我们需要记录硬件设备将会引发的所有待处理（pending）中断，
 * 以及它们应该在什么时候发生。
 *
 * <p>
 * 这个模块还负责记录模拟时间。只有在发生以下情况时，时间才会前进：
 * <ul>
 * <li>中断从之前的关闭状态变为开启
 * <li>执行了一条 MIPS 指令
 * </ul>
 *
 * <p>
 * 因此，与真实硬件不同，中断（包括时间片到期引起的 context switch）不会在
 * 代码中任何开着中断的地方发生，而只会在代码中模拟时间前进的那些地方发生
 * （这样才会到达硬件模拟调用中断处理函数的时刻）。
 *
 * <p>
 * 这意味着同步不正确的代码在这个硬件模拟上可能运行得很好（即使时间片是随机
 * 的），但在真实硬件上却无法工作。不过，即使 Nachos 不总能检测出你的程序在
 * 真实环境中会在什么时候出错，你仍然应该写出正确同步的代码。
 */
public final class Interrupt {
	/**
	 * 分配（创建）一个新的中断控制器。
	 *
	 * @param privilege 封装了对 Nachos 机器的特权访问。
	 */
	public Interrupt(Privilege privilege) {
		System.out.println(" interrupt");

		this.privilege = privilege;
		privilege.interrupt = new InterruptPrivilege();

		enabled = false;
		pending = new TreeSet<PendingInterrupt>();
	}

	/**
	 * 开启中断。这个方法的效果与 <tt>setStatus(true)</tt> 相同。
	 */
	public void enable() {
		setStatus(true);
	}

	/**
	 * 关闭中断，并返回原来的中断状态。这个方法的效果与
	 * <tt>setStatus(false)</tt> 相同。
	 *
	 * @return 如果原来中断是开启的，返回 <tt>true</tt>。
	 */
	public boolean disable() {
		return setStatus(false);
	}

	/**
	 * 把中断恢复为指定的状态。这个方法的效果与
	 * <tt>setStatus(<i>status</i>)</tt> 相同。
	 *
	 * @param status 为 <tt>true</tt> 表示开启中断。
	 */
	public void restore(boolean status) {
		setStatus(status);
	}

	/**
	 * 把中断状态设为开启（<tt>true</tt>）或关闭（<tt>false</tt>），并返回之前的
	 * 状态。如果中断状态从关闭变为开启，模拟时间就会前进。
	 *
	 * @param status 为 <tt>true</tt> 表示开启中断。
	 * @return 如果原来中断是开启的，返回 <tt>true</tt>。
	 */
	public boolean setStatus(boolean status) {
		boolean oldStatus = enabled;
		enabled = status;

		if (oldStatus == false && status == true)
			tick(true);

		return oldStatus;
	}

	/**
	 * 检查中断是否处于开启状态。
	 *
	 * @return 如果中断是开启的，返回 <tt>true</tt>。
	 */
	public boolean enabled() {
		return enabled;
	}

	/**
	 * 检查中断是否处于关闭状态。
	 *
	 * @return 如果中断是关闭的，返回 <tt>true</tt>。
	 */
	public boolean disabled() {
		return !enabled;
	}

	private void schedule(long when, String type, Runnable handler) {
		Lib.assertTrue(when > 0);

		long time = privilege.stats.totalTicks + when;
		PendingInterrupt toOccur = new PendingInterrupt(time, type, handler);

		Lib.debug(dbgInt, "Scheduling the " + type
				+ " interrupt handler at time = " + time);

		pending.add(toOccur);
	}

	private void tick(boolean inKernelMode) {
		Stats stats = privilege.stats;

		if (inKernelMode) {
			stats.kernelTicks += Stats.KernelTick;
			stats.totalTicks += Stats.KernelTick;
		}
		else {
			stats.userTicks += Stats.UserTick;
			stats.totalTicks += Stats.UserTick;
		}

		if (Lib.test(dbgInt))
			System.out.println("== Tick " + stats.totalTicks + " ==");

		enabled = false;
		checkIfDue();
		enabled = true;
	}

	private void checkIfDue() {
		long time = privilege.stats.totalTicks;

		Lib.assertTrue(disabled());

		if (Lib.test(dbgInt))
			print();

		if (pending.isEmpty())
			return;

		if (((PendingInterrupt) pending.first()).time > time)
			return;

		Lib.debug(dbgInt, "Invoking interrupt handlers at time = " + time);

		while (!pending.isEmpty()
				&& ((PendingInterrupt) pending.first()).time <= time) {
			PendingInterrupt next = (PendingInterrupt) pending.first();
			pending.remove(next);

			Lib.assertTrue(next.time <= time);

			if (privilege.processor != null)
				privilege.processor.flushPipe();

			Lib.debug(dbgInt, "  " + next.type);

			next.handler.run();
		}

		Lib.debug(dbgInt, "  (end of list)");
	}

	private void print() {
		System.out.println("Time: " + privilege.stats.totalTicks
				+ ", interrupts " + (enabled ? "on" : "off"));
		System.out.println("Pending interrupts:");

		for (Iterator i = pending.iterator(); i.hasNext();) {
			PendingInterrupt toOccur = (PendingInterrupt) i.next();
			System.out.println("  " + toOccur.type + ", scheduled at "
					+ toOccur.time);
		}

		System.out.println("  (end of list)");
	}

	private class PendingInterrupt implements Comparable {
		PendingInterrupt(long time, String type, Runnable handler) {
			this.time = time;
			this.type = type;
			this.handler = handler;
			this.id = numPendingInterruptsCreated++;
		}

		public int compareTo(Object o) {
			PendingInterrupt toOccur = (PendingInterrupt) o;

			// 对不相等的对象不能返回 0，所以要检查所有字段
			if (time < toOccur.time)
				return -1;
			else if (time > toOccur.time)
				return 1;
			else if (id < toOccur.id)
				return -1;
			else if (id > toOccur.id)
				return 1;
			else
				return 0;
		}

		long time;

		String type;

		Runnable handler;

		private long id;
	}

	private long numPendingInterruptsCreated = 0;

	private Privilege privilege;

	private boolean enabled;

	private TreeSet<PendingInterrupt> pending;

	private static final char dbgInt = 'i';

	private class InterruptPrivilege implements Privilege.InterruptPrivilege {
		public void schedule(long when, String type, Runnable handler) {
			Interrupt.this.schedule(when, type, handler);
		}

		public void tick(boolean inKernelMode) {
			Interrupt.this.tick(inKernelMode);
		}
	}
}
