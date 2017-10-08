package org.squiddev.plethora.core;

import com.google.common.base.Preconditions;
import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.pocket.IPocketUpgrade;
import dan200.computercraft.api.turtle.ITurtleUpgrade;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.pocket.items.PocketComputerItemFactory;
import dan200.computercraft.shared.turtle.items.TurtleItemFactory;
import dan200.computercraft.shared.util.ImpostorRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;
import org.squiddev.plethora.api.Constants;
import org.squiddev.plethora.api.minecart.IMinecartUpgradeHandler;
import org.squiddev.plethora.api.module.IModuleHandler;
import org.squiddev.plethora.api.module.IModuleRegistry;
import org.squiddev.plethora.gameplay.Plethora;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

final class ModuleRegistry implements IModuleRegistry {
	public static final ModuleRegistry instance = new ModuleRegistry();

	private final List<IPocketUpgrade> pocketUpgrades = new ArrayList<IPocketUpgrade>();
	private final List<ITurtleUpgrade> turtleUpgrades = new ArrayList<ITurtleUpgrade>();

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

		registerTurtleUpgrade(stack, handler, adjective);
	}

	@Override
	public void registerTurtleUpgrade(@Nonnull ItemStack stack, @Nonnull IModuleHandler handler, @Nonnull String adjective) {
		Preconditions.checkNotNull(stack, "stack cannot be null");
		Preconditions.checkNotNull(stack, "handler cannot be null");
		Preconditions.checkNotNull(adjective, "adjective cannot be null");

		ITurtleUpgrade upgrade = new TurtleUpgradeModule(stack, handler, adjective);
		ComputerCraftAPI.registerTurtleUpgrade(upgrade);
		turtleUpgrades.add(upgrade);
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

		registerPocketUpgrade(stack, handler, adjective);
	}

	@Override
	public void registerPocketUpgrade(@Nonnull ItemStack stack, @Nonnull IModuleHandler handler, @Nonnull String adjective) {
		Preconditions.checkNotNull(stack, "stack cannot be null");
		Preconditions.checkNotNull(stack, "handler cannot be null");
		Preconditions.checkNotNull(adjective, "adjective cannot be null");

		IPocketUpgrade upgrade = new PocketUpgradeModule(stack, handler, adjective);
		ComputerCraftAPI.registerPocketUpgrade(upgrade);
		pocketUpgrades.add(upgrade);
	}

	@Override
	public IMinecartUpgradeHandler toMinecartUpgrade(@Nonnull IModuleHandler handler) {
		Preconditions.checkNotNull(handler, "handler cannot be null");

		return new MinecartUpgradeModule(handler);
	}

	void addRecipes(IForgeRegistry<IRecipe>  registry) {
		for (ITurtleUpgrade upgrade : turtleUpgrades) {
			registry.register(new ImpostorRecipe(
				PlethoraCore.ID + ":turtle_upgrade", 2, 1,
				new ItemStack[]{
					upgrade.getCraftingItem(),
					TurtleItemFactory.create(-1, null, -1, ComputerFamily.Normal, null, null, -1, null)
				},
				TurtleItemFactory.create(-1, null, -1, ComputerFamily.Normal, null, upgrade, -1, null)
			).setRegistryName(new ResourceLocation(PlethoraCore.ID, "turtle_" + upgrade.getUpgradeID().toString().replace(':', '_'))));
		}

		for (IPocketUpgrade upgrade : pocketUpgrades) {
			registry.register(new ImpostorRecipe(
				PlethoraCore.ID + ":pocket_upgrade", 1, 2,
				new ItemStack[]{
					upgrade.getCraftingItem(),
					PocketComputerItemFactory.create(-1, null, -1, ComputerFamily.Normal, null)
				},
				PocketComputerItemFactory.create(-1, null, -1, ComputerFamily.Normal, upgrade)
			).setRegistryName(new ResourceLocation(PlethoraCore.ID, "pocket_" + upgrade.getUpgradeID().toString().replace(':', '_'))));
		}
	}
}
