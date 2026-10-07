// PART OF THE MACHINE SIMULATION. DO NOT CHANGE.

package nachos.machine;

/**
 * 一个以字节数组为后备存储的只读 <tt>OpenFile</tt>。
 */
public class ArrayFile extends OpenFileWithPosition {
	/**
	 * 分配（创建）一个新的 <tt>ArrayFile</tt>。
	 *
	 * @param array 作为这个文件后备存储的数组。
	 */
	public ArrayFile(byte[] array) {
		this.array = array;
	}

	public int length() {
		return array.length;
	}

	public void close() {
		array = null;
	}

	public int read(int position, byte[] buf, int offset, int length) {
		Lib.assertTrue(offset >= 0 && length >= 0
				&& offset + length <= buf.length);

		if (position < 0 || position >= array.length)
			return 0;

		length = Math.min(length, array.length - position);
		System.arraycopy(array, position, buf, offset, length);

		return length;
	}

	public int write(int position, byte[] buf, int offset, int length) {
		return 0;
	}

	private byte[] array;
}
