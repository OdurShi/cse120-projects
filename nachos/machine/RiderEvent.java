// PART OF THE MACHINE SIMULATION. DO NOT CHANGE.

package nachos.machine;

/**
 * 影响 rider 软件的一个事件。如果 rider 在电梯外面，它只会收到与它在同一层
 * 的事件。如果 rider 在电梯里面，它只会收到与那部电梯相关的事件。
 */
public final class RiderEvent {
	public RiderEvent(int event, int floor, int elevator, int direction) {
		this.event = event;
		this.floor = floor;
		this.elevator = elevator;
		this.direction = direction;
	}

	/** 事件标识符。参见 <i>event*</i> 系列常量。 */
	public final int event;

	/** 与该事件相关的楼层；不适用时为 -1。 */
	public final int floor;

	/** 与该事件相关的电梯；不适用时为 -1。 */
	public final int elevator;

	/** 电梯的方向显示（不适用时为 neither）。 */
	public final int direction;

	/** 一部电梯的门已经打开。 */
	public static final int eventDoorsOpened = 0;

	/** 一部电梯的门开着，并且它的方向显示改变了。 */
	public static final int eventDirectionChanged = 1;

	/** 一部电梯的门已经关上。 */
	public static final int eventDoorsClosed = 2;
}
