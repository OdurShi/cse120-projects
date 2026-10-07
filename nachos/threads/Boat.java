package nachos.threads;

import nachos.ag.BoatGrader;

public class Boat {
	static BoatGrader bg;

	public static void selfTest() {
		BoatGrader b = new BoatGrader();

		System.out.println("\n ***Testing Boats with only 2 children***");
		begin(0, 2, b);

		// System.out.println("\n ***Testing Boats with 2 children, 1 adult***");
		// begin(1, 2, b);

		// System.out.println("\n ***Testing Boats with 3 children, 3 adults***");
		// begin(3, 3, b);
	}

	public static void begin(int adults, int children, BoatGrader b) {
		// 把外部生成的 autograder 存到一个类变量里，
		// 让 children 线程也能访问到它。
		bg = b;

		// 在这里初始化全局变量

		// 在这里创建线程。参见项目页面上链接的 Nachos for Java
		// Walkthrough 的 3.4 节。

		Runnable r = new Runnable() {
			public void run() {
				SampleItinerary();
			}
		};
		KThread t = new KThread(r);
		t.setName("Sample Boat Thread");
		t.fork();

	}

	static void AdultItinerary() {
		/*
		 * 这里是你放解答的地方。通过调用 BoatGrader 来表明它是同步的。
		 * 例如：bg.AdultRowToMolokai(); 表示一个 adult 已经把船划到了
		 * Molokai。
		 */
	}

	static void ChildItinerary() {
	}

	static void SampleItinerary() {
		// 请注意，这不是一个有效的解答（船装不下所有人）。另外也请注意，
		// 你不能让单个线程把解答算出来，然后照着它在 autograder 面前
		// 演一遍 —— 这样会被发现的。
		System.out
				.println("\n ***Everyone piles on the boat and goes to Molokai***");
		bg.AdultRowToMolokai();
		bg.ChildRideToMolokai();
		bg.AdultRideToMolokai();
		bg.ChildRideToMolokai();
	}

}
