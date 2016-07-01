package org.squiddev.plethora.integration.vanilla.meta;

import com.google.common.collect.Maps;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.FoodStats;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.api.meta.MetaProvider;

import javax.annotation.Nonnull;
import java.util.Map;

@MetaProvider(value = EntityPlayer.class, namespace = "food")
public class MetaPlayer implements IMetaProvider<EntityPlayer> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull EntityPlayer object) {
		FoodStats stats = object.getFoodStats();

		Map<Object, Object> out = Maps.newHashMap();
		out.put("hunger", stats.getFoodLevel());
		out.put("saturation", stats.getSaturationLevel());
		out.put("hungry", stats.needFood());

		return out;
	}
}
