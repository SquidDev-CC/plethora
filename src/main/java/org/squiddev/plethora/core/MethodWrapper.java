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

	protected static Object[] unwrap(MethodResult result, IComputerAccess access, ILuaContext context) throws LuaException, InterruptedException {
		if (result.isFinal()) {
			return result.getResult();
		} else {
			if (access == null) {
				// This is a horrible hack. As we don't have a computer access, we cannot queue events.
				// Instead we issue a task n number of times until everything has executed.
				Task task = new Task(result);
				while (!task.done()) {
					context.executeMainThreadTask(task);
				}
				return task.returnValue;
			} else {
				return TaskHandler.addTask(access, context, result.getCallback(), result.getDelay());
			}
		}
	}
}
