package org.squiddev.plethora.integration.vanilla.meta;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.meta.BaseMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.api.method.IPartialContext;
import org.squiddev.plethora.utils.WorldDummy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

@IMetaProvider.Inject(value = EntityItem.class, namespace = "item")
public class MetaEntityItem extends BaseMetaProvider<EntityItem> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull IPartialContext<EntityItem> context) {
		return context.makePartialChild(context.getTarget().getItem()).getMeta();
	}

	@Nullable
	@Override
	public EntityItem getExample() {
		EntityItem item = new EntityItem(WorldDummy.INSTANCE);
		item.setItem(new ItemStack(Blocks.DIRT));
		return item;
	}
}
