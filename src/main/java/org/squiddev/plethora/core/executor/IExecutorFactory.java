package org.squiddev.plethora.core.executor;

import dan200.computercraft.api.peripheral.IComputerAccess;
import org.squiddev.plethora.api.method.IResultExecutor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Creates a result executor
 */
public interface IExecutorFactory {
	@Nonnull
	IResultExecutor createExecutor(@Nullable IComputerAccess access);
}
