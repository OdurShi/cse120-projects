package nachos.threads;

import nachos.machine.*;

/**
 * 一个 elevator bank（电梯组）中所有电梯的控制器。控制器通过一个
 * <tt>ElevatorControls</tt> 实例来访问 elevator bank。
 */
public class ElevatorController implements ElevatorControllerInterface {
	/**
	 * 分配（创建）一个新的电梯控制器。
	 */
	public ElevatorController() {
	}

	/**
	 * 初始化这个电梯控制器。控制器将通过 <i>controls</i> 访问 elevator bank。
	 * 这个方法应在控制器初始化完成后立即返回，但必须等到中断处理函数设置好之后
	 * 才能返回。这个方法返回后，控制器就会开始收到事件，而这可能发生在
	 * <tt>run()</tt> 被调用之前。
	 *
	 * @param controls 控制器访问 elevator bank 的接口。控制器不得尝试以
	 * <i>任何</i>其他方式访问 elevator bank。
	 */
	public void initialize(ElevatorControls controls) {
	}

	/**
	 * 让控制器使用所提供的 controls 来接收并处理 rider（乘客）的请求。这个方法
	 * 不应该返回，而应在控制器结束时调用 <tt>controls.finish()</tt>。
	 */
	public void run() {
	}
}
