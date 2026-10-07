// PART OF THE MACHINE SIMULATION. DO NOT CHANGE.

package nachos.machine;

import java.util.HashMap;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.io.StreamTokenizer;

/**
 * 提供访问 Nachos 配置的函数。
 */
public final class Config {
	/**
	 * 从指定的文件中加载配置信息。必须在安装 Nachos security manager 之前
	 * 调用。
	 *
	 * @param fileName 包含要使用的配置的文件名。
	 */
	public static void load(String fileName) {
		System.out.println(" config");

		Lib.assertTrue(!loaded);
		loaded = true;

		configFile = fileName;

		try {
			config = new HashMap<String, String>();

			File file = new File(configFile);
			Reader reader = new FileReader(file);
			StreamTokenizer s = new StreamTokenizer(reader);

			s.resetSyntax();
			s.whitespaceChars(0x00, 0x20);
			s.wordChars(0x21, 0xFF);
			s.eolIsSignificant(true);
			s.commentChar('#');
			s.quoteChar('"');

			int line = 1;

			s.nextToken();

			while (true) {
				if (s.ttype == StreamTokenizer.TT_EOF)
					break;

				if (s.ttype == StreamTokenizer.TT_EOL) {
					line++;
					s.nextToken();
					continue;
				}

				if (s.ttype != StreamTokenizer.TT_WORD)
					loadError(line);

				String key = s.sval;

				if (s.nextToken() != StreamTokenizer.TT_WORD
						|| !s.sval.equals("="))
					loadError(line);

				if (s.nextToken() != StreamTokenizer.TT_WORD && s.ttype != '"')
					loadError(line);

				String value = s.sval;

				// 忽略第一个字符串之后的所有内容
				while (s.nextToken() != StreamTokenizer.TT_EOL
						&& s.ttype != StreamTokenizer.TT_EOF)
					;

				if (config.get(key) != null)
					loadError(line);

				config.put(key, value);
				line++;
			}
		}
		catch (Throwable e) {
			System.err.println("Error loading " + configFile);
			System.exit(1);
		}
	}

	private static void loadError(int line) {
		System.err.println("Error in " + configFile + " line " + line);
		System.exit(1);
	}

	private static void configError(String message) {
		System.err.println("");
		System.err.println("Error in " + configFile + ": " + message);
		System.exit(1);
	}

	/**
	 * 获取 <tt>nachos.conf</tt> 中某个 key 的值。
	 *
	 * @param key 要查找的 key。
	 * @return 指定 key 的值；如果不存在，返回 <tt>null</tt>。
	 */
	public static String getString(String key) {
		return (String) config.get(key);
	}

	/**
	 * 获取 <tt>nachos.conf</tt> 中某个 key 的值；如果这个 key 不存在，返回
	 * 指定的默认值。
	 *
	 * @param key 要查找的 key。
	 * @param defaultValue key 不存在时要返回的值。
	 * @return 指定 key 的值；如果不存在，返回 <tt>defaultValue</tt>。
	 */
	public static String getString(String key, String defaultValue) {
		String result = getString(key);

		if (result == null)
			return defaultValue;

		return result;
	}

	private static Integer requestInteger(String key) {
		try {
			String value = getString(key);
			if (value == null)
				return null;

			return Integer.valueOf(value);
		}
		catch (NumberFormatException e) {
			configError(key + " should be an integer");

			Lib.assertNotReached();
			return null;
		}
	}

	/**
	 * 获取 <tt>nachos.conf</tt> 中某个整数 key 的值。
	 *
	 * @param key 要查找的 key。
	 * @return 指定 key 的值。
	 */
	public static int getInteger(String key) {
		Integer result = requestInteger(key);

		if (result == null)
			configError("missing int " + key);

		return result.intValue();
	}

	/**
	 * 获取 <tt>nachos.conf</tt> 中某个整数 key 的值；如果这个 key 不存在，
	 * 返回指定的默认值。
	 *
	 * @param key 要查找的 key。
	 * @param defaultValue key 不存在时要返回的值。
	 * @return 指定 key 的值；如果 key 不存在，返回 <tt>defaultValue</tt>。
	 */
	public static int getInteger(String key, int defaultValue) {
		Integer result = requestInteger(key);

		if (result == null)
			return defaultValue;

		return result.intValue();
	}

	private static Double requestDouble(String key) {
		try {
			String value = getString(key);
			if (value == null)
				return null;

			return Double.valueOf(value);
		}
		catch (NumberFormatException e) {
			configError(key + " should be a double");

			Lib.assertNotReached();
			return null;
		}
	}

	/**
	 * 获取 <tt>nachos.conf</tt> 中某个 double key 的值。
	 *
	 * @param key 要查找的 key。
	 * @return 指定 key 的值。
	 */
	public static double getDouble(String key) {
		Double result = requestDouble(key);

		if (result == null)
			configError("missing double " + key);

		return result.doubleValue();
	}

	/**
	 * 获取 <tt>nachos.conf</tt> 中某个 double key 的值；如果这个 key 不存在，
	 * 返回指定的默认值。
	 *
	 * @param key 要查找的 key。
	 * @param defaultValue key 不存在时要返回的值。
	 * @return 指定 key 的值；如果 key 不存在，返回 <tt>defaultValue</tt>。
	 */
	public static double getDouble(String key, double defaultValue) {
		Double result = requestDouble(key);

		if (result == null)
			return defaultValue;

		return result.doubleValue();
	}

	private static Boolean requestBoolean(String key) {
		String value = getString(key);

		if (value == null)
			return null;

		if (value.equals("1") || value.toLowerCase().equals("true")) {
			return Boolean.TRUE;
		}
		else if (value.equals("0") || value.toLowerCase().equals("false")) {
			return Boolean.FALSE;
		}
		else {
			configError(key + " should be a boolean");

			Lib.assertNotReached();
			return null;
		}
	}

	/**
	 * 获取 <tt>nachos.conf</tt> 中某个 boolean key 的值。
	 *
	 * @param key 要查找的 key。
	 * @return 指定 key 的值。
	 */
	public static boolean getBoolean(String key) {
		Boolean result = requestBoolean(key);

		if (result == null)
			configError("missing boolean " + key);

		return result.booleanValue();
	}

	/**
	 * 获取 <tt>nachos.conf</tt> 中某个 boolean key 的值；如果这个 key 不存在，
	 * 返回指定的默认值。
	 *
	 * @param key 要查找的 key。
	 * @param defaultValue key 不存在时要返回的值。
	 * @return 指定 key 的值；如果 key 不存在，返回 <tt>defaultValue</tt>。
	 */
	public static boolean getBoolean(String key, boolean defaultValue) {
		Boolean result = requestBoolean(key);

		if (result == null)
			return defaultValue;

		return result.booleanValue();
	}

	private static boolean loaded = false;

	private static String configFile;

	private static HashMap<String, String> config;
}
