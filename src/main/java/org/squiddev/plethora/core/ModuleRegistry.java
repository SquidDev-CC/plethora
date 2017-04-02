package org.squiddev.plethora.core;

import com.google.common.base.Preconditions;
import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.turtle.ITurtleUpgrade;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;
import org.squiddev.cctweaks.CCTweaks;
import org.squiddev.cctweaks.api.CCTweaksAPI;
import org.squiddev.cctweaks.api.pocket.IPocketUpgrade;
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

		if (Loader.isModLoaded(CCTweaks.ID)) doRegisterPocket(stack, handler, adjective);
	}

	@Override
	public void registerPocketUpgrade(@Nonnull ItemStack stack, @Nonnull IModuleHandler handler, @Nonnull String adjective) {
		Preconditions.checkNotNull(stack, "stack cannot be null");
		Preconditions.checkNotNull(stack, "handler cannot be null");
		Preconditions.checkNotNull(adjective, "adjective cannot be null");

		if (Loader.isModLoaded(CCTweaks.ID)) doRegisterPocket(stack, handler, adjective);
	}

	@Optional.Method(modid = CCTweaks.ID)
	private void doRegisterPocket(ItemStack stack, IModuleHandler handler, String adjective) {
		IPocketUpgrade upgrade = new PocketUpgradeModule(stack, handler, adjective);
		CCTweaksAPI.instance().pocketRegistry().addUpgrade(upgrade);
	}

	@Override
	public IMinecartUpgradeHandler toMinecartUpgrade(@Nonnull IModuleHandler handler) {
		Preconditions.checkNotNull(handler, "handler cannot be null");

		return new MinecartUpgradeModule(handler);
	}
}
