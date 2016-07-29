package org.squiddev.plethora.core;

import com.google.common.base.Preconditions;
import com.google.common.collect.MapMaker;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import org.squiddev.plethora.api.method.ICostHandler;

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * A basic {@link ICostHandler} implementation
 */
public final class CostHandler implements ICostHandler {
	@CapabilityInject(ICostHandler.class)
	public static Capability<ICostHandler> COST_HANDLER_CAPABILITY = null;

	/**
	 * Used to store all handlers.
	 * This uses the identity function
	 */
	private static final Map<Object, CostHandler> handlers = new MapMaker().weakKeys().makeMap();

	private double value;
	private final double regenRate;
	private final double limit;

	public CostHandler(double initial, double regenRate, double limit) {
		Preconditions.checkArgument(initial >= 0, "initial must be >= 0");
		Preconditions.checkArgument(regenRate > 0, "regenRate must be > 0");

		Preconditions.checkArgument(limit > 0, "limit must be > 0");
		Preconditions.checkArgument(limit > regenRate, "limit must be > regenRate");

		this.regenRate = regenRate;
		this.limit = limit;
	}

	public CostHandler() {
		this(100, 10, 100);
	}

	@Override
	public synchronized double get() {
		return value;
	}

	@Override
	public synchronized boolean consume(double amount) {
		Preconditions.checkArgument(amount > 0, "amount must be > 0");
		if (amount > value) return false;

		value -= amount;
		return true;
	}

	private synchronized void regen() {
		if (value < limit) {
			value = Math.min(limit, value + regenRate);
		}
	}

	public static ICostHandler get(Object owner) {
		synchronized (handlers) {
			CostHandler handler = handlers.get(owner);
			if (handler == null) {
				handler = new CostHandler();
				handlers.put(owner, handler);
			}

			return handler;
		}
	}

	public static void update() {
		synchronized (handlers) {
			for (CostHandler handler : handlers.values()) {
				handler.regen();
			}
		}
	}

	public static void register() {
		CapabilityManager.INSTANCE.register(ICostHandler.class, new Capability.IStorage<ICostHandler>() {
			@Override
			public NBTBase writeNBT(Capability<ICostHandler> capability, ICostHandler instance, EnumFacing side) {
				return null;
			}

			@Override
			public void readNBT(Capability<ICostHandler> capability, ICostHandler instance, EnumFacing side, NBTBase base) {
			}
		}, new Callable<ICostHandler>() {
			@Override
			public ICostHandler call() throws Exception {
				return new CostHandler();
			}
		});
	}
}
