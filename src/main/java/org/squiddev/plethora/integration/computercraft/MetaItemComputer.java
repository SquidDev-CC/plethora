package org.squiddev.plethora.integration.computercraft;

import com.google.common.base.Strings;
import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.items.ComputerItemFactory;
import dan200.computercraft.shared.computer.items.IComputerItem;
import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.ItemStackMetaProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Provider for computer items;
 */
@Injects(ComputerCraft.MOD_ID)
public final class MetaItemComputer extends ItemStackMetaProvider<IComputerItem> {
	public MetaItemComputer() {
		super("computer", IComputerItem.class);
	}

	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull ItemStack stack, @Nonnull IComputerItem item) {
		Map<Object, Object> data = new HashMap<>();

		int id = item.getComputerID(stack);
		if (id >= 0) data.put("id", id);

		String label = item.getLabel(stack);
		if (!Strings.isNullOrEmpty(label)) data.put("label", label);
		data.put("family", item.getFamily(stack).toString());

		return data;
	}

	@Nullable
	@Override
	public ItemStack getExample() {
		return ComputerItemFactory.create(3, "My computer", ComputerFamily.Normal);
	}
}
