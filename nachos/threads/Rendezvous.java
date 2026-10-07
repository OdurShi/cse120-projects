package nachos.threads;

import nachos.machine.*;

/**
 * <i>Rendezvous</i> 让线程之间可以同步地交换值。
 */
public class Rendezvous {
    /**
     * 分配（创建）一个新的 Rendezvous。
     */
    public Rendezvous () {
    }

    /**
     * 和另一个线程同步地交换一个值。第一个调用 exchange 的线程 A（带着值 X）
     * 会阻塞，等待另一个线程 B（带着值 Y）。当线程 B 到来时，它会解除 A 的
     * 阻塞，然后两个线程交换值：值 Y 返回给线程 A，值 X 返回给线程 B。
     *
     * 不同的整数 tag 用作不同的、相互平行的同步点（也就是说，在不同 tag 上
     * 同步的线程之间互不影响）。同一个 tag 也可以被反复用于多次交换。
     *
     * @param tag 同步用的 tag。
     * @param value 要交换的整数值。
     */
    public int exchange (int tag, int value) {
	return 0;
    }
}
