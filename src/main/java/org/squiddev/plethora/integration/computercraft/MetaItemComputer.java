package org.squiddev.plethora.integration.computercraft;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.items.ComputerItemFactory;
import dan200.computercraft.shared.computer.items.IComputerItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;

/**
 * Provider for computer items;
 */
@IMetaProvider.Inject(value = ItemStack.class, namespace = "computer", modId = ComputerCraft.MOD_ID)
public class MetaItemComputer extends BasicMetaProvider<ItemStack> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull ItemStack stack) {
		Item item = stack.getItem();
		if (!(item instanceof IComputerItem)) return Collections.emptyMap();

		IComputerItem cItem = (IComputerItem) item;
		Map<Object, Object> data = Maps.newHashMap();

		int id = cItem.getComputerID(stack);
		if (id > 0) data.put("id", id);

		String label = cItem.getLabel(stack);
		if (!Strings.isNullOrEmpty(label)) data.put("label", label);
		data.put("family", cItem.getFamily(stack).toString());

		return data;
	}

	@Nullable
	@Override
	public ItemStack getExample() {
		return ComputerItemFactory.create(3, "My computer", ComputerFamily.Normal);
	}
}
