package org.squiddev.plethora.core.executor;

import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.ListenableFuture;
import dan200.computercraft.api.lua.LuaException;
import org.squiddev.plethora.api.method.MethodResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.Callable;

/**
 * An implementation of {@link Task} which generates a listenable future.
 */
public class FutureTask extends Task {
	private final DirectFuture<Object[]> future;

	public FutureTask(Callable<MethodResult> callback, MethodResult.Resolver resolver) {
		super(callback, resolver);
		this.future = new DirectFuture<>();
	}

	@Override
	protected void finish(Object[] result) {
		future.set(result);
	}

	@Override
	protected void finish(@Nonnull LuaException e) {
		future.setException(e);
	}

	@Override
	public boolean update() {
		if (future.isCancelled()) {
			markFinished();
			return true;
		}

		return super.update();
	}

	public ListenableFuture<Object[]> getFuture() {
		return future;
	}

	private static final class DirectFuture<V> extends AbstractFuture<V> {
		@Override
		public boolean set(@Nullable V value) {
			return super.set(value);
		}

		@Override
		public boolean setException(Throwable throwable) {
			return super.setException(throwable);
		}
	}
}
