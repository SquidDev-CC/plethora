package org.squiddev.plethora.integration.vanilla.meta;

import com.google.common.collect.Maps;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.FoodStats;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.MetaProvider;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

@MetaProvider(EntityPlayer.class)
public class MetaEntityPlayer extends BasicMetaProvider<EntityPlayer> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull EntityPlayer object) {
		FoodStats stats = object.getFoodStats();

		Map<Object, Object> food = Maps.newHashMap();
		food.put("hunger", stats.getFoodLevel());
		food.put("saturation", stats.getSaturationLevel());
		food.put("hungry", stats.needFood());

		return Collections.<Object, Object>singletonMap("food", food);
	}
}
