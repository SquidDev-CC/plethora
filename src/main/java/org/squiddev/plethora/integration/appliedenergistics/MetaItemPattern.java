package org.squiddev.plethora.integration.appliedenergistics;

import appeng.api.implementations.ICraftingPatternItem;
import appeng.core.AppEng;
import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.ItemStackContextMetaProvider;
import org.squiddev.plethora.api.method.ContextKeys;
import org.squiddev.plethora.api.method.IPartialContext;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

@Injects(AppEng.MOD_ID)
public final class MetaItemPattern extends ItemStackContextMetaProvider<ICraftingPatternItem> {
	public MetaItemPattern() {
		super("pattern", ICraftingPatternItem.class);
	}

	@Nonnull
	@Override
	public Map<String, ?> getMeta(@Nonnull IPartialContext<ItemStack> context, @Nonnull ICraftingPatternItem pattern) {
		IWorldLocation position = context.getContext(ContextKeys.ORIGIN, IWorldLocation.class);
		return position != null
			? context.makePartialChild(pattern.getPatternForItem(context.getTarget(), position.getWorld())).getMeta()
			: Collections.emptyMap();
	}
}
