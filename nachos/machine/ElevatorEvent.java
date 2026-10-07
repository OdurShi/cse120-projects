// PART OF THE MACHINE SIMULATION. DO NOT CHANGE.

package nachos.machine;

/**
 * 影响电梯软件的一个事件。
 */
public final class ElevatorEvent {
	public ElevatorEvent(int event, int floor, int elevator) {
		this.event = event;
		this.floor = floor;
		this.elevator = elevator;
	}

	/** 事件标识符。参见 <i>event*</i> 系列常量。 */
	public final int event;

	/** 与该事件相关的楼层；不适用时为 -1。 */
	public final int floor;

	/** 与该事件相关的电梯；不适用时为 -1。 */
	public final int elevator;

	/** 有人按了向上按钮。 */
	public static final int eventUpButtonPressed = 0;

	/** 有人按了向下按钮。 */
	public static final int eventDownButtonPressed = 1;

	/** 有人在电梯内按了楼层按钮。 */
	public static final int eventFloorButtonPressed = 2;

	/** 一部电梯已经到达并停在了它的目的楼层。 */
	public static final int eventElevatorArrived = 3;

	/** 所有 rider 都已结束；电梯控制器应该终止。 */
	public static final int eventRidersDone = 4;
}
