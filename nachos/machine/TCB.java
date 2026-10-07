// PART OF THE MACHINE SIMULATION. DO NOT CHANGE.

package nachos.machine;

import nachos.security.*;
import nachos.threads.KThread;

import java.util.Vector;
import java.security.PrivilegedAction;

/**
 * TCB 模拟了创建 Nachos 线程、对它们做 context switch 以及销毁它们所需的底层
 * 细节。每个 TCB 控制一个底层的 JVM Thread 对象。
 *
 * <p>
 * 不要使用 <tt>java.lang.Thread</tt> 中的任何方法，因为它们与 TCB API 不兼容。
 * 大多数 <tt>Thread</tt> 方法要么会让 Nachos 崩溃，要么没有任何有用的效果。
 *
 * <p>
 * 不要在你代码的<b>任何地方</b>使用 <i>synchronized</i> 关键字。这是违反规则
 * 的，<i>而且</i>很容易让 nachos 死锁。
 */
@SuppressWarnings("removal")
public final class TCB {
	/**
	 * 分配（创建）一个新的 TCB。
	 */
	public TCB() {
	}

	/**
	 * 给 TCB 类授予创建线程所需的特权。之所以需要这样做，是因为与其他需要
	 * 特权的 machine 类不同，我们希望内核能够自行创建 TCB 对象。
	 *
	 * @param privilege 封装了对 Nachos 机器的特权访问。
	 */
	public static void givePrivilege(Privilege privilege) {
		TCB.privilege = privilege;
		privilege.tcb = new TCBPrivilege();
	}

	/**
	 * 让这个 TCB 所代表的线程开始执行。指定的 target 会在这个线程中运行。
	 */
	public void start(Runnable target) {
		/*
		 * 这里我们不使用同步，因为我们假定：要么这是第一次调用 start()，要么
		 * 我们是在另一个 TCB 的上下文中被调用的。由于我们同一时刻只允许一个
		 * TCB 运行，所以不需要同步。
		 *
		 * 唯一可能打破这个假定的情况，是我们的某个非 Nachos 线程使用了 TCB
		 * 的代码。
		 */

		/*
		 * 确保这个 TCB 还没有被启动过。如果 done 为 false，那么 destroy()
		 * 还没有把 javaThread 重新设回 null，所以我们可以把 javaThread 作为
		 * start() 是否已经被调用过的可靠标志。
		 */
		Lib.assertTrue(javaThread == null && !done);

		/*
		 * 确保已经在运行的 TCB 不会太多。这个限制是为了防止线程被滥用。
		 */
		Lib.assertTrue(runningThreads.size() < maxThreads);

		isFirstTCB = (currentTCB == null);

		/*
		 * 一个可能没必要的健全性检查：如果这不是第一个 TCB，我们确认当前线程
		 * 绑定在当前 TCB 上。只有当非 Nachos 线程调用 start() 时，这个检查
		 * 才可能失败。
		 */
		if (!isFirstTCB)
			Lib.assertTrue(currentTCB.javaThread == Thread.currentThread());

		/*
		 * 到这里所有检查都完成了，所以我们开始启动这个 TCB。无论它是不是第一个
		 * TCB，都会被加入 runningThreads，并且我们保存 target 闭包。
		 */
		runningThreads.add(this);

		this.target = target;

		if (!isFirstTCB) {
			/*
			 * 如果这不是第一个 TCB，我们必须创建一个新的 Java 线程来运行它。
			 * 创建 Java 线程是一个特权操作。
			 */
			tcbTarget = new Runnable() {
				public void run() {
					threadroot();
				}
			};

			privilege.doPrivileged(new Runnable() {
				public void run() {
					javaThread = new Thread(tcbTarget);
				}
			});

			/*
			 * 这个 Java 线程还没有启动，但我们需要让它阻塞在 yield() 中。
			 * 做法是：暂时关掉当前 TCB，启动新的 Java 线程，然后等待它在
			 * threadroot() 中把我们唤醒。一旦新 TCB 把我们唤醒，就可以安全地
			 * context switch 到这个新 TCB 了。
			 */
			currentTCB.running = false;

			this.javaThread.start();
			currentTCB.waitForInterrupt();
		}
		else {
			/*
			 * 这是第一个 TCB，所以我们不需要创建新的 Java 线程来运行它；
			 * 直接借用当前的 Java 线程即可。
			 */
			javaThread = Thread.currentThread();

			/* 现在我们只需要直接调用 threadroot()。 */
			threadroot();
		}
	}

	/**
	 * 返回当前正在运行的线程的 TCB。
	 */
	public static TCB currentTCB() {
		return currentTCB;
	}

	/**
	 * 在当前 TCB 和这个 TCB 之间做 context switch。这个 TCB 将成为新的当前
	 * TCB。这个 TCB 就是当前 TCB 也是可以的。
	 */
	public void contextSwitch() {
		/*
		 * 一个可能没必要的健全性检查：我们确认当前线程绑定在当前 TCB 上。
		 * 只有当非 Nachos 线程调用 start() 时，这个检查才可能失败。
		 */
		Lib.assertTrue(currentTCB.javaThread == Thread.currentThread());

		// 确保 AutoGrader.runningThread() 已经调用过 associateThread()
		Lib.assertTrue(currentTCB.associated);
		currentTCB.associated = false;

		// 不能从一个 TCB 切换到它自己
		if (this == currentTCB)
			return;

		/*
		 * 这里有一些同步方面的问题。一旦我们唤醒了下一个线程，就不能再对静态
		 * 变量或任何 TCB 的状态做任何假设。因此，在唤醒下一个线程之前，我们
		 * 必须先把 currentTCB 的值锁存下来，并把它的 running 标志设为 false
		 * （这样，如果我们在调用 yield() 之前被打断，那次中断会把 running
		 * 标志设好，yield() 就不会阻塞）。
		 */

		TCB previous = currentTCB;
		previous.running = false;

		this.interrupt();
		previous.yield();
	}

	/**
	 * 销毁这个 TCB。这个 TCB 不能正被当前线程使用。这个 TCB 还必须已经被
	 * autograder 授权销毁。
	 */
	public void destroy() {
		// 确保当前 TCB 是正确的
		Lib.assertTrue(currentTCB != null
				&& currentTCB.javaThread == Thread.currentThread());
		// 不能销毁当前线程
		Lib.assertTrue(this != currentTCB);
		// 线程必须已经启动，但还没有被销毁
		Lib.assertTrue(javaThread != null && !done);

		// 确保 AutoGrader.finishingCurrentThread() 已经调用过 authorizeDestroy()
		Lib.assertTrue(nachosThread == toBeDestroyed);
		toBeDestroyed = null;

		this.done = true;
		currentTCB.running = false;

		this.interrupt();
		currentTCB.waitForInterrupt();

		this.javaThread = null;
	}

	/**
	 * 销毁所有 TCB 并退出 Nachos。等同于 <tt>Machine.terminate()</tt>。
	 */
	public static void die() {
		privilege.exit(0);
	}

	/**
	 * 检查当前的 JVM 线程是否属于某个 Nachos TCB。AWT 的事件分发线程就是
	 * 一个非 Nachos 线程的例子。
	 *
	 * @return 如果当前 JVM 线程是一个 Nachos 线程，返回 <tt>true</tt>。
	 */
	public static boolean isNachosThread() {
		return (currentTCB != null && Thread.currentThread() == currentTCB.javaThread);
	}

	private void threadroot() {
		// 这里运行的应当是当前线程
		Lib.assertTrue(javaThread == Thread.currentThread());

		if (!isFirstTCB) {
			/*
			 * start() 正在等我们唤醒它，以此表示现在可以 context switch 到
			 * 我们了。我们让 running 标志保持为 false，这样即使在我们进入
			 * sleep 之前发生了 context switch，我们也仍然会运行。我们要做的
			 * 只是唤醒当前 TCB，然后等待被 contextSwitch() 或 destroy() 唤醒。
			 */

			currentTCB.interrupt();
			this.yield();
		}
		else {
			/*
			 * start() 是直接调用我们的，所以我们只需要初始化几样东西。
			 */

			currentTCB = this;
			running = true;
		}

		try {
			target.run();

			// 不经过下面某个 catch 块，是不可能从这里出去的
			Lib.assertNotReached();
		}
		catch (ThreadDeath e) {
			// 确保这个 TCB 是被正确销毁的
			if (!done) {
				System.out.print("\nTCB terminated improperly!\n");
				privilege.exit(1);
			}

			runningThreads.removeElement(this);
			if (runningThreads.isEmpty())
				privilege.exit(0);
		}
		catch (Throwable e) {
			System.out.print("\n");
			e.printStackTrace();

			runningThreads.removeElement(this);
			if (runningThreads.isEmpty())
				privilege.exit(1);
			else
				die();
		}
	}

	/**
	 * 当需要等待另一个 TCB context switch 到这个 TCB 时，由 threadroot() 和
	 * contextSwitch() 调用。由于这个 TCB 也可能是被销毁而不是被切换到，所以
	 * 醒来后我们要检查 <tt>done</tt> 标志。如果它被设置了，说明唤醒我们的那个
	 * TCB 正在 destroy() 中等待我们的确认。否则，我们只需把当前 TCB 设为这个
	 * TCB，然后返回。
	 */
	private void yield() {
		waitForInterrupt();

		if (done) {
			currentTCB.interrupt();
			throw new ThreadDeath();
		}

		currentTCB = this;
	}

	/**
	 * 在绑定到这个 TCB 的 monitor 上等待，直到它的 <tt>running</tt> 标志被设为
	 * <tt>true</tt>。每当一个 TCB 需要等待轮到自己运行时，就会使用
	 * <tt>waitForInterrupt()</tt>。这包括启动和销毁 TCB 时的「乒乓」过程，以及
	 * 从这个 TCB context switch 到另一个 TCB 的过程。我们不依赖
	 * <tt>currentTCB</tt>，因为它在我们被调用之前就已经被
	 * <tt>contextSwitch()</tt> 更新了。
	 */
	private synchronized void waitForInterrupt() {
		while (!running) {
			try {
				wait();
			}
			catch (InterruptedException e) {
			}
		}
	}

	/**
	 * 唤醒这个 TCB：把它的 <tt>running</tt> 标志设为 <tt>true</tt>，并向绑定在
	 * 它上面的 monitor 发信号。用于启动和销毁 TCB 时的「乒乓」过程，以及
	 * context switch 到这个 TCB 的过程。
	 */
	private synchronized void interrupt() {
		running = true;
		notify();
	}

	private void associateThread(KThread thread) {
		// 确保每次 context switch 只调用一次 AutoGrader.runningThread()
		Lib.assertTrue(!associated);
		associated = true;

		Lib.assertTrue(thread != null);

		if (nachosThread != null)
			Lib.assertTrue(thread == nachosThread);
		else
			nachosThread = thread;
	}

	private static void authorizeDestroy(KThread thread) {
		// 确保每次 destroy 只调用一次 AutoGrader.finishingThread()
		Lib.assertTrue(toBeDestroyed == null);
		toBeDestroyed = thread;
	}

	/**
	 * 已启动且未被销毁的 TCB 最多可以同时存在多少个。
	 */
	public static final int maxThreads = 250;

	/**
	 * 指向当前正在运行的 TCB 的引用。<tt>TCB</tt> 类被加载时它被初始化为
	 * <tt>null</tt>，然后第一次调用 <tt>start(Runnable)</tt> 时，把指向第一个
	 * TCB 的引用赋给 <tt>currentTCB</tt>。在那之后，只有 <tt>yield()</tt> 能把
	 * <tt>currentTCB</tt> 改成当前 TCB，并且只能在 <tt>waitForInterrupt()</tt>
	 * 返回之后。
	 *
	 * <p>
	 * 注意，如果当前线程没有绑定到某个 TCB（这包括为硬件模拟而创建的线程），
	 * 那么 <tt>currentTCB.javaThread</tt> 就不是当前线程。
	 */
	private static TCB currentTCB = null;

	/**
	 * 一个包含所有<i>正在运行的</i> TCB 对象的 vector。<tt>TCB</tt> 类被加载时
	 * 它被初始化为空 vector。TCB 对象只在 <tt>start(Runnable)</tt> 中被加入，
	 * 而 <tt>start(Runnable)</tt> 对每个 TCB 对象只能调用一次。TCB 对象只在
	 * <tt>threadroot()</tt> 的各个 <tt>catch</tt> 子句中被移除，线程终止时总会
	 * 执行其中一个。<tt>start(Runnable)</tt> 把 <tt>runningThreads</tt> 中的
	 * 线程数限制在 <tt>maxThreads</tt> 以内。如果 <tt>threadroot()</tt> 让
	 * <tt>runningThreads</tt> 中的 TCB 对象数降为零，Nachos 就会退出；所以一旦
	 * 第一个 TCB 被创建，这个 vector 基本上就不会为空。
	 */
	private static Vector<TCB> runningThreads = new Vector<TCB>();

	private static Privilege privilege;

	private static KThread toBeDestroyed = null;

	/**
	 * 当且仅当这个 TCB 是第一个启动的 TCB（即在
	 * <tt>Machine.main(String[])</tt> 中启动的那个）时为 <tt>true</tt>。由
	 * <tt>start(Runnable)</tt> 根据 <tt>currentTCB</tt> 是否已被初始化来设置。
	 */
	private boolean isFirstTCB;

	/**
	 * 指向绑定到这个 TCB 的 Java 线程的引用。它最初为 <tt>null</tt>，在
	 * <tt>start(Runnable)</tt> 中被赋值为一个 Java 线程，在 <tt>destroy()</tt>
	 * 中又被设回 <tt>null</tt>。
	 */
	private Thread javaThread = null;

	/**
	 * 当且仅当绑定到这个 TCB 的 Java 线程应当运行时为 <tt>true</tt>。这与是否
	 * 属于 <tt>runningThreads</tt> 是完全不同的条件：<tt>runningThreads</tt>
	 * 包含所有已启动且尚未终止的 TCB 对象。只有当所关联的 Java 线程应当尽快
	 * 运行时，<tt>running</tt> 才为 <tt>true</tt>。在启动或销毁一个 TCB 时，
	 * 它会暂时对当前 TCB 以外的某个线程为 true。
	 */
	private boolean running = false;

	/**
	 * 由 <tt>destroy()</tt> 设为 <tt>true</tt>，这样当注定被销毁的 TCB 中的
	 * <tt>waitForInterrupt()</tt> 返回时，<tt>yield()</tt> 就会知道当前 TCB
	 * 注定要被销毁。
	 */
	private boolean done = false;

	private KThread nachosThread = null;

	private boolean associated = false;

	private Runnable target;

	private Runnable tcbTarget;

	private static class TCBPrivilege implements Privilege.TCBPrivilege {
		public void associateThread(KThread thread) {
			Lib.assertTrue(currentTCB != null);
			currentTCB.associateThread(thread);
		}

		public void authorizeDestroy(KThread thread) {
			TCB.authorizeDestroy(thread);
		}
	}
}
