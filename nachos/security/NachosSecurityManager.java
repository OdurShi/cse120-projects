// PART OF THE MACHINE SIMULATION. DO NOT CHANGE.

package nachos.security;

import nachos.machine.*;

import java.io.File;
import java.security.Permission;
import java.io.FilePermission;
import java.util.PropertyPermission;
import java.net.NetPermission;
import java.awt.AWTPermission;
import java.lang.reflect.ReflectPermission;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.security.PrivilegedActionException;

/**
 * 保护运行环境不受恶意 Nachos 代码的破坏。
 */
@SuppressWarnings("removal")
public class NachosSecurityManager extends SecurityManager {
	/**
	 * 分配（创建）一个新的 Nachos security manager。
	 *
	 * @param testDirectory stub 文件系统可以使用的目录。
	 */
	public NachosSecurityManager(File testDirectory) {
		this.testDirectory = testDirectory;

		fullySecure = Config.getBoolean("NachosSecurityManager.fullySecure");
	}

	/**
	 * 返回这个 security manager 的一个 privilege 对象。这个 security manager
	 * 不能是当前生效的 security manager。
	 *
	 * @return 这个 security manager 的一个 privilege 对象。
	 */
	public Privilege getPrivilege() {
		Lib.assertTrue(this != System.getSecurityManager());

		return new PrivilegeProvider();
	}

	/**
	 * 安装这个 security manager。
	 */
	public void enable() {
		Lib.assertTrue(this != System.getSecurityManager());

		doPrivileged(new Runnable() {
			public void run() {
				System.setSecurityManager(NachosSecurityManager.this);
			}
		});
	}

	private class PrivilegeProvider extends Privilege {
		public void doPrivileged(Runnable action) {
			NachosSecurityManager.this.doPrivileged(action);
		}

		public Object doPrivileged(PrivilegedAction action) {
			return NachosSecurityManager.this.doPrivileged(action);
		}

		public Object doPrivileged(PrivilegedExceptionAction action)
				throws PrivilegedActionException {
			return NachosSecurityManager.this.doPrivileged(action);
		}

		public void exit(int exitStatus) {
			invokeExitNotificationHandlers();
			NachosSecurityManager.this.exit(exitStatus);
		}
	}

	private void enablePrivilege() {
		if (privilegeCount == 0) {
			Lib.assertTrue(privileged == null);
			privileged = Thread.currentThread();
			privilegeCount++;
		}
		else {
			Lib.assertTrue(privileged == Thread.currentThread());
			privilegeCount++;
		}
	}

	private void rethrow(Throwable e) {
		disablePrivilege();

		if (e instanceof RuntimeException)
			throw (RuntimeException) e;
		else if (e instanceof Error)
			throw (Error) e;
		else
			Lib.assertNotReached();
	}

	private void disablePrivilege() {
		Lib.assertTrue(privileged != null && privilegeCount > 0);
		privilegeCount--;
		if (privilegeCount == 0)
			privileged = null;
	}

	private void forcePrivilege() {
		privileged = Thread.currentThread();
		privilegeCount = 1;
	}

	private void exit(int exitStatus) {
		forcePrivilege();
		System.exit(exitStatus);
	}

	private boolean isPrivileged() {
		// autograder 不允许创建非 Nachos 线程，所以……
		if (!TCB.isNachosThread())
			return true;

		return (privileged == Thread.currentThread());
	}

	private void doPrivileged(final Runnable action) {
		doPrivileged(new PrivilegedAction() {
			public Object run() {
				action.run();
				return null;
			}
		});
	}

	private Object doPrivileged(PrivilegedAction action) {
		Object result = null;
		enablePrivilege();
		try {
			result = action.run();
		}
		catch (Throwable e) {
			rethrow(e);
		}
		disablePrivilege();
		return result;
	}

	private Object doPrivileged(PrivilegedExceptionAction action)
			throws PrivilegedActionException {
		Object result = null;
		enablePrivilege();
		try {
			result = action.run();
		}
		catch (Exception e) {
			throw new PrivilegedActionException(e);
		}
		catch (Throwable e) {
			rethrow(e);
		}
		disablePrivilege();
		return result;
	}

	private void no() {
		throw new SecurityException();
	}

	private void no(Permission perm) {
		System.err.println("\n\nLacked permission: " + perm);
		throw new SecurityException();
	}

	/**
	 * 检查指定的权限。有些操作在不评分时是允许的，这些操作在这里进行管控。
	 *
	 * @param perm 要检查的权限。
	 */
	public void checkPermission(Permission perm) {
		String name = perm.getName();

		// 有些权限是严格禁止的
		if (perm instanceof RuntimePermission) {
			// 不允许创建 class loader
			if (name.equals("createClassLoader"))
				no(perm);
			// lambda 表达式需要这些权限
			if (name.equals("getProtectionDomain") ||
			    name.equals("accessDeclaredMembers"))
			    return;
		}

		// lambda 表达式需要这个权限
		if (perm instanceof ReflectPermission) {
		        if (name.equals("suppressAccessChecks"))
			    return;
		}

		// 不评分时，允许 AWT 那一堆乱七八糟的操作
		if (!fullySecure) {
			if (perm instanceof NetPermission) {
				// 加载 awt 相关的东西时可能需要
				if (name.equals("specifyStreamHandler"))
					return;
			}

			if (perm instanceof RuntimePermission) {
				// 可能需要加载 libawt
				if (name.startsWith("loadLibrary.")) {
					String lib = name.substring("loadLibrary.".length());
					if (lib.equals("awt")) {
						Lib.debug(dbgSecurity, "\tdynamically linking " + lib);
						return;
					}
				}
			}

			if (perm instanceof AWTPermission) {
				// 允许 AWT 相关的操作
				if (name.equals("accessEventQueue"))
					return;
			}
		}

		// 有些权限总是允许的
		if (perm instanceof PropertyPermission) {
			// 允许读取属性
			if (perm.getActions().equals("read"))
				return;
		}

		// 有些权限需要进一步检查
		if (perm instanceof FilePermission) {
			if (perm.getActions().equals("read")) {
				// test 目录只能在有特权时读取
				if (isPrivileged())
					return;

				enablePrivilege();

				// 没有特权时，不允许直接读取 test 目录
				try {
					File f = new File(name);
					if (f.isFile()) {
						File p = f.getParentFile();
						if (p != null) {
							if (p.equals(testDirectory))
								no(perm);
						}
					}
				}
				catch (Throwable e) {
					rethrow(e);
				}

				disablePrivilege();
				return;
			}
			else if (perm.getActions().equals("write")
					|| perm.getActions().equals("delete")) {
				// 只允许写 test 目录，而且必须有特权
				verifyPrivilege();

				try {
					File f = new File(name);
					if (f.isFile()) {
						File p = f.getParentFile();
						if (p != null && p.equals(testDirectory))
							return;
					}
				}
				catch (Throwable e) {
					no(perm);
				}
			}
			else if (perm.getActions().equals("execute")) {
				// 只有在有特权、并且存在网络时才允许执行
				verifyPrivilege();

				if (Machine.networkLink() == null)
					no(perm);
			}
			else {
				no(perm);
			}
		}

		// 默认情况下需要特权
		verifyPrivilege(perm);
	}

	/**
	 * 由 <tt>java.lang.Thread</tt> 的构造函数调用，为当前线程的子线程确定一个
	 * 线程组（thread group）。调用者必须有特权才能成功创建线程。
	 *
	 * @return 新线程的线程组；返回 <tt>null</tt> 表示使用当前线程的线程组。
	 */
	public ThreadGroup getThreadGroup() {
		verifyPrivilege();
		return null;
	}

	/**
	 * 验证调用者有特权。
	 */
	public void verifyPrivilege() {
		if (!isPrivileged())
			no();
	}

	/**
	 * 验证调用者有特权，以便检查指定的权限。
	 *
	 * @param perm 正在检查的权限。
	 */
	public void verifyPrivilege(Permission perm) {
		if (!isPrivileged())
			no(perm);
	}

	private File testDirectory;

	private boolean fullySecure;

	private Thread privileged = null;

	private int privilegeCount = 0;

	private static final char dbgSecurity = 'S';
}
