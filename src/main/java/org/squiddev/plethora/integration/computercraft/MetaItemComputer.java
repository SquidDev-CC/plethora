package org.squiddev.plethora.integration.computercraft;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import dan200.computercraft.shared.computer.items.IComputerItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.api.meta.MetaProvider;

import java.util.Map;

/**
 * Provider for computer items;
 */
@MetaProvider(value = ItemStack.class, namespace = "computer")
public class MetaItemComputer implements IMetaProvider<ItemStack> {
	@Override
	public Map<Object, Object> getMeta(ItemStack stack) {
		Item item = stack.getItem();
		if (item instanceof IComputerItem) {
			Map<Object, Object> data = Maps.newHashMap();
			IComputerItem cItem = (IComputerItem) item;

			int id = cItem.getComputerID(stack);
			if (id > 0) data.put("id", id);

			String label = cItem.getLabel(stack);
			if (!Strings.isNullOrEmpty(label)) data.put("label", label);
			data.put("family", cItem.getFamily(stack).toString());

			return data;
		} else {
			return null;
		}
	}
}
