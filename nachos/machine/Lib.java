// PART OF THE MACHINE SIMULATION. DO NOT CHANGE.

package nachos.machine;

import java.util.List;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.PrivilegedAction;
import java.util.Random;

/**
 * 断言（assertion）失败时抛出。
 */
class AssertionFailureError extends Error {
	AssertionFailureError() {
		super();
	}

	AssertionFailureError(String message) {
		super(message);
	}
}

/**
 * 提供各种杂项库函数。
 */
public final class Lib {
	/**
	 * 禁止实例化。
	 */
	private Lib() {
	}

	private static Random random = null;

	/**
	 * 为随机数生成器设置种子（seed）。只能调用一次。
	 *
	 * @param randomSeed 随机数生成器的种子。
	 */
	public static void seedRandom(long randomSeed) {
		assertTrue(random == null);
		random = new Random(randomSeed);
	}

	/**
	 * 返回一个 0 到 <i>range - 1</i> 之间的随机整数。在 <tt>seedRandom()</tt>
	 * 为随机数生成器设置种子之前，不能调用这个方法。
	 *
	 * @param range 一个正数，表示可能的返回值的个数。
	 * @return 指定范围内的一个随机整数。
	 */
	public static int random(int range) {
		assertTrue(range > 0);
		return random.nextInt(range);
	}

	/**
	 * 返回一个介于 0.0（含）和 1.0（不含）之间的随机 double。
	 *
	 * @return 一个介于 0.0 和 1.0 之间的随机 double。
	 */
	public static double random() {
		return random.nextDouble();
	}

	/**
	 * 断言 <i>expression</i> 为 <tt>true</tt>。如果不是，Nachos 会带着一条
	 * 错误信息退出。
	 *
	 * @param expression 要断言的表达式。
	 */
	public static void assertTrue(boolean expression) {
		if (!expression)
			throw new AssertionFailureError();
	}

	/**
	 * 断言 <i>expression</i> 为 <tt>true</tt>。如果不是，Nachos 会带着指定的
	 * 错误信息退出。
	 *
	 * @param expression 要断言的表达式。
	 * @param message 错误信息。
	 */
	public static void assertTrue(boolean expression, String message) {
		if (!expression)
			throw new AssertionFailureError(message);
	}

	/**
	 * 断言这个调用永远不会被执行到。等同于 <tt>assertTrue(false)</tt>。
	 */
	public static void assertNotReached() {
		assertTrue(false);
	}

	/**
	 * 断言这个调用永远不会被执行到，并附带指定的错误信息。等同于
	 * <tt>assertTrue(false, message)</tt>。
	 *
	 * @param message 错误信息。
	 */
	public static void assertNotReached(String message) {
		assertTrue(false, message);
	}

	/**
	 * 如果命令行中启用了 <i>flag</i>，就打印 <i>message</i>。要指定启用哪些
	 * flag，使用 -d 命令行选项。例如，要启用 flag a、c 和 e，这样做：
	 *
	 * <p>
	 *
	 * <pre>
	 * nachos -d ace
	 * </pre>
	 *
	 * <p>
	 * Nachos 已经用了几个调试 flag，但也鼓励你添加自己的 flag。
	 *
	 * @param flag 打印这条信息所必须设置的调试 flag。
	 * @param message 调试信息。
	 */
	public static void debug(char flag, String message) {
		if (test(flag))
			System.out.println(message);
	}

	/**
	 * 检查命令行中是否启用了 <i>flag</i>。
	 *
	 * @param flag 要检查的调试 flag。
	 *
	 * @return 如果命令行中启用了这个 flag，返回 <tt>true</tt>。
	 */
	public static boolean test(char flag) {
		if (debugFlags == null)
			return false;
		else if (debugFlags[(int) '+'])
			return true;
		else if (flag >= 0 && flag < 0x80 && debugFlags[(int) flag])
			return true;
		else
			return false;
	}

	/**
	 * 启用 <i>flagsString</i> 中的所有调试 flag。
	 *
	 * @param flagsString 要启用的 flag。
	 */
	public static void enableDebugFlags(String flagsString) {
		if (debugFlags == null)
			debugFlags = new boolean[0x80];

		char[] newFlags = flagsString.toCharArray();
		for (int i = 0; i < newFlags.length; i++) {
			char c = newFlags[i];
			if (c >= 0 && c < 0x80)
				debugFlags[(int) c] = true;
		}
	}

	/** 命令行中指定的调试 flag。 */
	private static boolean debugFlags[];

	/**
	 * 读取一个文件，并验证确实读到了所请求的字节数，以及读操作花费了非零的
	 * 时间。
	 *
	 * @param file 要读取的文件。
	 * @param position 开始读取的文件偏移量。
	 * @param buf 用来存放数据的缓冲区。
	 * @param offset 开始存放数据的缓冲区偏移量。
	 * @param length 要读取的字节数。
	 */
	public static void strictReadFile(OpenFile file, int position, byte[] buf,
			int offset, int length) {
		long startTime = Machine.timer().getTime();
		assertTrue(file.read(position, buf, offset, length) == length);
		long finishTime = Machine.timer().getTime();
		assertTrue(finishTime > startTime);
	}

	/**
	 * 把整个文件加载到内存中。
	 *
	 * @param file 要加载的文件。
	 * @return 一个包含整个文件内容的数组；如果出错，返回 <tt>null</tt>。
	 */
	public static byte[] loadFile(OpenFile file) {
		int startOffset = file.tell();

		int length = file.length();
		if (length < 0)
			return null;

		byte[] data = new byte[length];

		file.seek(0);
		int amount = file.read(data, 0, length);
		file.seek(startOffset);

		if (amount == length)
			return data;
		else
			return null;
	}

	/**
	 * 给文件做一个只读的快照。
	 *
	 * @param file 要做快照的文件。
	 * @return 这个文件的只读快照。
	 */
	public static OpenFile cloneFile(OpenFile file) {
		OpenFile clone = new ArrayFile(loadFile(file));

		clone.seek(file.tell());

		return clone;
	}

	/**
	 * 把一个 short 转换成它的小端（little-endian）字节串表示。
	 *
	 * @param array 用来存放字节串的数组。
	 * @param offset 字节串在数组中的起始偏移量。
	 * @param value 要转换的值。
	 */
	public static void bytesFromShort(byte[] array, int offset, short value) {
		array[offset + 0] = (byte) ((value >> 0) & 0xFF);
		array[offset + 1] = (byte) ((value >> 8) & 0xFF);
	}

	/**
	 * 把一个 int 转换成它的小端字节串表示。
	 *
	 * @param array 用来存放字节串的数组。
	 * @param offset 字节串在数组中的起始偏移量。
	 * @param value 要转换的值。
	 */
	public static void bytesFromInt(byte[] array, int offset, int value) {
		array[offset + 0] = (byte) ((value >> 0) & 0xFF);
		array[offset + 1] = (byte) ((value >> 8) & 0xFF);
		array[offset + 2] = (byte) ((value >> 16) & 0xFF);
		array[offset + 3] = (byte) ((value >> 24) & 0xFF);
	}

	/**
	 * 把一个 int 转换成它的小端字节串表示，并返回一个包含它的数组。
	 *
	 * @param value 要转换的值。
	 * @return 一个包含该字节串的数组。
	 */
	public static byte[] bytesFromInt(int value) {
		byte[] array = new byte[4];
		bytesFromInt(array, 0, value);
		return array;
	}

	/**
	 * 把一个 int 转换成指定长度的小端字节串表示。
	 *
	 * @param array 用来存放字节串的数组。
	 * @param offset 字节串在数组中的起始偏移量。
	 * @param length 要存放的字节数（必须是 1、2 或 4）。
	 * @param value 要转换的值。
	 */
	public static void bytesFromInt(byte[] array, int offset, int length,
			int value) {
		assertTrue(length == 1 || length == 2 || length == 4);

		switch (length) {
		case 1:
			array[offset] = (byte) value;
			break;
		case 2:
			bytesFromShort(array, offset, (short) value);
			break;
		case 4:
			bytesFromInt(array, offset, value);
			break;
		}
	}

	/**
	 * 从小端字节串表示转换成一个 short。
	 *
	 * @param array 包含字节串的数组。
	 * @param offset 字节串在数组中的偏移量。
	 * @return 对应的 short 值。
	 */
	public static short bytesToShort(byte[] array, int offset) {
		return (short) ((((short) array[offset + 0] & 0xFF) << 0) | (((short) array[offset + 1] & 0xFF) << 8));
	}

	/**
	 * 从小端字节串表示转换成一个无符号 short。
	 *
	 * @param array 包含字节串的数组。
	 * @param offset 字节串在数组中的偏移量。
	 * @return 对应的 short 值。
	 */
	public static int bytesToUnsignedShort(byte[] array, int offset) {
		return (((int) bytesToShort(array, offset)) & 0xFFFF);
	}

	/**
	 * 从小端字节串表示转换成一个 int。
	 *
	 * @param array 包含字节串的数组。
	 * @param offset 字节串在数组中的偏移量。
	 * @return 对应的 int 值。
	 */
	public static int bytesToInt(byte[] array, int offset) {
		return (int) ((((int) array[offset + 0] & 0xFF) << 0)
				| (((int) array[offset + 1] & 0xFF) << 8)
				| (((int) array[offset + 2] & 0xFF) << 16) | (((int) array[offset + 3] & 0xFF) << 24));
	}

	/**
	 * 从指定长度的小端字节串表示转换成一个 int。
	 *
	 * @param array 包含字节串的数组。
	 * @param offset 字节串在数组中的偏移量。
	 * @param length 字节串的长度。
	 * @return 对应的值。
	 */
	public static int bytesToInt(byte[] array, int offset, int length) {
		assertTrue(length == 1 || length == 2 || length == 4);

		switch (length) {
		case 1:
			return array[offset];
		case 2:
			return bytesToShort(array, offset);
		case 4:
			return bytesToInt(array, offset);
		default:
			return -1;
		}
	}

	/**
	 * 从一个可能以 null 结尾的字节数组转换成字符串。
	 *
	 * @param array 包含字节串的数组。
	 * @param offset 字节串在数组中的偏移量。
	 * @param length 字节串的最大长度。
	 * @return 一个包含指定字节的字符串，直到 null 结束符为止（如果有的话），
	 * 不包括 null 结束符。
	 */
	public static String bytesToString(byte[] array, int offset, int length) {
		int i;
		for (i = 0; i < length; i++) {
			if (array[offset + i] == 0)
				break;
		}

		return new String(array, offset, i);
	}

	/**
	 * 用掩码取出一段位子串，并把它移位到最低位。
	 *
	 * @param bits 位串。
	 * @param lowest 子串在位串中的起始位。
	 * @param size 子串的位数。
	 * @return 该子串。
	 */
	public static int extract(int bits, int lowest, int size) {
		if (size == 32)
			return (bits >> lowest);
		else
			return ((bits >> lowest) & ((1 << size) - 1));
	}

	/**
	 * 用掩码取出一段位子串，并把它移位到最低位。
	 *
	 * @param bits 位串。
	 * @param lowest 子串在位串中的起始位。
	 * @param size 子串的位数。
	 * @return 该子串。
	 */
	public static long extract(long bits, int lowest, int size) {
		if (size == 64)
			return (bits >> lowest);
		else
			return ((bits >> lowest) & ((1L << size) - 1));
	}

	/**
	 * 用掩码取出一段位子串并移位到最低位，然后对它做符号扩展。
	 *
	 * @param bits 位串。
	 * @param lowest 子串在位串中的起始位。
	 * @param size 子串的位数。
	 * @return 符号扩展后的子串。
	 */
	public static int extend(int bits, int lowest, int size) {
		int extra = 32 - (lowest + size);
		return ((extract(bits, lowest, size) << extra) >> extra);
	}

	/**
	 * 检查位串中的某一位是否被置位。
	 *
	 * @param flag 要检查的 flag。
	 * @param bits 位串。
	 * @return 如果 <tt>(bits & flag)</tt> 非零，返回 <tt>true</tt>。
	 */
	public static boolean test(long flag, long bits) {
		return ((bits & flag) != 0);
	}

	/**
	 * 生成整数参数的 16 进制大写字符串表示，并补齐位数。
	 *
	 * @param i 一个整数。
	 * @return 补齐后的 16 进制大写字符串表示。
	 */
	public static String toHexString(int i) {
		return toHexString(i, 8);
	}

	/**
	 * 生成整数参数的 16 进制大写字符串表示，并把它补齐到指定的位数。
	 *
	 * @param i 一个整数。
	 * @param pad 要补齐到的最少 16 进制位数。
	 * @return 补齐后的 16 进制大写字符串表示。
	 */
	public static String toHexString(int i, int pad) {
		String result = Integer.toHexString(i).toUpperCase();
		while (result.length() < pad)
			result = "0" + result;
		return result;
	}

	/**
	 * 两个非负整数相除，把商向上取整，并返回结果。
	 *
	 * @param a 被除数。
	 * @param b 除数。
	 * @return <tt>ceiling(a / b)</tt>。
	 */
	public static int divRoundUp(int a, int b) {
		assertTrue(a >= 0 && b > 0);

		return ((a + (b - 1)) / b);
	}

	/**
	 * 加载并返回指定名字的类；如果这个类无法加载，返回 <tt>null</tt>。
	 *
	 * @param className 要加载的类名。
	 * @return 加载好的类；如果出错，返回 <tt>null</tt>。
	 */
	public static Class tryLoadClass(String className) {
		try {
			return ClassLoader.getSystemClassLoader().loadClass(className);
		}
		catch (Throwable e) {
			return null;
		}
	}

	/**
	 * 加载并返回指定名字的类；出现任何错误都会终止 Nachos。
	 *
	 * @param className 要加载的类名。
	 * @return 加载好的类。
	 */
	public static Class loadClass(String className) {
		try {
			return ClassLoader.getSystemClassLoader().loadClass(className);
		}
		catch (Throwable e) {
			Machine.terminate(e);
			return null;
		}
	}

	/**
	 * 用无参构造函数创建并返回指定名字的类的一个新实例。
	 *
	 * @param className 要实例化的类名。
	 * @return 这个类的一个新实例。
	 */
	public static Object constructObject(String className) {
		try {
			// kamil - 针对 Java 1.4 的变通写法
			// 感谢 Ka-Hing Cheung 的建议。
			// 由 geels 修复以适配 Java 1.5
		        Class<?> cls = loadClass(className);
			Class<?>[] param_types = new Class[0];
			Constructor<?> cons = cls.getConstructor(param_types);
			Object[] params = new Object[0];
			return cons.newInstance(params);
		}
		catch (Throwable e) {
			Machine.terminate(e);
			return null;
		}
	}

	/**
	 * 验证指定的类继承或实现了指定的父类。
	 *
	 * @param cls 子类。
	 * @param superCls 祖先类。
	 */
	public static void checkDerivation(Class<?> cls, Class<?> superCls) {
		Lib.assertTrue(superCls.isAssignableFrom(cls));
	}

	/**
	 * 验证指定的类是 public 且非 abstract 的，并且存在一个具有指定签名的
	 * public 构造函数。
	 *
	 * @param cls 包含该构造函数的类。
	 * @param parameterTypes 参数类型列表。
	 */
	public static void checkConstructor(Class<?> cls, Class[] parameterTypes) {
		try {
			Lib.assertTrue(Modifier.isPublic(cls.getModifiers())
					&& !Modifier.isAbstract(cls.getModifiers()));
			Constructor<?> constructor = cls.getConstructor(parameterTypes);
			Lib.assertTrue(Modifier.isPublic(constructor.getModifiers()));
		}
		catch (Exception e) {
			Lib.assertNotReached();
		}
	}

	/**
	 * 验证指定的类是 public 的，并且存在一个具有指定名字和签名的非 static
	 * 方法，该方法是 public 的，返回指定的类型。
	 *
	 * @param cls 包含该非 static 方法的类。
	 * @param methodName 该非 static 方法的名字。
	 * @param parameterTypes 参数类型列表。
	 * @param returnType 要求的返回类型。
	 */
	public static void checkMethod(Class<?> cls, String methodName,
			Class[] parameterTypes, Class returnType) {
		try {
			Lib.assertTrue(Modifier.isPublic(cls.getModifiers()));
			Method method = cls.getMethod(methodName, parameterTypes);
			Lib.assertTrue(Modifier.isPublic(method.getModifiers())
					&& !Modifier.isStatic(method.getModifiers()));
			Lib.assertTrue(method.getReturnType() == returnType);
		}
		catch (Exception e) {
			Lib.assertNotReached();
		}
	}

	/**
	 * 验证指定的类是 public 的，并且存在一个具有指定名字和签名的 static 方法，
	 * 该方法是 public 的，返回指定的类型。
	 *
	 * @param cls 包含该 static 方法的类。
	 * @param methodName 该 static 方法的名字。
	 * @param parameterTypes 参数类型列表。
	 * @param returnType 要求的返回类型。
	 */
	public static void checkStaticMethod(Class<?> cls, String methodName,
			Class[] parameterTypes, Class returnType) {
		try {
			Lib.assertTrue(Modifier.isPublic(cls.getModifiers()));
			Method method = cls.getMethod(methodName, parameterTypes);
			Lib.assertTrue(Modifier.isPublic(method.getModifiers())
					&& Modifier.isStatic(method.getModifiers()));
			Lib.assertTrue(method.getReturnType() == returnType);
		}
		catch (Exception e) {
			Lib.assertNotReached();
		}
	}

	/**
	 * 验证指定的类是 public 的，并且存在一个具有指定名字和类型的非 static
	 * 字段，该字段是 public 的，且不是 final 的。
	 *
	 * @param cls 包含该字段的类。
	 * @param fieldName 字段名。
	 * @param fieldType 要求的类型。
	 */
	public static void checkField(Class cls, String fieldName, Class fieldType) {
		try {
			Lib.assertTrue(Modifier.isPublic(cls.getModifiers()));
			Field field = cls.getField(fieldName);
			Lib.assertTrue(field.getType() == fieldType);
			Lib.assertTrue(Modifier.isPublic(field.getModifiers())
					&& !Modifier.isStatic(field.getModifiers())
					&& !Modifier.isFinal(field.getModifiers()));
		}
		catch (Exception e) {
			Lib.assertNotReached();
		}
	}

	/**
	 * 验证指定的类是 public 的，并且存在一个具有指定名字和类型的 public
	 * static 字段。
	 *
	 * @param cls 包含该 static 字段的类。
	 * @param fieldName 该 static 字段的名字。
	 * @param fieldType 要求的类型。
	 */
	public static void checkStaticField(Class cls, String fieldName,
			Class fieldType) {
		try {
			Lib.assertTrue(Modifier.isPublic(cls.getModifiers()));
			Field field = cls.getField(fieldName);
			Lib.assertTrue(field.getType() == fieldType);
			Lib.assertTrue(Modifier.isPublic(field.getModifiers())
					&& Modifier.isStatic(field.getModifiers()));
		}
		catch (Exception e) {
			Lib.assertNotReached();
		}
	}

	/**
	 * 验证指定的类只拥有指定名字的那些字段。
	 *
	 * @param cls 包含这些字段的类。
	 * @param names 要验证的字段名列表。
	 */
        public static void checkFields(Class cls, List<String> names) {
		try {
			for (Field field : cls.getFields()) {
				if (!names.contains(field.getName())) {
					System.out.println("Unknown field " + field.getName() +
							   " found in " + cls.getSimpleName() + ".");
					System.out.println(cls.getSimpleName() +" is part of the machine simulation and should not be changed.");
					Lib.assertTrue(names.contains(field.getName()));
				}
			}
		} catch (Exception e) {
			Lib.assertNotReached();
		}
	}

}
