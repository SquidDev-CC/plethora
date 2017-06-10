package org.squiddev.plethora.core;

import com.google.common.base.Preconditions;
import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.pocket.IPocketUpgrade;
import dan200.computercraft.api.turtle.ITurtleUpgrade;
import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.Constants;
import org.squiddev.plethora.api.minecart.IMinecartUpgradeHandler;
import org.squiddev.plethora.api.module.IModuleHandler;
import org.squiddev.plethora.api.module.IModuleRegistry;

import javax.annotation.Nonnull;

final class ModuleRegistry implements IModuleRegistry {
	public static final ModuleRegistry instance = new ModuleRegistry();

	private ModuleRegistry() {
	}

	@Override
	public void registerTurtleUpgrade(@Nonnull ItemStack stack) {
		Preconditions.checkNotNull(stack, "stack cannot be null");
		registerTurtleUpgrade(stack, stack.getUnlocalizedName() + ".adjective");
	}

	@Override
	public void registerTurtleUpgrade(@Nonnull ItemStack stack, @Nonnull String adjective) {
		Preconditions.checkNotNull(stack, "stack cannot be null");
		Preconditions.checkNotNull(adjective, "adjective cannot be null");

		IModuleHandler handler = stack.getCapability(Constants.MODULE_HANDLER_CAPABILITY, null);
		if (handler == null) throw new NullPointerException("stack has no handler");

		ITurtleUpgrade upgrade = new TurtleUpgradeModule(stack, handler, adjective);
		ComputerCraftAPI.registerTurtleUpgrade(upgrade);
	}

	@Override
	public void registerTurtleUpgrade(@Nonnull ItemStack stack, @Nonnull IModuleHandler handler, @Nonnull String adjective) {
		Preconditions.checkNotNull(stack, "stack cannot be null");
		Preconditions.checkNotNull(stack, "handler cannot be null");
		Preconditions.checkNotNull(adjective, "adjective cannot be null");

		ITurtleUpgrade upgrade = new TurtleUpgradeModule(stack, handler, adjective);
		ComputerCraftAPI.registerTurtleUpgrade(upgrade);
	}

	@Override
	public void registerPocketUpgrade(@Nonnull ItemStack stack) {
		Preconditions.checkNotNull(stack, "stack cannot be null");
		registerPocketUpgrade(stack, stack.getUnlocalizedName() + ".adjective");
	}

	@Override
	public void registerPocketUpgrade(@Nonnull ItemStack stack, @Nonnull String adjective) {
		Preconditions.checkNotNull(stack, "stack cannot be null");
		Preconditions.checkNotNull(adjective, "adjective cannot be null");

		IModuleHandler handler = stack.getCapability(Constants.MODULE_HANDLER_CAPABILITY, null);
		if (handler == null) throw new NullPointerException("stack has no handler");

		IPocketUpgrade upgrade = new PocketUpgradeModule(stack, handler, adjective);
		ComputerCraftAPI.registerPocketUpgrade(upgrade);
	}

	@Override
	public void registerPocketUpgrade(@Nonnull ItemStack stack, @Nonnull IModuleHandler handler, @Nonnull String adjective) {
		Preconditions.checkNotNull(stack, "stack cannot be null");
		Preconditions.checkNotNull(stack, "handler cannot be null");
		Preconditions.checkNotNull(adjective, "adjective cannot be null");

		IPocketUpgrade upgrade = new PocketUpgradeModule(stack, handler, adjective);
		ComputerCraftAPI.registerPocketUpgrade(upgrade);
	}

	@Override
	public IMinecartUpgradeHandler toMinecartUpgrade(@Nonnull IModuleHandler handler) {
		Preconditions.checkNotNull(handler, "handler cannot be null");

		return new MinecartUpgradeModule(handler);
	}
}
