package org.squiddev.plethora.integration.appliedenergistics;

import appeng.api.implementations.ICraftingPatternItem;
import appeng.core.AppEng;
import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.meta.BaseMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.api.method.IPartialContext;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

@IMetaProvider.Inject(value = ItemStack.class, modId = AppEng.MOD_ID, namespace = "pattern")
public class MetaItemPattern extends BaseMetaProvider<ItemStack> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull IPartialContext<ItemStack> context) {
		ItemStack stack = context.getTarget();

		if (!(stack.getItem() instanceof ICraftingPatternItem)) return Collections.emptyMap();

		IWorldLocation position = context.getContext(IWorldLocation.class);
		if (position != null) {
			ICraftingPatternItem pattern = (ICraftingPatternItem) stack.getItem();
			return context.makePartialChild(pattern.getPatternForItem(stack, position.getWorld())).getMeta();
		} else {
			return Collections.emptyMap();
		}
	}
}
