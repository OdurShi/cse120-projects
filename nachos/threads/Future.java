package nachos.threads;

import java.util.*;
import java.util.function.IntSupplier;
import nachos.machine.*;

/**
 * <i>Future</i> 是一种方便使用异步操作的机制。
 */
public class Future {
    /**
     * 创建一个新的 <i>Future</i>。这个 <i>Future</i> 会在一个 KThread 中异步地
     * 调用所提供的 <i>function</i>。特别地，构造函数不应该因为调用
     * <i>function</i> 而阻塞。
     */
    public Future (IntSupplier function) {
    }

    /**
     * 返回调用创建 <i>Future</i> 时传入的 <i>function</i> 的结果。如果调用
     * <i>get</i> 时这个函数还没执行完，调用者就会被阻塞。如果函数已经执行完，
     * <i>get</i> 就返回函数的结果。注意 <i>get</i> 可以被调用任意多次（可能由
     * 多个线程调用），并且每次都应该返回同一个值。
     */
    public int get () {
	return -1;
    }
}
