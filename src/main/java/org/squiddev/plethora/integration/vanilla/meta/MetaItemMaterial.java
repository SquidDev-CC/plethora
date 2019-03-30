package org.squiddev.plethora.integration.vanilla.meta;

import net.minecraft.init.Items;
import net.minecraft.item.*;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.BasicMetaProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;

/**
 * Displays tool's material
 */
@Injects
public final class MetaItemMaterial extends BasicMetaProvider<ItemStack> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull ItemStack stack) {
		String name = getName(stack);
		return name != null ? Collections.singletonMap("material", name) : Collections.emptyMap();

	}

	private static String getName(@Nonnull ItemStack stack) {
		Item item = stack.getItem();
		if (item instanceof ItemTool) {
			return ((ItemTool) item).getToolMaterialName();
		} else if (item instanceof ItemSword) {
			return ((ItemSword) item).getToolMaterialName();
		} else if (item instanceof ItemArmor) {
			return ((ItemArmor) item).getArmorMaterial().toString();
		} else {
			return null;
		}
	}

	@Nullable
	@Override
	public ItemStack getExample() {
		return new ItemStack(Items.DIAMOND_PICKAXE);
	}
}
