// PART OF THE MACHINE SIMULATION. DO NOT CHANGE.

package nachos.machine;

import java.io.EOFException;

/**
 * 一个支持读、写和定位（seek）的文件。
 */
public class OpenFile {
	/**
	 * 在指定的文件系统上，用指定的名字分配（创建）一个新的 <tt>OpenFile</tt>
	 * 对象。
	 *
	 * @param fileSystem 这个文件所属的文件系统。
	 * @param name 这个文件在该文件系统上的名字。
	 */
	public OpenFile(FileSystem fileSystem, String name) {
		this.fileSystem = fileSystem;
		this.name = name;
	}

	/**
	 * 分配（创建）一个新的、未命名的、不与任何文件系统关联的
	 * <tt>OpenFile</tt>。
	 */
	public OpenFile() {
		this(null, "unnamed");
	}

	/**
	 * 获取这个文件所属的文件系统。
	 *
	 * @return 这个文件所属的文件系统。
	 */
	public FileSystem getFileSystem() {
		return fileSystem;
	}

	/**
	 * 获取这个打开文件的名字。
	 *
	 * @return 这个打开文件的名字。
	 */
	public String getName() {
		return name;
	}

	/**
	 * 从指定位置开始读取这个文件，并返回成功读取的字节数。如果因为致命错误
	 * 一个字节都没有读到，返回 -1。
	 *
	 * @param pos 文件中开始读取的偏移量。
	 * @param buf 用来存放读到的字节的缓冲区。
	 * @param offset 缓冲区中开始存放字节的偏移量。
	 * @param length 要读取的字节数。
	 * @return 实际成功读取的字节数；失败时返回 -1。
	 */
	public int read(int pos, byte[] buf, int offset, int length) {
		return -1;
	}

	/**
	 * 从指定位置开始写这个文件，并返回成功写入的字节数。如果因为致命错误
	 * 一个字节都没有写入，返回 -1。
	 *
	 * @param pos 文件中开始写入的偏移量。
	 * @param buf 提供要写入字节的缓冲区。
	 * @param offset 缓冲区中开始取字节的偏移量。
	 * @param length 要写入的字节数。
	 * @return 实际成功写入的字节数；失败时返回 -1。
	 */
	public int write(int pos, byte[] buf, int offset, int length) {
		return -1;
	}

	/**
	 * 获取这个文件的长度。
	 *
	 * @return 这个文件的长度；如果这个文件没有长度，返回 -1。
	 */
	public int length() {
		return -1;
	}

	/**
	 * 关闭这个文件，并释放所有相关的系统资源。
	 */
	public void close() {
	}

	/**
	 * 设置当前文件指针的值。
	 */
	public void seek(int pos) {
	}

	/**
	 * 获取当前文件指针的值；如果这个文件没有文件指针，返回 -1。
	 */
	public int tell() {
		return -1;
	}

	/**
	 * 从当前文件指针处开始读取这个文件，并返回成功读取的字节数。文件指针会
	 * 前进相应的字节数。如果因为致命错误一个字节都无法读取，返回 -1。
	 *
	 * @param buf 用来存放读到的字节的缓冲区。
	 * @param offset 缓冲区中开始存放字节的偏移量。
	 * @param length 要读取的字节数。
	 * @return 实际成功读取的字节数；失败时返回 -1。
	 */
	public int read(byte[] buf, int offset, int length) {
		return -1;
	}

	/**
	 * 从当前文件指针处开始写这个文件，并返回成功写入的字节数。文件指针会
	 * 前进相应的字节数。如果因为致命错误一个字节都无法写入，返回 -1。
	 *
	 * @param buf 提供要写入字节的缓冲区。
	 * @param offset 缓冲区中开始取字节的偏移量。
	 * @param length 要写入的字节数。
	 * @return 实际成功写入的字节数；失败时返回 -1。
	 */
	public int write(byte[] buf, int offset, int length) {
		return -1;
	}

	private FileSystem fileSystem;

	private String name;
}
