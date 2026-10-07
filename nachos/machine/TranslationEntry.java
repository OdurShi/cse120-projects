// PART OF THE MACHINE SIMULATION. DO NOT CHANGE.

package nachos.machine;

import nachos.machine.*;

/**
 * 一个虚拟页与一个物理页之间的单条翻译（映射）。
 */
public final class TranslationEntry {
	/**
	 * 分配（创建）一个新的无效翻译条目。
	 */
	public TranslationEntry() {
		valid = false;
	}

	/**
	 * 用指定的初始状态分配（创建）一个新的翻译条目。
	 *
	 * @param vpn 虚拟页号。
	 * @param ppn 物理页号。
	 * @param valid valid（有效）位。
	 * @param readOnly read-only（只读）位。
	 * @param used used（已使用）位。
	 * @param dirty dirty（已修改）位。
	 */
	public TranslationEntry(int vpn, int ppn, boolean valid, boolean readOnly,
			boolean used, boolean dirty) {
		this.vpn = vpn;
		this.ppn = ppn;
		this.valid = valid;
		this.readOnly = readOnly;
		this.used = used;
		this.dirty = dirty;
	}

	/**
	 * 分配（创建）一个新的翻译条目，复制一个已有条目的内容。
	 *
	 * @param entry 要复制的翻译条目。
	 */
	public TranslationEntry(TranslationEntry entry) {
		vpn = entry.vpn;
		ppn = entry.ppn;
		valid = entry.valid;
		readOnly = entry.readOnly;
		used = entry.used;
		dirty = entry.dirty;
	}

// PART OF THE MACHINE SIMULATION. DO NOT CHANGE.

	/** 虚拟页号。 */
	public int vpn;

	/** 物理页号。 */
	public int ppn;

	/**
	 * 如果这个标志为 <tt>false</tt>，这个翻译条目会被忽略。
	 */
	public boolean valid;

	/**
	 * 如果这个标志为 <tt>true</tt>，用户程序不允许修改这个虚拟页的内容。
	 */
	public boolean readOnly;

	/**
	 * 每当用户程序读或写这个页时，这个标志都会被设为 <tt>true</tt>。
	 */
	public boolean used;

	/**
	 * 每当用户程序写这个页时，这个标志都会被设为 <tt>true</tt>。
	 */
	public boolean dirty;

// PART OF THE MACHINE SIMULATION. DO NOT CHANGE.

}
