package org.squiddev.plethora.core.capabilities;

import com.google.common.base.Preconditions;
import com.google.common.collect.MapMaker;
import dan200.computercraft.api.lua.LuaException;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.squiddev.plethora.api.method.ICostHandler;
import org.squiddev.plethora.api.method.MethodResult;
import org.squiddev.plethora.utils.DebugLogger;

import java.util.Map;
import java.util.concurrent.Callable;

import static org.squiddev.plethora.core.ConfigCore.CostSystem;

/**
 * A basic {@link ICostHandler} implementation. Every object registered with it is updated every tick.
 *
 * @see org.squiddev.plethora.core.PlethoraCore#onServerTick(TickEvent.ServerTickEvent)
 */
public final class DefaultCostHandler implements ICostHandler {
	public static final DefaultCostHandler EMPTY = new DefaultCostHandler(0, 0, 0, false, false);

	/**
	 * Used to store all handlers.
	 * This uses the identity function
	 */
	private static final Map<Object, DefaultCostHandler> handlers = new MapMaker().weakKeys().makeMap();

	private double value;
	private final double regenRate;
	private final double limit;
	private final boolean allowNegative;
	private final boolean allowAwait;

	public DefaultCostHandler(double initial, double regenRate, double limit, boolean allowNegative, boolean allowAwait) {
		Preconditions.checkArgument(initial >= 0, "initial must be >= 0");
		Preconditions.checkArgument(regenRate >= 0, "regenRate must be > 0");
		Preconditions.checkArgument(limit >= 0, "limit must be >= 0");

		this.regenRate = regenRate;
		this.limit = limit;
		this.allowNegative = allowNegative;
		this.allowAwait = allowAwait;
	}

	public DefaultCostHandler() {
		this(CostSystem.initial, CostSystem.regen, CostSystem.limit, CostSystem.allowNegative, CostSystem.awaitRegen);
	}

	@Override
	public synchronized double get() {
		return value;
	}

	@Override
	public synchronized boolean consume(double amount) {
		Preconditions.checkArgument(amount >= 0, "amount must be >= 0");

		if (allowNegative) {
			if (value <= 0) return false;
		} else {
			if (amount > value) return false;
		}

		value -= amount;
		return true;
	}

	@Override
	public MethodResult await(double amount, MethodResult next) throws LuaException {
		// First try to consume as normal, unwrapping if not possible.
		if (consume(amount)) return next;

		// Otherwise if we'll never be able to consume then give up.
		if ((!allowNegative && amount > limit) || !allowAwait) {
			throw new LuaException("Insufficient energy (requires " + amount + ", has " + value + ".");
		}

		return MethodResult.awaiting(() -> consume(amount), () -> next);
	}

	@Override
	public MethodResult await(double amount, Callable<MethodResult> next) throws LuaException {
		// First try to consume as normal, unwrapping if not possible.
		if (consume(amount)) {
			try {
				return next.call();
			} catch (LuaException e) {
				throw e;
			} catch (Throwable e) {
				DebugLogger.error("Unexpected error", e);
				throw new LuaException("Java Exception Thrown: " + e.toString());
			}
		}

		// Otherwise if we'll never be able to consume then give up.
		if ((!allowNegative && amount > limit) || !allowAwait) {
			throw new LuaException("Insufficient energy (requires " + amount + ", has " + value + ".");
		}

		return MethodResult.awaiting(() -> consume(amount), next);
	}

	private synchronized void regen() {
		if (value < limit) value = Math.min(limit, value + regenRate);
	}

	public static ICostHandler get(Object owner) {
		synchronized (handlers) {
			DefaultCostHandler handler = handlers.get(owner);
			if (handler == null) {
				handler = new DefaultCostHandler();
				handlers.put(owner, handler);
			}

			return handler;
		}
	}

	public static void update() {
		synchronized (handlers) {
			for (DefaultCostHandler handler : handlers.values()) {
				handler.regen();
			}
		}
	}

	public static void reset() {
		synchronized (handlers) {
			handlers.clear();
		}
	}
}
