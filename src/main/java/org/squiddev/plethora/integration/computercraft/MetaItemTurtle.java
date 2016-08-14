package org.squiddev.plethora.integration.computercraft;

import com.google.common.collect.Maps;
import dan200.computercraft.api.turtle.ITurtleUpgrade;
import dan200.computercraft.api.turtle.TurtleSide;
import dan200.computercraft.shared.computer.items.IComputerItem;
import dan200.computercraft.shared.turtle.items.ITurtleItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

@IMetaProvider.Inject(value = ItemStack.class, modId = "ComputerCraft", namespace = "turtle")
public class MetaItemTurtle extends BasicMetaProvider<ItemStack> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull ItemStack object) {
		Item item = object.getItem();
		if (!(item instanceof IComputerItem)) return Collections.emptyMap();

		ITurtleItem turtle = (ITurtleItem) item;
		Map<Object, Object> out = Maps.newHashMap();

		out.put("color", turtle.getColour(object).toString());
		out.put("colour", turtle.getColour(object).toString()); // For those who can spell :p
		out.put("fuel", turtle.getFuelLevel(object));

		out.put("left", getUpgrade(turtle.getUpgrade(object, TurtleSide.Left)));
		out.put("right", getUpgrade(turtle.getUpgrade(object, TurtleSide.Right)));

		return out;
	}

	public static Map<String, String> getUpgrade(ITurtleUpgrade upgrade) {
		if (upgrade == null) return null;

		Map<String, String> out = Maps.newHashMap();
		out.put("id", upgrade.getUpgradeID().toString());
		out.put("adjective", upgrade.getUnlocalisedAdjective());
		out.put("type", upgrade.getType().toString());

		return out;
	}
}
