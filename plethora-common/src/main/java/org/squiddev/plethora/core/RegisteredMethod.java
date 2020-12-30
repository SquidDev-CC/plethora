package org.squiddev.plethora.core;

import dan200.computercraft.api.lua.LuaException;
import org.squiddev.plethora.api.method.IMethod;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.method.MethodResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public abstract class RegisteredMethod<T> extends RegisteredValue {
	private final Class<T> target;
	private int cost;

	public RegisteredMethod(@Nonnull String name, @Nullable String mod, @Nonnull Class<T> target) {
		super(name, mod);
		this.target = Objects.requireNonNull(target);
	}

	public abstract IMethod<T> method();

	@Nonnull
	public final Class<T> target() {
		return target;
	}

	public void build() {
		IMethod<T> method = method();
		String comment = method.getName() + ": " + method.getDocString();

		String id = name();
		if (id.indexOf('#') >= 0) {
			String oldId = id.replace('#', '$');
			int targetIdx = oldId.lastIndexOf('(');
			if (targetIdx >= 0) oldId = oldId.substring(0, targetIdx);
			ConfigCore.configuration.renameProperty("baseCosts", oldId, id);
		}

		cost = ConfigCore.configuration
			.get("baseCosts", id, 0, comment, 0, Integer.MAX_VALUE)
			.getInt();
	}

	MethodResult call(IUnbakedContext<T> context, Object[] args) throws LuaException {
		try {
			if (cost <= 0) return method().apply(context, args);

			// This is a little sub-optimal, as argument validation will be deferred until later.
			// However, we don't have much of a way round this as the method could technically
			// have side effects.
			return context.getCostHandler().await(cost, () -> method().apply(context, args));
		} catch (LuaException e) {
			throw e;
		} catch (Exception | LinkageError | VirtualMachineError e) {
			PlethoraCore.LOG.error("Unexpected error calling " + name(), e);
			throw new LuaException("Java Exception Thrown: " + e);
		}
	}

	public static final class Impl<T> extends RegisteredMethod<T> {
		private final IMethod<T> method;

		public Impl(@Nonnull String name, @Nullable String mod, @Nonnull Class<T> target, @Nonnull IMethod<T> method) {
			super(name, mod, target);
			this.method = Objects.requireNonNull(method);
		}

		@Override
		public IMethod<T> method() {
			return method;
		}
	}
}
