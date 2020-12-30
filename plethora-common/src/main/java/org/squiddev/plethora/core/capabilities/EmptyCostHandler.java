package org.squiddev.plethora.core.capabilities;

import dan200.computercraft.api.lua.LuaException;
import org.squiddev.plethora.api.method.ICostHandler;
import org.squiddev.plethora.api.method.MethodResult;

import java.util.concurrent.Callable;

public class EmptyCostHandler implements ICostHandler {
	public static final ICostHandler INSTANCE = new EmptyCostHandler();

	private EmptyCostHandler() {
	}

	@Override
	public double get() {
		return 0;
	}

	@Override
	public boolean consume(double amount) {
		if (amount < 0) throw new IllegalArgumentException("amount must be >= 0");
		return amount == 0;
	}

	@Override
	public MethodResult await(double amount, Callable<MethodResult> next) throws LuaException {
		throw new LuaException("Insufficient energy (requires " + amount + ", has 0).");
	}
}
