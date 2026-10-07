package nachos.ag;

public class BoatGrader {

	/**
	 * BoatGrader 由一组函数组成，调用它们可以表明你的解答是正确同步的。这个
	 * 版本只是把消息打印到标准输出，方便你观察。你不能提交这个文件，因为评分时
	 * 我们会使用我们自己的版本。
	 *
	 * 注意，这个文件包含了一个人从一个岛到另一个岛的所有可能方式。某个动作出现
	 * 在这个类中，并不代表它是个好主意，甚至不代表它是被允许的。
	 */

	/*
	 * 当一个 child 驾船从 Oahu 划到 Molokai 时，应该调用 ChildRowToMolokai
	 */
	public void ChildRowToMolokai() {
		System.out.println("**Child rowing to Molokai.");
	}

	/*
	 * 当一个 child 驾船从 Molokai 划到 Oahu 时，应该调用 ChildRowToOahu
	 */
	public void ChildRowToOahu() {
		System.out.println("**Child rowing to Oahu.");
	}

	/*
	 * 当一个不驾船的 child 在 Molokai 下船时，应该调用 ChildRideToMolokai
	 */
	public void ChildRideToMolokai() {
		System.out.println("**Child arrived on Molokai as a passenger.");
	}

	/*
	 * 当一个不驾船的 child 在 Oahu 下船时，应该调用 ChildRideToOahu
	 */
	public void ChildRideToOahu() {
		System.out.println("**Child arrived on Oahu as a passenger.");
	}

	/*
	 * 当一个 adult 驾船从 Oahu 划到 Molokai 时，应该调用 AdultRowToMolokai
	 */
	public void AdultRowToMolokai() {
		System.out.println("**Adult rowing to Molokai.");
	}

	/*
	 * 当一个 adult 驾船从 Molokai 划到 Oahu 时，应该调用 AdultRowToOahu
	 */
	public void AdultRowToOahu() {
		System.out.println("**Adult rowing to Oahu.");
	}

	/*
	 * 当一个不驾船的 adult 在 Molokai 下船时，应该调用 AdultRideToMolokai
	 */
	public void AdultRideToMolokai() {
		System.out.println("**Adult arrived on Molokai as a passenger.");
	}

	/*
	 * 当一个不驾船的 adult 在 Oahu 下船时，应该调用 AdultRideToOahu
	 */
	public void AdultRideToOahu() {
		System.out.println("**Adult arrived on Oahu as a passenger.");
	}
}
