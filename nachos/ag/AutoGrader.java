// PART OF THE MACHINE SIMULATION. DO NOT CHANGE.

package nachos.ag;

import nachos.machine.*;
import nachos.security.*;
import nachos.threads.*;

import java.util.Hashtable;
import java.util.StringTokenizer;

/**
 * 默认的 autograder。加载内核，然后用 <tt>Kernel.selfTest()</tt> 测试它。
 */
public class AutoGrader {
	/**
	 * 分配（创建）一个新的 autograder。
	 */
	public AutoGrader() {
	}

	/**
	 * 启动这个 autograder。提取 <tt>-#</tt> 参数，调用 <tt>init()</tt>，加载并
	 * 初始化内核，然后调用 <tt>run()</tt>。
	 *
	 * @param privilege 封装了对 Nachos 机器的特权访问。
	 */
	public void start(Privilege privilege) {
		Lib.assertTrue(this.privilege == null, "start() called multiple times");
		this.privilege = privilege;

		String[] args = Machine.getCommandLineArguments();

		extractArguments(args);

		System.out.println(" grader");

		init();

		System.out.print("\n");

		kernel = (Kernel) Lib
				.constructObject(Config.getString("Kernel.kernel"));
		kernel.initialize(args);

		run();
	}

	private void extractArguments(String[] args) {
		String testArgsString = Config.getString("AutoGrader.testArgs");
		if (testArgsString == null) {
			testArgsString = "";
		}

		for (int i = 0; i < args.length;) {
			String arg = args[i++];
			if (arg.length() > 0 && arg.charAt(0) == '-') {
				if (arg.equals("-#")) {
					Lib.assertTrue(i < args.length,
							"-# switch missing argument");
					testArgsString = args[i++];
				}
			}
		}

		StringTokenizer st = new StringTokenizer(testArgsString, ",\n\t\f\r");

		while (st.hasMoreTokens()) {
			StringTokenizer pair = new StringTokenizer(st.nextToken(), "=");

			Lib.assertTrue(pair.hasMoreTokens(), "test argument missing key");
			String key = pair.nextToken();

			Lib.assertTrue(pair.hasMoreTokens(), "test argument missing value");
			String value = pair.nextToken();

			testArgs.put(key, value);
		}
	}

	String getStringArgument(String key) {
		String value = (String) testArgs.get(key);
		Lib.assertTrue(value != null, "getStringArgument(" + key
				+ ") failed to find key");
		return value;
	}

	int getIntegerArgument(String key) {
		try {
			return Integer.parseInt(getStringArgument(key));
		}
		catch (NumberFormatException e) {
			Lib.assertNotReached("getIntegerArgument(" + key + ") failed: "
					+ "value is not an integer");
			return 0;
		}
	}

	boolean getBooleanArgument(String key) {
		String value = getStringArgument(key);

		if (value.equals("1") || value.toLowerCase().equals("true")) {
			return true;
		}
		else if (value.equals("0") || value.toLowerCase().equals("false")) {
			return false;
		}
		else {
			Lib.assertNotReached("getBooleanArgument(" + key + ") failed: "
					+ "value is not a boolean");
			return false;
		}
	}

	long getTime() {
		return privilege.stats.totalTicks;
	}

	void targetLevel(int targetLevel) {
		this.targetLevel = targetLevel;
	}

	void level(int level) {
		this.level++;
		Lib.assertTrue(level == this.level,
				"level() advanced more than one step: test jumped ahead");

		if (level == targetLevel)
			done();
	}

	private int level = 0, targetLevel = 0;

	void done() {
		System.out.print("\nsuccess\n");
		privilege.exit(162);
	}

	private Hashtable<String, String> testArgs = new Hashtable<String, String>();

	void init() {
	}

	void run() {
		kernel.selfTest();
		kernel.run();
		kernel.terminate();
	}

	Privilege privilege = null;

	Kernel kernel;

	/**
	 * 通知 autograder：指定的线程是 idle 线程。<tt>KThread.createIdleThread()</tt>
	 * 在 fork idle 线程之前<i>必须</i>调用这个方法。
	 *
	 * @param idleThread idle 线程。
	 */
	public void setIdleThread(KThread idleThread) {
	}

	/**
	 * 通知 autograder：指定的线程已经进入 ready 状态。<tt>KThread.ready()</tt>
	 * 在返回之前<i>必须</i>调用这个方法。
	 *
	 * @param thread 已经被加入 ready set 的线程。
	 */
	public void readyThread(KThread thread) {
	}

	/**
	 * 通知 autograder：指定的线程现在正在运行。<tt>KThread.restoreState()</tt>
	 * 在返回之前<i>必须</i>调用这个方法。
	 *
	 * @param thread 现在正在运行的线程。
	 */
	public void runningThread(KThread thread) {
		privilege.tcb.associateThread(thread);
		currentThread = thread;
	}

	/**
	 * 通知 autograder：当前线程已经结束。<tt>KThread.finish()</tt> 在让这个
	 * 线程 sleep 并安排销毁它的 TCB 之前，<i>必须</i>调用这个方法。
	 */
	public void finishingCurrentThread() {
		privilege.tcb.authorizeDestroy(currentThread);
	}

	/**
	 * 通知 autograder：发生了一次 timer 中断，并且如果安装了 timer 中断处理
	 * 函数，它已经被软件处理了。由硬件 timer 调用。
	 *
	 * @param privilege 证明这次调用的真实性。
	 * @param time 发出 timer 中断的实际时间。
	 */
	public void timerInterrupt(Privilege privilege, long time) {
		Lib.assertTrue(privilege == this.privilege, "security violation");
	}

	/**
	 * 通知 autograder：一个用户程序执行了一条 syscall 指令。
	 *
	 * @param privilege 证明这次调用的真实性。
	 * @return 如果应该调用内核的异常处理函数，返回 <tt>true</tt>。
	 */
	public boolean exceptionHandler(Privilege privilege) {
		Lib.assertTrue(privilege == this.privilege, "security violation");
		return true;
	}

	/**
	 * 通知 autograder：<tt>Processor.run()</tt> 被调用了。可以用它来模拟用户
	 * 程序。
	 *
	 * @param privilege 证明这次调用的真实性。
	 */
	public void runProcessor(Privilege privilege) {
		Lib.assertTrue(privilege == this.privilege, "security violation");
	}

	/**
	 * 通知 autograder：正在为指定的文件构造一个 COFF 加载器。autograder 可以借此
	 * 提供它自己的 COFF 加载器，或者返回 <tt>null</tt> 表示使用默认加载器。
	 *
	 * @param file 正在被加载的可执行文件。
	 * @return 用来加载这个文件的加载器；返回 <tt>null</tt> 表示使用默认加载器。
	 */
	public Coff createLoader(OpenFile file) {
		return null;
	}


        /**
	 * 通知 autograder：当前进程已经结束。<tt>UserProcess.exit()</tt>
	 * <i>必须</i>调用这个方法。
	 */
        public void finishingCurrentProcess(int status) {
        }

        /**
         * 通知 autograder：机器即将被终止。<tt>UserProcess.halt()</tt>
	 * <i>必须</i>调用这个方法。
	 */
        public void finishingMachine() {
	}

	/**
	 * 请求发送一个数据包的许可。autograder 可以借此非常有选择地丢弃数据包。
	 *
	 * @param privilege 证明这次调用的真实性。
	 * @return 如果应该发送这个数据包，返回 <tt>true</tt>。
	 */
	public boolean canSendPacket(Privilege privilege) {
		Lib.assertTrue(privilege == this.privilege, "security violation");
		return true;
	}

	/**
	 * 请求接收一个数据包的许可。autograder 可以借此非常有选择地丢弃数据包。
	 *
	 * @param privilege 证明这次调用的真实性。
	 * @return 如果应该把这个数据包交给内核，返回 <tt>true</tt>。
	 */
	public boolean canReceivePacket(Privilege privilege) {
		Lib.assertTrue(privilege == this.privilege, "security violation");
		return true;
	}

	private KThread currentThread;
}
