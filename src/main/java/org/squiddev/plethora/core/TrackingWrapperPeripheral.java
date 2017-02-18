package org.squiddev.plethora.core;

import dan200.computercraft.api.peripheral.IComputerAccess;
import org.apache.commons.lang3.tuple.Pair;
import org.squiddev.cctweaks.core.utils.DebugLogger;
import org.squiddev.plethora.api.IAttachable;
import org.squiddev.plethora.api.method.IMethod;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.core.executor.IExecutorFactory;
import org.squiddev.plethora.core.executor.TrackingExecutor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link MethodWrapperPeripheral} which trackes
 */
public class TrackingWrapperPeripheral extends MethodWrapperPeripheral {
	private final TrackingExecutor trackingFactory;
	private final Collection<IAttachable> attachments;
	private final Set<IComputerAccess> accesses = Collections.newSetFromMap(new ConcurrentHashMap<IComputerAccess, Boolean>());

	public TrackingWrapperPeripheral(String name, Object owner, Pair<List<IMethod<?>>, List<IUnbakedContext<?>>> methods, IExecutorFactory factory, Collection<IAttachable> attachments) {
		super(name, owner, methods, new TrackingExecutor(factory));
		this.attachments = attachments;
		this.trackingFactory = (TrackingExecutor)getExecutorFactory();
	}

	@Override
	public void attach(IComputerAccess access) {
		super.attach(access);

		int count;
		synchronized (accesses) {
			count = accesses.size();
			accesses.add(access);
		}

		if (count == 0) {
			trackingFactory.setAttached(true);
			for (IAttachable attachable : attachments) {
				attachable.attach();
			}
		}
	}

	@Override
	public void detach(IComputerAccess access) {
		super.detach(access);

		int count;
		synchronized (accesses) {
			accesses.remove(access);
			count = accesses.size();
		}

		if (count == 0) {
			trackingFactory.setAttached(false);
			for (IAttachable attachable : attachments) {
				attachable.detach();
			}
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
