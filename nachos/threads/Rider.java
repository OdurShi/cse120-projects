package nachos.threads;

import nachos.machine.*;

/**
 * 单个 rider（乘客）。每个 rider 通过一个 <tt>RiderControls</tt> 实例来
 * 访问 elevator bank。
 */
public class Rider implements RiderInterface {
	/**
	 * 分配（创建）一个新的 rider。
	 */
	public Rider() {
	}

	/**
	 * 初始化这个 rider。rider 将通过 <i>controls</i> 访问 elevator bank，并按
	 * <i>stops</i> 的指定在不同楼层停靠。这个方法应在 rider 初始化完成后立即
	 * 返回，但必须等到中断处理函数设置好之后才能返回。这个方法返回后，rider
	 * 就会开始收到事件，而这可能发生在 <tt>run()</tt> 被调用之前。
	 *
	 * @param controls rider 访问 elevator bank 的接口。rider 不得尝试以
	 * <i>任何</i>其他方式访问 elevator bank。
	 * @param stops rider 应该停靠的楼层数组；见下文。
	 */
	public void initialize(RiderControls controls, int[] stops) {
	}

	/**
	 * 让 rider 使用所提供的 controls，完成构造时指定的停靠。rider 应该在
	 * <i>stops</i>（一个楼层号数组）中的每一层停靠，并且<i>只</i>在这些指定的
	 * 楼层停靠。
	 *
	 * <p>
	 * 例如，假设 rider 通过 <i>controls</i> 得知自己一开始在 1 楼，并且 stops
	 * 数组有两个元素：{ 0, 2 }。那么 rider 应该进电梯，在 0 楼出电梯，再进电梯，
	 * 然后在 2 楼出电梯，并在需要时按按钮。
	 *
	 * <p>
	 * 这个方法不应该返回，而应在 rider 结束时调用 <tt>controls.finish()</tt>。
	 */
	public void run() {
	}
}
