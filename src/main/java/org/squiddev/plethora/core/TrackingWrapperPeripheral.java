package org.squiddev.plethora.core;

import dan200.computercraft.api.peripheral.IComputerAccess;
import org.apache.commons.lang3.tuple.Pair;
import org.squiddev.cctweaks.core.utils.DebugLogger;
import org.squiddev.plethora.api.method.IMethod;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.core.executor.IExecutorFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link MethodWrapperPeripheral} which tracks peripheral changes.
 */
public class TrackingWrapperPeripheral extends MethodWrapperPeripheral {
	private final Set<IComputerAccess> accesses = Collections.newSetFromMap(new ConcurrentHashMap<IComputerAccess, Boolean>());

	public TrackingWrapperPeripheral(String name, Object owner, Pair<List<IMethod<?>>, List<IUnbakedContext<?>>> methods, IExecutorFactory factory) {
		super(name, owner, methods, factory);
	}

	@Override
	public void attach(IComputerAccess access) {
		super.attach(access);

		synchronized (accesses) {
			accesses.add(access);
		}
	}

	@Override
	public void detach(IComputerAccess access) {
		super.detach(access);

		synchronized (accesses) {
			accesses.remove(access);
		}
	}

	public void queueEvent(@Nonnull String name, @Nullable Object... args) {
		for (IComputerAccess access : accesses) {
			try {
				access.queueEvent(name, args);
			} catch (RuntimeException e) {
				DebugLogger.error("Cannot queue event on " + access, e);
				detach(access);
			}
		}
	}
}
