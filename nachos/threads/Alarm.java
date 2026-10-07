package nachos.threads;

import nachos.machine.*;

/**
 * 利用硬件 timer 来实现抢占（preemption），并让线程可以 sleep 到某个时间点。
 */
public class Alarm {
	/**
	 * 分配（创建）一个新的 Alarm。把机器的 timer 中断处理函数设置为这个
	 * alarm 的回调函数。
	 *
	 * <p>
	 * <b>注意</b>：如果存在多于一个 alarm，Nachos 将无法正常工作。
	 */
	public Alarm() {
		Machine.timer().setInterruptHandler(new Runnable() {
			public void run() {
				timerInterrupt();
			}
		});
	}

	/**
	 * timer 中断处理函数。机器的 timer 会周期性地调用它（大约每 500 个
	 * clock tick 一次）。它会让当前线程 yield；如果有其他应该运行的线程，
	 * 就强制发生一次 context switch。
	 */
	public void timerInterrupt() {

	        /* 不要删除这个 yield 调用。 */
		KThread.currentThread().yield();
	}

	/**
	 * 让当前线程 sleep 至少 <i>x</i> 个 tick，并在 timer 中断处理函数中把它
	 * 唤醒。这个线程必须在满足下面条件的第一次 timer 中断中被唤醒（放入
	 * scheduler 的 ready set）：
	 *
	 * <p>
	 * <blockquote> (current time) >= (WaitUntil called time)+(x) </blockquote>
	 *
	 * 如果等待值 <i>x</i> 为负数或 0，则立即返回，不阻塞调用它的线程。
	 *
	 * @param x 至少要等待的 clock tick 数。
	 *
	 * @see nachos.machine.Timer#getTime()
	 */
	public void waitUntil(long x) {
		// 暂时先用一个投机取巧的写法让程序能跑起来（busy waiting 是不好的）
		long wakeTime = Machine.timer().getTime() + x;
		while (wakeTime > Machine.timer().getTime())
			KThread.yield();
	}

        /**
	 * 取消 <i>thread</i> 设置的任何 timer，相当于立即唤醒这个线程（把它放入
	 * scheduler 的 ready set），并返回 true。如果 <i>thread</i> 没有设置
	 * timer，则返回 false。
	 *
	 * <p>
	 * @param thread 要取消其 timer 的线程。
	 */
        public boolean cancel(KThread thread) {
		return false;
	}
}
