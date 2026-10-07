// PART OF THE MACHINE SIMULATION. DO NOT CHANGE.

package nachos.machine;

/**
 * 一组可以被电梯控制器使用的 controls（控制接口）。
 */
public interface ElevatorControls {
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
	 * 设置电梯中断处理函数。当发生电梯事件，以及所有 rider 都到达目的地时，
	 * 会调用这个处理函数。
	 *
	 * @param handler 电梯中断处理函数。
	 */
	public void setInterruptHandler(Runnable handler);

	/**
	 * 打开某部电梯的门。
	 *
	 * @param elevator 要开门的电梯。
	 */
	public void openDoors(int elevator);

	/**
	 * 关上某部电梯的门。
	 *
	 * @param elevator 要关门的电梯。
	 */
	public void closeDoors(int elevator);

	/**
	 * 把电梯移动到另一层楼。电梯的门必须是关着的。如果电梯已经在移动，并且
	 * 因为已经经过或即将经过指定楼层而无法在那里安全停下，则操作失败并返回
	 * <tt>false</tt>。如果电梯已经停在指定楼层，返回 <tt>false</tt>。
	 *
	 * @param floor 要移动到的楼层。
	 * @param elevator 要移动的电梯。
	 * @return 如果电梯的目的地被改变了，返回 <tt>true</tt>。
	 */
	public boolean moveTo(int floor, int elevator);

	/**
	 * 返回电梯的当前位置。如果电梯正在移动，返回值与准确位置的差距在一层
	 * 以内。
	 *
	 * @param elevator 要定位的电梯。
	 * @return 电梯所在的楼层。
	 */
	public int getFloor(int elevator);

	/**
	 * 设置 elevator bank 在这部电梯的显示屏上显示哪个方向。<i>direction</i>
	 * 参数应该是 <tt>ElevatorBank</tt> 类中 <i>dir*</i> 系列常量之一。
	 *
	 * @param elevator 要设置方向显示的电梯。
	 * @param direction 要显示的方向（上、下或都不是）。
	 */
	public void setDirectionDisplay(int elevator, int direction);

	/**
	 * 电梯控制器结束时调用。
	 */
	public void finish();

	/**
	 * 返回事件队列中的下一个事件。注意，发生电梯中断时可能有多个待处理的事件，
	 * 所以应该反复调用这个方法，直到它返回 <tt>null</tt>。
	 *
	 * @return 下一个事件；如果当前没有更多待处理的事件，返回 <tt>null</tt>。
	 */
	public ElevatorEvent getNextEvent();
}
