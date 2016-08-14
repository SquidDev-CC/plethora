package org.squiddev.plethora.core;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.ILuaTask;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import org.squiddev.plethora.api.method.IMethod;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.method.MethodResult;
import org.squiddev.plethora.api.reference.IReference;
import org.squiddev.plethora.utils.DebugLogger;

import java.util.List;
import java.util.concurrent.Callable;

import static org.squiddev.plethora.api.reference.Reference.id;

/**
 * Handles the base for various Lua objects
 *
 * @see dan200.computercraft.api.peripheral.IPeripheral
 * @see dan200.computercraft.api.lua.ILuaObject
 */
public class MethodWrapper {
	private final List<IMethod<?>> methods;
	private final List<IUnbakedContext<?>> contexts;

	private final String[] names;

	public MethodWrapper(List<IMethod<?>> methods, List<IUnbakedContext<?>> contexts) {
		this.contexts = contexts;
		this.methods = methods;

		String[] names = this.names = new String[methods.size()];
		for (int i = 0; i < names.length; i++) {
			names[i] = methods.get(i).getName();
		}
	}

	public String[] getMethodNames() {
		return names;
	}

	public IMethod<?> getMethod(int i) {
		return methods.get(i);
	}

	public IUnbakedContext<?> getContext(int i) {
		return contexts.get(i);
	}

	protected IReference<?>[] getReferences(IComputerAccess access, ILuaContext context) {
		return new IReference[]{id(access), id(context)};
	}

	/**
	 * Check if the methods are the same on both.
	 *
	 * This isn't a perfect test for equality: it doesn't check contexts are the same
	 *
	 * @param other The item to match against
	 * @return Whether the methods are equal
	 */
	public boolean equalMethods(MethodWrapper other) {
		// Do the easy version: check they are the same items with same order.
		if (methods.equals(other.methods)) return true;

		if (methods.size() != other.methods.size()) return false;

		/*
			Doubt this is possible but better safe than sorry?
			We *could* make a hash set but this path is not going to be visited much, also not sure if there would be
			any efficiency gain: the method count is pretty small.
		  */
		for (IMethod method : methods) {
			if (!other.methods.contains(method)) return false;
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	protected static MethodResult doCallMethod(IMethod method, IUnbakedContext context, Object[] args) throws LuaException {
		try {
			return method.apply(context, args);
		} catch (LuaException e) {
			throw e;
		} catch (Throwable e) {
			DebugLogger.error("Unexpected error calling " + method.getName(), e);
			throw new LuaException("Java Exception Thrown: " + e.toString());
		}
	}

	private static class Task implements ILuaTask {
		public Object[] returnValue;
		private int remaining;
		private Callable<MethodResult> callback;

		public Task(MethodResult result) {
			setup(result);
		}

		private void setup(MethodResult result) {
			if (result.isFinal()) {
				returnValue = result.getResult();
			} else {
				remaining = result.getDelay();
				callback = result.getCallback();
			}
		}

		@Override
		public Object[] execute() throws LuaException {
			if (remaining == 0) {
				remaining--;
				try {
					setup(callback.call());
				} catch (LuaException e) {
					throw e;
				} catch (Throwable e) {
					DebugLogger.error("Unexpected error", e);
					throw new LuaException("Java Exception Thrown: " + e.toString());
				}
			} else {
				remaining--;
			}
			return null;
		}

		public boolean done() {
			return remaining < 0;
		}
	}

	protected static Object[] unwrap(MethodResult result, ILuaContext context) throws LuaException, InterruptedException {
		if (result.isFinal()) {
			return result.getResult();
		} else {
			/**
			 * This is a horrible hack. Ideally we'd be able to have our own task manager. However
			 * {@link IComputerAccess#queueEvent(String, Object[])} requires being attached to a computer which we may
			 * no longer be. This results in this throwing an exception, so we never receive the event we are waiting
			 * for, resulting in the computer hanging until the terminate event is fired.
			 *
			 * To avoid this we queue the task each tick until the delay has elapsed. Most of the time this doesn't
			 * matter as there will be a zero tick delay.
			 */
			Task task = new Task(result);
			while (!task.done()) {
				context.executeMainThreadTask(task);
			}
			return task.returnValue;
		}
	}
}
