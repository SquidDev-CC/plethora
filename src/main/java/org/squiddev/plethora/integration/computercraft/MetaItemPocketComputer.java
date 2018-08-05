package org.squiddev.plethora.integration.computercraft;

import com.google.common.collect.Maps;
import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.pocket.IPocketUpgrade;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.pocket.items.ItemPocketComputer;
import dan200.computercraft.shared.pocket.items.PocketComputerItemFactory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;

@IMetaProvider.Inject(value = ItemStack.class, modId = ComputerCraft.MOD_ID, namespace = "turtle")
public class MetaItemPocketComputer extends BasicMetaProvider<ItemStack> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull ItemStack object) {
		Item item = object.getItem();
		if (!(item instanceof ItemPocketComputer)) return Collections.emptyMap();

		ItemPocketComputer pocket = (ItemPocketComputer) item;
		Map<Object, Object> out = Maps.newHashMap();

		int colour = pocket.getColour(object);
		if (colour != -1) {
			out.put("color", colour);
			out.put("colour", colour); // For those who can spell :p
		}

		out.put("back", getUpgrade(pocket.getUpgrade(object)));

		return out;
	}

	public static Map<String, String> getUpgrade(IPocketUpgrade upgrade) {
		if (upgrade == null) return null;

		Map<String, String> out = Maps.newHashMap();
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
