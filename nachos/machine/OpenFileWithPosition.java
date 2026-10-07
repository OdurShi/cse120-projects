// PART OF THE MACHINE SIMULATION. DO NOT CHANGE.

package nachos.machine;

/**
 * 一个会维护当前文件位置的 <tt>OpenFile</tt>。
 */
public abstract class OpenFileWithPosition extends OpenFile {
	/**
	 * 在指定的文件系统上，用指定的名字分配（创建）一个新的
	 * <tt>OpenFileWithPosition</tt>。
	 *
	 * @param fileSystem 这个文件所属的文件系统。
	 * @param name 这个文件在该文件系统上的名字。
	 */
	public OpenFileWithPosition(FileSystem fileSystem, String name) {
		super(fileSystem, name);
	}

	/**
	 * 分配（创建）一个新的、未命名的、不与任何文件系统关联的
	 * <tt>OpenFileWithPosition</tt>。
	 */
	public OpenFileWithPosition() {
		super();
	}

	public void seek(int position) {
		this.position = position;
	}

	public int tell() {
		return position;
	}

	public int read(byte[] buf, int offset, int length) {
		int amount = read(position, buf, offset, length);
		if (amount == -1)
			return -1;

		position += amount;
		return amount;
	}

	public int write(byte[] buf, int offset, int length) {
		int amount = write(position, buf, offset, length);
		if (amount == -1)
			return -1;

		position += amount;
		return amount;
	}

	/**
	 * 文件指针的当前值。
	 */
	protected int position = 0;
}
