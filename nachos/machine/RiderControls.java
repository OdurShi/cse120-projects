// PART OF THE MACHINE SIMULATION. DO NOT CHANGE.

package nachos.machine;

/**
 * 一组可以被 rider 控制程序使用的 controls（控制接口）。每个 rider 使用一个
 * 独立的 <tt>RiderControls</tt> 对象。
 */
public interface RiderControls {
	/**
	 * 返回 elevator bank 中的楼层数。如果这组电梯有 <i>n</i> 层楼，那么楼层
	 * 编号从 <i>0</i>（底层）到 <i>n - 1</i>（顶层）。
	 *
	 * @return 这组电梯的楼层数。
	 */
	public int getNumFloors();

	/**
	 * 返回 elevator bank 中的电梯数。如果这组电梯有 <i>n</i> 部电梯，那么电梯
	 * 编号从 <i>0</i> 到 <i>n - 1</i>。
	 *
	 * @return 这组电梯中的电梯数。
	 */
	public int getNumElevators();

	/**
	 * 设置 rider 的中断处理函数。当 rider 观察到一个事件时，会调用这个处理
	 * 函数。
	 *
	 * @param handler rider 的中断处理函数。
	 */
	public void setInterruptHandler(Runnable handler);

	/**
	 * 返回 rider 的当前位置。如果 rider 正在移动，返回值与准确位置的差距在
	 * 一层以内。
	 *
	 * @return rider 所在的楼层。
	 */
	public int getFloor();

	/**
	 * 返回一个数组，按顺序列出这个 rider 成功走出电梯的各个楼层。这个数组自然
	 * 不包括 rider 的起始楼层，也不包括 rider 在电梯里但没有出电梯的楼层。
	 *
	 * @return 一个列出这个 rider 去过的楼层的数组。
	 */
	public int[] getFloors();

	/**
	 * 返回指定电梯显示的方向，这个方向由
	 * <tt>ElevatorControls.setDirectionDisplay()</tt> 设置。
	 *
	 * @param elevator 要检查方向的电梯。
	 * @return 这部电梯显示的方向。
	 *
	 * @see nachos.machine.ElevatorControls#setDirectionDisplay
	 */
	public int getDirectionDisplay(int elevator);

	/**
	 * 按一个方向按钮。如果 <tt>up</tt> 为 <tt>true</tt>，调用
	 * <tt>pressUpButton()</tt>；否则调用 <tt>pressDownButton()</tt>。
	 *
	 * @param up 为 <tt>true</tt> 表示按向上按钮，为 <tt>false</tt> 表示按向下
	 * 按钮。
	 * @return <tt>pressUpButton()</tt> 或 <tt>pressDownButton()</tt> 的返回值。
	 */
	public boolean pressDirectionButton(boolean up);

	/**
	 * 按向上按钮。rider 不能在顶层，也不能在电梯里。如果有一部电梯和这个 rider
	 * 在同一层、门开着、并且显示要向上走，则什么都不做并返回 <tt>false</tt>。
	 *
	 * @return 如果按钮事件被发送给了电梯控制器，返回 <tt>true</tt>。
	 */
	public boolean pressUpButton();

	/**
	 * 按向下按钮。rider 不能在底层，也不能在电梯里。如果有一部电梯和这个 rider
	 * 在同一层、门开着、并且显示要向下走，则什么都不做并返回 <tt>false</tt>。
	 *
	 * @return 如果按钮事件被发送给了电梯控制器，返回 <tt>true</tt>。
	 */
	public boolean pressDownButton();

	/**
	 * 进入一部电梯。如果电梯的门没有在同一层打开，或者电梯已满，rider 就不能
	 * 进入。rider 不能已经在某部电梯里。
	 *
	 * @param elevator 要进入的电梯。
	 * @return 如果 rider 成功进入了电梯，返回 <tt>true</tt>。
	 */
	public boolean enterElevator(int elevator);

	/**
	 * 按一个楼层按钮。rider 必须在电梯里。如果电梯已经在 <tt>floor</tt> 层开着
	 * 门，则什么都不做并返回 <tt>false</tt>。
	 *
	 * @param floor 要按的按钮。
	 * @return 如果按钮事件被发送给了电梯控制器，返回 <tt>true</tt>。
	 */
	public boolean pressFloorButton(int floor);

	/**
	 * 走出电梯。如果电梯的门没有在请求的楼层打开，rider 就不能出电梯。rider
	 * 必须已经在某部电梯里。
	 *
	 * @param floor 要出电梯的楼层。
	 * @return 如果 rider 成功出了电梯，返回 <tt>true</tt>。
	 */
	public boolean exitElevator(int floor);

	/**
	 * rider 结束时调用。
	 */
	public void finish();

	/**
	 * 返回事件队列中的下一个事件。注意，发生 rider 中断时可能有多个待处理的
	 * 事件，所以应该反复调用这个方法，直到它返回 <tt>null</tt>。
	 *
	 * @return 下一个事件；如果当前没有更多待处理的事件，返回 <tt>null</tt>。
	 */
	public RiderEvent getNextEvent();
}
