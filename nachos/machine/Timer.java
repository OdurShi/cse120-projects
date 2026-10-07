// PART OF THE MACHINE SIMULATION. DO NOT CHANGE.

package nachos.machine;

import nachos.security.*;

/**
 * 硬件 timer 大约每 500 个 clock tick 产生一次 CPU timer 中断。这意味着它可以
 * 用来实现时间片轮转（time-slicing），或者让一个线程 sleep 一段指定的时间。
 *
 * <tt>Timer</tt> 类模拟硬件 timer 的方式是：每经过大约 500 个 clock tick，
 * 就安排（schedule）一次 timer 中断。这里带有少量随机性，所以中断并不是精确
 * 地每 500 个 tick 发生一次。
 */
public final class Timer {
	/**
	 * 分配（创建）一个新的 timer。
	 *
	 * @param privilege 封装了对 Nachos 机器的特权访问。
	 */
	public Timer(Privilege privilege) {
		System.out.println(" timer");

		this.privilege = privilege;

		timerInterrupt = new Runnable() {
			public void run() {
				timerInterrupt();
			}
		};

		autoGraderInterrupt = new Runnable() {
			public void run() {
				Machine.autoGrader().timerInterrupt(Timer.this.privilege,
						lastTimerInterrupt);
			}
		};

		scheduleInterrupt();
	}

	/**
	 * 设置用作 timer 中断处理函数的回调。timer 中断处理函数大约每 500 个
	 * clock tick 被调用一次。
	 *
	 * @param handler timer 中断处理函数。
	 */
	public void setInterruptHandler(Runnable handler) {
		this.handler = handler;
	}

	/**
	 * 获取当前时间。
	 *
	 * @return 自 Nachos 启动以来经过的 clock tick 数。
	 */
	public long getTime() {
		return privilege.stats.totalTicks;
	}

	private void timerInterrupt() {
		scheduleInterrupt();
		scheduleAutoGraderInterrupt();

		lastTimerInterrupt = getTime();

		if (handler != null)
			handler.run();
	}

	private void scheduleInterrupt() {
		int delay = Stats.TimerTicks;
		delay += Lib.random(delay / 10) - (delay / 20);

		privilege.interrupt.schedule(delay, "timer", timerInterrupt);
	}

	private void scheduleAutoGraderInterrupt() {
		privilege.interrupt.schedule(1, "timerAG", autoGraderInterrupt);
	}

	private long lastTimerInterrupt;

	private Runnable timerInterrupt;

	private Runnable autoGraderInterrupt;

	private Privilege privilege;

	private Runnable handler = null;
}
