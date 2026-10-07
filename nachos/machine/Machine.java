// PART OF THE MACHINE SIMULATION. DO NOT CHANGE.

package nachos.machine;

import nachos.security.*;
import nachos.ag.*;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * 模拟机器的主类。负责处理命令行参数、构造所有模拟的硬件设备，并启动
 * grader（评分器）。
 */
@SuppressWarnings("removal")
public final class Machine {
	/**
	 * Nachos 的 main 入口。
	 *
	 * @param args 命令行参数。
	 */
	public static void main(final String[] args) {
		// 在较新版本的 Java 中，使用 String.format 会触发
		// java.util.Local 动态初始化它的默认值。在这个过程中，它会
		// 请求对系统属性的写权限。如果学生代码在 Nachos 启动之后
		// 第一次使用它，就会触发 SecurityException 并失败。作为一个
		// 方便的变通办法，下面这条语句会替想使用它的学生代码，
		// 安全地先把 java.util.Local 初始化好。

	        String s = String.format("nachos %d.0j initializing...", 5);
	        System.out.println(s);

		Lib.assertTrue(Machine.args == null);
		Machine.args = args;

		processArgs();

		Config.load(configFileName);

		// 获取当前目录 (.)
		baseDirectory = new File(new File("").getAbsolutePath());
		// 获取 nachos 目录 (./nachos)
		nachosDirectory = new File(baseDirectory, "nachos");

		String testDirectoryName = Config.getString("FileSystem.testDirectory");

		// 获取 test 目录
		if (testDirectoryName != null) {
			testDirectory = new File(testDirectoryName);
		}
		else {
			// 使用 ../test
			testDirectory = new File(baseDirectory.getParentFile(), "test");
		}

		securityManager = new NachosSecurityManager(testDirectory);
		privilege = securityManager.getPrivilege();

		privilege.machine = new MachinePrivilege();

		TCB.givePrivilege(privilege);
		privilege.stats = stats;

		// Java 的 SecurityManager 已经被弃用（deprecated）
		// securityManager.enable();
		createDevices();
		checkUserClasses();

		autoGrader = (AutoGrader) Lib.constructObject(autoGraderClassName);

		new TCB().start(new Runnable() {
			public void run() {
				autoGrader.start(privilege);
			}
		});
	}

	/**
	 * 让出 CPU 给非 Nachos 线程。在非抢占式的 JVM 中使用，让非 Nachos 线程
	 * 有机会运行。
	 */
	public static void yield() {
		Thread.yield();
	}

	/**
	 * 终止 Nachos。等同于 <tt>TCB.die()</tt>。
	 */
	public static void terminate() {
		TCB.die();
	}

	/**
	 * 因为一个未处理的异常或错误而终止 Nachos。
	 *
	 * @param e 该异常或错误。
	 */
	public static void terminate(Throwable e) {
		if (e instanceof ThreadDeath)
			throw (ThreadDeath) e;

		e.printStackTrace();
		terminate();
	}

	/**
	 * 打印统计信息，然后终止 Nachos。
	 */
	public static void halt() {
		System.out.print("Machine halting!\n\n");
		stats.print();
		terminate();
	}

	/**
	 * 返回一个包含所有命令行参数的数组。
	 *
	 * @return 传给 Nachos 的命令行参数。
	 */
	public static String[] getCommandLineArguments() {
		String[] result = new String[args.length];

		System.arraycopy(args, 0, result, 0, args.length);

		return result;
	}

	private static void processArgs() {
		for (int i = 0; i < args.length;) {
			String arg = args[i++];
			if (arg.length() > 0 && arg.charAt(0) == '-') {
				if (arg.equals("-d")) {
					Lib.assertTrue(i < args.length, "switch without argument");
					Lib.enableDebugFlags(args[i++]);
				}
				else if (arg.equals("-h")) {
					System.out.print(help);
					System.exit(1);
				}
				else if (arg.equals("-m")) {
					Lib.assertTrue(i < args.length, "switch without argument");
					try {
						numPhysPages = Integer.parseInt(args[i++]);
					}
					catch (NumberFormatException e) {
						Lib.assertNotReached("bad value for -m switch");
					}
				}
				else if (arg.equals("-s")) {
					Lib.assertTrue(i < args.length, "switch without argument");
					try {
						randomSeed = Long.parseLong(args[i++]);
					}
					catch (NumberFormatException e) {
						Lib.assertNotReached("bad value for -s switch");
					}
				}
				else if (arg.equals("-x")) {
					Lib.assertTrue(i < args.length, "switch without argument");
					shellProgramName = args[i++];
				}
				else if (arg.equals("-z")) {
					System.out.print(copyright);
					System.exit(1);
				}
				// 这些开关是保留给 autograder 使用的
				else if (arg.equals("-[]")) {
					Lib.assertTrue(i < args.length, "switch without argument");
					configFileName = args[i++];
				}
				else if (arg.equals("--")) {
					Lib.assertTrue(i < args.length, "switch without argument");
					autoGraderClassName = args[i++];
				}
			}
		}

		Lib.seedRandom(randomSeed);
	}

	private static void createDevices() {
		interrupt = new Interrupt(privilege);
		timer = new Timer(privilege);

		if (Config.getBoolean("Machine.bank"))
			bank = new ElevatorBank(privilege);

		if (Config.getBoolean("Machine.processor")) {
			if (numPhysPages == -1)
				numPhysPages = Config.getInteger("Processor.numPhysPages");
			processor = new Processor(privilege, numPhysPages);
		}

		if (Config.getBoolean("Machine.console"))
			console = new StandardConsole(privilege);

		if (Config.getBoolean("Machine.stubFileSystem"))
			stubFileSystem = new StubFileSystem(privilege, testDirectory);

		if (Config.getBoolean("Machine.networkLink"))
			networkLink = new NetworkLink(privilege);
	}

	private static void checkUserClasses() {
		System.out.println(" user-check");

		Class aclsInt = (new int[0]).getClass();
		Class clsObject = Lib.loadClass("java.lang.Object");
		Class clsRunnable = Lib.loadClass("java.lang.Runnable");
		Class clsString = Lib.loadClass("java.lang.String");

		Class clsKernel = Lib.loadClass("nachos.machine.Kernel");
		Class clsFileSystem = Lib.loadClass("nachos.machine.FileSystem");
		Class clsRiderControls = Lib.loadClass("nachos.machine.RiderControls");
		Class clsElevatorControls = Lib
				.loadClass("nachos.machine.ElevatorControls");
		Class clsRiderInterface = Lib
				.loadClass("nachos.machine.RiderInterface");
		Class clsElevatorControllerInterface = Lib
				.loadClass("nachos.machine.ElevatorControllerInterface");

		Class clsAlarm = Lib.loadClass("nachos.threads.Alarm");
		Class clsThreadedKernel = Lib
				.loadClass("nachos.threads.ThreadedKernel");
		Class clsKThread = Lib.loadClass("nachos.threads.KThread");
		//Class clsCommunicator = Lib.loadClass("nachos.threads.Communicator");
		Class clsSemaphore = Lib.loadClass("nachos.threads.Semaphore");
		Class clsLock = Lib.loadClass("nachos.threads.Lock");
		Class clsCondition = Lib.loadClass("nachos.threads.Condition");
		Class clsCondition2 = Lib.loadClass("nachos.threads.Condition2");
		Class clsRider = Lib.loadClass("nachos.threads.Rider");
		Class clsElevatorController = Lib
				.loadClass("nachos.threads.ElevatorController");

		Lib.checkDerivation(clsThreadedKernel, clsKernel);

		Lib.checkStaticField(clsThreadedKernel, "alarm", clsAlarm);
		Lib.checkStaticField(clsThreadedKernel, "fileSystem", clsFileSystem);

		Lib.checkMethod(clsAlarm, "waitUntil", new Class[] { long.class },
				void.class);

		Lib.checkConstructor(clsKThread, new Class[] {});
		Lib.checkConstructor(clsKThread, new Class[] { clsRunnable });

		Lib.checkStaticMethod(clsKThread, "currentThread", new Class[] {},
				clsKThread);
		Lib.checkStaticMethod(clsKThread, "finish", new Class[] {}, void.class);
		Lib.checkStaticMethod(clsKThread, "yield", new Class[] {}, void.class);
		Lib.checkStaticMethod(clsKThread, "sleep", new Class[] {}, void.class);

		Lib.checkMethod(clsKThread, "setTarget", new Class[] { clsRunnable },
				clsKThread);
		Lib.checkMethod(clsKThread, "setName", new Class[] { clsString },
				clsKThread);
		Lib.checkMethod(clsKThread, "getName", new Class[] {}, clsString);
		Lib.checkMethod(clsKThread, "fork", new Class[] {}, void.class);
		Lib.checkMethod(clsKThread, "ready", new Class[] {}, void.class);
		Lib.checkMethod(clsKThread, "join", new Class[] {}, void.class);

		Lib.checkField(clsKThread, "schedulingState", clsObject);

		//Lib.checkConstructor(clsCommunicator, new Class[] {});
		//Lib.checkMethod(clsCommunicator, "speak", new Class[] { int.class },
		//void.class);
                //Lib.checkMethod(clsCommunicator, "listen", new Class[] {}, int.class);

		Lib.checkConstructor(clsSemaphore, new Class[] { int.class });
		Lib.checkMethod(clsSemaphore, "P", new Class[] {}, void.class);
		Lib.checkMethod(clsSemaphore, "V", new Class[] {}, void.class);

		Lib.checkConstructor(clsLock, new Class[] {});
		Lib.checkMethod(clsLock, "acquire", new Class[] {}, void.class);
		Lib.checkMethod(clsLock, "release", new Class[] {}, void.class);
		Lib.checkMethod(clsLock, "isHeldByCurrentThread", new Class[] {},
				boolean.class);

		Lib.checkConstructor(clsCondition, new Class[] { clsLock });
		Lib.checkConstructor(clsCondition2, new Class[] { clsLock });

		Lib.checkMethod(clsCondition, "sleep", new Class[] {}, void.class);
		Lib.checkMethod(clsCondition, "wake", new Class[] {}, void.class);
		Lib.checkMethod(clsCondition, "wakeAll", new Class[] {}, void.class);
		Lib.checkMethod(clsCondition2, "sleep", new Class[] {}, void.class);
		Lib.checkMethod(clsCondition2, "wake", new Class[] {}, void.class);
		Lib.checkMethod(clsCondition2, "wakeAll", new Class[] {}, void.class);

		Lib.checkDerivation(clsRider, clsRiderInterface);

		Lib.checkConstructor(clsRider, new Class[] {});
		Lib.checkMethod(clsRider, "initialize", new Class[] { clsRiderControls,
				aclsInt }, void.class);

		Lib.checkDerivation(clsElevatorController,
				clsElevatorControllerInterface);

		Lib.checkConstructor(clsElevatorController, new Class[] {});
		Lib.checkMethod(clsElevatorController, "initialize",
				new Class[] { clsElevatorControls }, void.class);

		Class clsTE = Lib.loadClass("nachos.machine.TranslationEntry");
		Lib.checkFields (clsTE, Arrays.asList ("vpn", "ppn", "valid", "readOnly", "used", "dirty"));
	}

	/**
	 * 禁止实例化。
	 */
	private Machine() {
	}

	/**
	 * 返回硬件中断管理器。
	 *
	 * @return 硬件中断管理器。
	 */
	public static Interrupt interrupt() {
		return interrupt;
	}

	/**
	 * 返回硬件 timer。
	 *
	 * @return 硬件 timer。
	 */
	public static Timer timer() {
		return timer;
	}

	/**
	 * 返回硬件 elevator bank（电梯组）。
	 *
	 * @return 硬件 elevator bank；如果不存在，返回 <tt>null</tt>。
	 */
	public static ElevatorBank bank() {
		return bank;
	}

	/**
	 * 返回 MIPS 处理器。
	 *
	 * @return MIPS 处理器；如果不存在，返回 <tt>null</tt>。
	 */
	public static Processor processor() {
		return processor;
	}

	/**
	 * 返回硬件 console（控制台）。
	 *
	 * @return 硬件 console；如果不存在，返回 <tt>null</tt>。
	 */
	public static SerialConsole console() {
		return console;
	}

	/**
	 * 返回 stub 文件系统。
	 *
	 * @return stub 文件系统；如果不存在，返回 <tt>null</tt>。
	 */
	public static FileSystem stubFileSystem() {
		return stubFileSystem;
	}

	/**
	 * 返回网络链路（network link）。
	 *
	 * @return 网络链路；如果不存在，返回 <tt>null</tt>。
	 */
	public static NetworkLink networkLink() {
		return networkLink;
	}

	/**
	 * 返回 autograder。
	 *
	 * @return autograder。
	 */
	public static AutoGrader autoGrader() {
		return autoGrader;
	}

	private static Interrupt interrupt = null;

	private static Timer timer = null;

	private static ElevatorBank bank = null;

	private static Processor processor = null;

	private static SerialConsole console = null;

	private static FileSystem stubFileSystem = null;

	private static NetworkLink networkLink = null;

	private static AutoGrader autoGrader = null;

	private static String autoGraderClassName = "nachos.ag.AutoGrader";

	/**
	 * 返回运行用户程序的内核必须运行的 shell 程序的名字。确保
	 * <tt>UserKernel.run()</tt> <i>总是</i>用这个方法来决定运行哪个程序。
	 *
	 * @return 要运行的 shell 程序的名字。
	 */
	public static String getShellProgramName() {
		if (shellProgramName == null) {
			shellProgramName = Config.getString("Kernel.shellProgram");
			System.out.println ("No program specified using '-x', defaulting to " + shellProgramName);
		}

		Lib.assertTrue(shellProgramName != null);
		return shellProgramName;
	}

	private static String shellProgramName = null;

	/**
	 * 返回内核应该使用的进程类的名字。在多道程序（multi-programming）项目中，
	 * 返回 <tt>nachos.userprog.UserProcess</tt>。在 VM 项目中，返回
	 * <tt>nachos.vm.VMProcess</tt>。在网络（networking）项目中，返回
	 * <tt>nachos.network.NetProcess</tt>。
	 *
	 * @return 内核应该使用的进程类的名字。
	 *
	 * @see nachos.userprog.UserKernel#run
	 * @see nachos.userprog.UserProcess
	 * @see nachos.vm.VMProcess
	 * @see nachos.network.NetProcess
	 */
	public static String getProcessClassName() {
		if (processClassName == null)
			processClassName = Config.getString("Kernel.processClassName");

		Lib.assertTrue(processClassName != null);
		return processClassName;
	}

	private static String processClassName = null;

	private static NachosSecurityManager securityManager;

	private static Privilege privilege;

	private static String[] args = null;

	private static Stats stats = new Stats();
        public static void incrNumCOFFReads() { stats.numCOFFReads++; }
        public static void incrNumSwapReads() { stats.numSwapReads++; }
        public static void incrNumSwapWrites() { stats.numSwapWrites++; }
        public static void incrNumSwapSkips() { stats.numSwapSkips++; }

	private static int numPhysPages = -1;

	private static long randomSeed = 0;

	private static File baseDirectory, nachosDirectory, testDirectory;

	private static String configFileName = "nachos.conf";

	private static final String help = "\n"
			+ "Options:\n"
			+ "\n"
			+ "\t-d <debug flags>\n"
			+ "\t\tEnable some debug flags, e.g. -d ti\n"
			+ "\n"
			+ "\t-h\n"
			+ "\t\tPrint this help message.\n"
			+ "\n"
			+ "\t-m <pages>\n"
			+ "\t\tSpecify how many physical pages of memory to simulate.\n"
			+ "\n"
			+ "\t-s <seed>\n"
			+ "\t\tSpecify the seed for the random number generator (seed is a\n"
			+ "\t\tlong).\n" + "\n" + "\t-x <program>\n"
			+ "\t\tSpecify a program that UserKernel.run() should execute,\n"
			+ "\t\tinstead of the value of the configuration variable\n"
			+ "\t\tKernel.shellProgram\n" + "\n" + "\t-z\n"
			+ "\t\tprint the copyright message\n" + "\n"
			+ "\t-- <grader class>\n"
			+ "\t\tSpecify an autograder class to use, instead of\n"
			+ "\t\tnachos.ag.AutoGrader\n" + "\n" + "\t-# <grader arguments>\n"
			+ "\t\tSpecify the argument string to pass to the autograder.\n"
			+ "\n" + "\t-[] <config file>\n"
			+ "\t\tSpecifiy a config file to use, instead of nachos.conf\n"
			+ "";

	private static final String copyright = "\n"
			+ "Copyright 1992-2001 The Regents of the University of California.\n"
			+ "All rights reserved.\n"
			+ "\n"
			+ "Permission to use, copy, modify, and distribute this software and\n"
			+ "its documentation for any purpose, without fee, and without\n"
			+ "written agreement is hereby granted, provided that the above\n"
			+ "copyright notice and the following two paragraphs appear in all\n"
			+ "copies of this software.\n"
			+ "\n"
			+ "IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY\n"
			+ "PARTY FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL\n"
			+ "DAMAGES ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS\n"
			+ "DOCUMENTATION, EVEN IF THE UNIVERSITY OF CALIFORNIA HAS BEEN\n"
			+ "ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.\n"
			+ "\n"
			+ "THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY\n"
			+ "WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES\n"
			+ "OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.  THE\n"
			+ "SOFTWARE PROVIDED HEREUNDER IS ON AN \"AS IS\" BASIS, AND THE\n"
			+ "UNIVERSITY OF CALIFORNIA HAS NO OBLIGATION TO PROVIDE\n"
			+ "MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.\n";

	private static class MachinePrivilege implements Privilege.MachinePrivilege {
		public void setConsole(SerialConsole console) {
			Machine.console = console;
		}
	}

	// 一些 dummy 变量，让 javac 更"聪明"（顺带把这些类也编译进来）
	private static Coff dummy1 = null;
}
