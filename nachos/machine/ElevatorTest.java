// PART OF THE MACHINE SIMULATION. DO NOT CHANGE.

package nachos.machine;

import nachos.security.*;
import nachos.threads.KThread;
import nachos.threads.Semaphore;

/**
 * 用一部电梯和一个 rider 测试 <tt>ElevatorBank</tt> 模块。
 */
public final class ElevatorTest {
	/**
	 * 分配（创建）一个新的 <tt>ElevatorTest</tt> 对象。
	 */
	public ElevatorTest() {
	}

	/**
	 * 在 <tt>Machine.bank()</tt> 上运行一次测试。
	 */
	public void run() {
		Machine.bank().init(1, 2, new ElevatorController());

		int[] stops = { 1 };

		Machine.bank().addRider(new Rider(), 0, stops);

		Machine.bank().run();
	}

	private class ElevatorController implements ElevatorControllerInterface {
		public void initialize(ElevatorControls controls) {
			this.controls = controls;

			eventWait = new Semaphore(0);

			controls.setInterruptHandler(new Runnable() {
				public void run() {
					interrupt();
				}
			});
		}

		public void run() {
			ElevatorEvent e;

			Lib.assertTrue(controls.getFloor(0) == 0);

			e = getNextEvent();
			Lib.assertTrue(e.event == ElevatorEvent.eventUpButtonPressed
					&& e.floor == 0);

			controls.setDirectionDisplay(0, dirUp);
			controls.openDoors(0);

			e = getNextEvent();
			Lib.assertTrue(e.event == ElevatorEvent.eventFloorButtonPressed
					&& e.floor == 1);

			controls.closeDoors(0);
			controls.moveTo(1, 0);

			e = getNextEvent();
			Lib.assertTrue(e.event == ElevatorEvent.eventElevatorArrived
					&& e.floor == 1 && e.elevator == 0);

			controls.openDoors(0);

			e = getNextEvent();
			Lib.assertTrue(e.event == ElevatorEvent.eventRidersDone);

			controls.finish();
			Lib.assertNotReached();
		}

		private void interrupt() {
			eventWait.V();
		}

		private ElevatorEvent getNextEvent() {
			ElevatorEvent event;
			while (true) {
				if ((event = controls.getNextEvent()) != null)
					break;

				eventWait.P();
			}
			return event;
		}

		private ElevatorControls controls;

		private Semaphore eventWait;
	}

	private class Rider implements RiderInterface {
		public void initialize(RiderControls controls, int[] stops) {
			this.controls = controls;
			Lib.assertTrue(stops.length == 1 && stops[0] == 1);

			eventWait = new Semaphore(0);

			controls.setInterruptHandler(new Runnable() {
				public void run() {
					interrupt();
				}
			});
		}

		public void run() {
			RiderEvent e;

			Lib.assertTrue(controls.getFloor() == 0);

			controls.pressUpButton();

			e = getNextEvent();
			Lib.assertTrue(e.event == RiderEvent.eventDoorsOpened
					&& e.floor == 0 && e.elevator == 0);
			Lib.assertTrue(controls.getDirectionDisplay(0) == dirUp);

			Lib.assertTrue(controls.enterElevator(0));
			controls.pressFloorButton(1);

			e = getNextEvent();
			Lib.assertTrue(e.event == RiderEvent.eventDoorsClosed
					&& e.floor == 0 && e.elevator == 0);

			e = getNextEvent();
			Lib.assertTrue(e.event == RiderEvent.eventDoorsOpened
					&& e.floor == 1 && e.elevator == 0);

			Lib.assertTrue(controls.exitElevator(1));

			controls.finish();
			Lib.assertNotReached();
		}

		private void interrupt() {
			eventWait.V();
		}

		private RiderEvent getNextEvent() {
			RiderEvent event;
			while (true) {
				if ((event = controls.getNextEvent()) != null)
					break;

				eventWait.P();
			}
			return event;
		}

		private RiderControls controls;

		private Semaphore eventWait;
	}
}
