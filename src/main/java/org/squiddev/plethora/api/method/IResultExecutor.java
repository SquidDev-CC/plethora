package org.squiddev.plethora.api.method;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.ILuaTask;
import dan200.computercraft.api.lua.LuaException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A task which evaluates a {@link MethodResult}.
 *
 * This should immediately return {@link MethodResult#getResult()} if the result is final,
 * otherwise defer for the specified delay and wait til that task has finished.
 *
 * By default this uses {@link ILuaContext#executeMainThreadTask(ILuaTask)} to execute the task,
 * but some instances will execute on the TileEntity's tick instead
 */
public interface IResultExecutor {
	/**
	 * @param result  The method result to evaluate
	 * @param context The context to evaluate under
	 * @return The final result
	 * @throws LuaException         If something went wrong
	 * @throws InterruptedException If the code was interrupted
	 */
	@Nullable
	Object[] execute(@Nonnull MethodResult result, @Nonnull ILuaContext context) throws LuaException, InterruptedException;
}
