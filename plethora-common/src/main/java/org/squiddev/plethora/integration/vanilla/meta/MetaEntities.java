package org.squiddev.plethora.integration.vanilla.meta;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.BaseMetaProvider;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.api.method.IPartialContext;
import org.squiddev.plethora.utils.WorldDummy;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Injects
public final class MetaEntities {
	public static final IMetaProvider<EntityItem> ENTITY_ITEM = new BaseMetaProvider<EntityItem>(
		"Provides the stack of a dropped item"
	) {
		@Nonnull
		@Override
		public Map<String, ?> getMeta(@Nonnull IPartialContext<EntityItem> context) {
			return Collections.singletonMap("item", context.makePartialChild(context.getTarget().getItem()).getMeta());
		}

		@Nonnull
		@Override
		public EntityItem getExample() {
			EntityItem item = new EntityItem(WorldDummy.INSTANCE);
			item.setItem(new ItemStack(Blocks.DIRT));
			return item;
		}
	};

	public static final IMetaProvider<EntitySheep> ENTITY_SHEEP = new BasicMetaProvider<EntitySheep>(
		"Provides the wool colour of the sheep."
	) {
		@Nonnull
		@Override
		public Map<String, ?> getMeta(@Nonnull EntitySheep sheep) {
			Map<String, Object> meta = new HashMap<>(2);
			meta.put("woolColour", sheep.getFleeceColor().getTranslationKey());
			meta.put("woolColor", sheep.getFleeceColor().getTranslationKey());
			return meta;
		}

		@Nonnull
		@Override
		public EntitySheep getExample() {
			EntitySheep sheep = new EntitySheep(WorldDummy.INSTANCE);
			sheep.setFleeceColor(EnumDyeColor.GREEN);
			return sheep;
		}
	};
}
