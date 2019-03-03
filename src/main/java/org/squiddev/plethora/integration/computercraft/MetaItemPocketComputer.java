package org.squiddev.plethora.integration.computercraft;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.pocket.IPocketUpgrade;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.pocket.items.ItemPocketComputer;
import dan200.computercraft.shared.pocket.items.PocketComputerItemFactory;
import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.ItemStackMetaProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

@Injects(ComputerCraft.MOD_ID)
public final class MetaItemPocketComputer extends ItemStackMetaProvider<ItemPocketComputer> {
	public MetaItemPocketComputer() {
		super("pocket", ItemPocketComputer.class);
	}

	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull ItemStack object, @Nonnull ItemPocketComputer pocket) {
		Map<Object, Object> out = new HashMap<>(2);

		int colour = pocket.getColour(object);
		if (colour != -1) {
			out.put("color", colour);
			out.put("colour", colour); // For those who can spell :p
		}

		out.put("back", getUpgrade(pocket.getUpgrade(object)));

		return out;
	}

	private static Map<String, String> getUpgrade(IPocketUpgrade upgrade) {
		if (upgrade == null) return null;

		Map<String, String> out = new HashMap<>(2);
		out.put("id", upgrade.getUpgradeID().toString());
		out.put("adjective", upgrade.getUnlocalisedAdjective());

		return out;
	}

	@Nullable
	@Override
	public ItemStack getExample() {
		return PocketComputerItemFactory.create(
			2, "My pocket computer", 0xFF0000, ComputerFamily.Advanced,
			ComputerCraft.PocketUpgrades.pocketSpeaker
		);
	}
}
