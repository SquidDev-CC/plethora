package org.squiddev.plethora.utils;

import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.core.computer.Computer;
import dan200.computercraft.core.computer.IComputerOwned;
import dan200.computercraft.core.tracking.Tracking;

/**
 * A wrapper for CC:T's timing system, as we do not execute using CC's own threading system.
 *
 * This is horrible, as all things using reflection are wont to be.
 */
public final class PlethoraTimings {
	private static final boolean enabled;

	private PlethoraTimings() {
	}

	static {
		boolean found = true;
		try {
			Class.forName("dan200.computercraft.core.tracking.Tracking");
			Class.forName("dan200.computercraft.core.computer.IComputerOwned");
		} catch (ReflectiveOperationException ignored) {
			found = false;
		}

		enabled = found;
	}

	public static void addServerTiming(IComputerAccess computer, long time) {
		if (enabled) addTimingImpl(computer, time);
	}

	private static void addTimingImpl(Object object, long time) {
		if (object instanceof IComputerOwned) {
			Computer computer = ((IComputerOwned) object).getComputer();
			if (computer != null) Tracking.addServerTiming(computer, time);
		}
	}
}
