// PART OF THE MACHINE SIMULATION. DO NOT CHANGE.

package nachos.machine;

/**
 * 一个允许用户创建、打开和删除文件的文件系统。
 */
public interface FileSystem {
	/**
	 * 原子地打开一个文件；如果文件还不存在，可以选择创建它。如果文件不存在且
	 * <tt>create</tt> 为 <tt>false</tt>，返回 <tt>null</tt>。如果文件不存在且
	 * <tt>create</tt> 为 <tt>true</tt>，创建一个长度为零的文件。如果文件已经
	 * 存在，就打开它，并且不对它做任何修改。
	 *
	 * @param name 要打开的文件名。
	 * @param create 为 <tt>true</tt> 表示文件不存在时创建它。
	 * @return 一个表示被打开文件的新实例的 <tt>OpenFile</tt>；如果文件无法
	 * 打开，返回 <tt>null</tt>。
	 */
	public OpenFile open(String name, boolean create);

	/**
	 * 原子地删除一个已存在的文件。文件被删除后，在用 <tt>open</tt> 重新创建
	 * 之前，它无法被打开。如果文件已经被打开，由具体实现决定这个文件是否仍然
	 * 可以访问，还是立即被删除。
	 *
	 * @param name 要删除的文件名。
	 * @return 如果文件被成功删除，返回 <tt>true</tt>。
	 */
	public boolean remove(String name);

	/**
	 * 返回文件系统中打开的文件数。
	 *
	 * @return 表示打开文件数的 <tt>int</tt>。
	 */
	public int getOpenCount();
}
