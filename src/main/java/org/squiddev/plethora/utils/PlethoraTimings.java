package org.squiddev.plethora.utils;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.core.apis.ComputerAccess;
import dan200.computercraft.core.apis.IAPIEnvironment;
import dan200.computercraft.core.computer.Computer;
import dan200.computercraft.core.tracking.Tracking;
import dan200.computercraft.shared.computer.core.ServerComputer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * A wrapper for CC:T's timing system, as we do not execute using CC's own threading system.
 *
 * This is horrible, as all things using reflection are wont to be.
 */
public final class PlethoraTimings {
	/**
	 * @see Tracking#addServerTiming(Computer, long)
	 */
	private static final Method addServerTiming;

	/**
	 * @see dan200.computercraft.core.apis.ComputerAccess#m_environment
	 */
	private static final Field computerAccessEnvironment;

	/**
	 * @see dan200.computercraft.shared.peripheral.modem.WiredModemPeripheral.RemotePeripheralWrapper
	 */
	private static final Class<?> remotePeripheralWrapper;

	/**
	 * @see dan200.computercraft.shared.peripheral.modem.WiredModemPeripheral.RemotePeripheralWrapper#m_computer
	 */
	private static final Field remotePeripheralWrapperComputer;

	private PlethoraTimings() {
	}

	static {
		Class<?> wrapper = null;
		Method serverTiming = null;
		Field environment = null, remoteComputer = null;

		try {
			serverTiming = Class.forName("dan200.computercraft.core.tracking.Tracking")
				.getDeclaredMethod("addServerTiming", Computer.class, long.class);

			environment = Class.forName("dan200.computercraft.core.apis.ComputerAccess")
				.getDeclaredField("m_environment");
			environment.setAccessible(true);

			wrapper = Class.forName("dan200.computercraft.shared.peripheral.modem.WiredModemPeripheral$RemotePeripheralWrapper");
			remoteComputer = wrapper.getDeclaredField("m_computer");
		} catch (ReflectiveOperationException ignored) {
		}

		addServerTiming = serverTiming;
		computerAccessEnvironment = environment;
		remotePeripheralWrapper = wrapper;
		remotePeripheralWrapperComputer = remoteComputer;
	}

	public static void addServerTiming(IComputerAccess computer, long time) {
		if (addServerTiming == null || computerAccessEnvironment == null || remotePeripheralWrapperComputer == null) {
			return;
		}

		try {
			addTimingImpl(computer, time);
		} catch (ReflectiveOperationException ignored) {
		} catch (RuntimeException e) {
			DebugLogger.error("Could not dump", e);
		}
	}

	/**
	 * Attempt to unwrap a {@link IComputerAccess} to a {@link Computer} and schedule a message.
	 *
	 * I'm sorry sorry, this shouldn't be how
	 * I have to do it. The absurdity of doing reflection against my own code is not lost on me.
	 *
	 * @param computer The computer to add timings for
	 * @param time     The time it took to execute
	 * @throws ReflectiveOperationException If this task could not be executed.
	 */
	private static void addTimingImpl(IComputerAccess computer, long time) throws ReflectiveOperationException {
		if (computer instanceof ComputerAccess) {
			IAPIEnvironment environment = (IAPIEnvironment) computerAccessEnvironment.get(computer);
			Tracking.addServerTiming(environment.getComputer(), time);
		} else if (remotePeripheralWrapper.isInstance(computer)) {
			IComputerAccess child = (IComputerAccess) remotePeripheralWrapperComputer.get(computer);
			if (child != null) addTimingImpl(child, time);
		} else {
			ServerComputer server = ComputerCraft.serverComputerRegistry.lookup(computer.getID());
			if (server != null) Tracking.addServerTiming(server.getAPIEnvironment().getComputer(), time);
		}
	}
}
