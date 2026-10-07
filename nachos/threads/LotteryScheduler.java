package nachos.threads;

import nachos.machine.*;

import java.util.TreeSet;
import java.util.HashSet;
import java.util.Iterator;

/**
 * 用抽奖（lottery）方式选择线程的 scheduler。
 *
 * <p>
 * lottery scheduler 给每个线程分配若干张彩票（ticket）。当需要从队列中取出
 * 一个线程时，就在所有等待出队的线程的全部彩票中随机抽一次奖，持有中奖
 * 彩票的线程被选中。
 *
 * <p>
 * 注意，lottery scheduler 必须能处理大量彩票（有时多达数十亿张），所以不能
 * 为每一张彩票都维护状态。
 *
 * <p>
 * lottery scheduler 必须部分地解决优先级反转（priority inversion）问题；
 * 具体来说，彩票必须能通过 lock 和 join 进行转移。和 priority scheduler
 * 不同，这些彩票是相加的（而不是只取最大值）。
 */
public class LotteryScheduler extends PriorityScheduler {
	/**
	 * 分配（创建）一个新的 lottery scheduler。
	 */
	public LotteryScheduler() {
	}

	/**
	 * 分配（创建）一个新的 lottery 线程队列。
	 *
	 * @param transferPriority 如果这个队列应当把等待线程的彩票转移给持有
	 * 该队列的线程，则为 <tt>true</tt>。
	 * @return 一个新的 lottery 线程队列。
	 */
	public ThreadQueue newThreadQueue(boolean transferPriority) {
		// 请实现我
		return null;
	}
}
