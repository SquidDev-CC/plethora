package org.squiddev.plethora.integration.computercraft;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.turtle.ITurtleUpgrade;
import dan200.computercraft.api.turtle.TurtleSide;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.turtle.items.ITurtleItem;
import dan200.computercraft.shared.turtle.items.TurtleItemFactory;
import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.ItemStackMetaProvider;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

@Injects(ComputerCraft.MOD_ID)
public final class MetaItemTurtle extends ItemStackMetaProvider<ITurtleItem> {
	public MetaItemTurtle() {
		super("turtle", ITurtleItem.class);
	}

	@Nonnull
	@Override
	public Map<String, ?> getMeta(@Nonnull ItemStack object, @Nonnull ITurtleItem turtle) {
		Map<String, Object> out = new HashMap<>();

		int colour = turtle.getColour(object);
		if (colour != -1) {
			out.put("color", colour);
			out.put("colour", colour); // For those who can spell :p
		}
		out.put("fuel", turtle.getFuelLevel(object));

		out.put("left", getUpgrade(turtle.getUpgrade(object, TurtleSide.Left)));
		out.put("right", getUpgrade(turtle.getUpgrade(object, TurtleSide.Right)));

		return out;
	}

	static Map<String, String> getUpgrade(ITurtleUpgrade upgrade) {
		if (upgrade == null) return null;

		Map<String, String> out = new HashMap<>(2);
		out.put("id", upgrade.getUpgradeID().toString());
		out.put("adjective", upgrade.getUnlocalisedAdjective());
		out.put("type", upgrade.getType().toString());

		return out;
	}

	@Nonnull
	@Override
	public ItemStack getExample() {
		return TurtleItemFactory.create(
			3, "My turtle", 0x00FF00, ComputerFamily.Normal,
			ComputerCraft.TurtleUpgrades.advancedModem, null, 3000, null
		);
	}
}
