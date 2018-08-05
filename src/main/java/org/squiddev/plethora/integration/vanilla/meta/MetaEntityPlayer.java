package org.squiddev.plethora.integration.vanilla.meta;

import com.google.common.collect.Maps;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.util.FoodStats;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.utils.EntityPlayerDummy;
import org.squiddev.plethora.utils.WorldDummy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

@IMetaProvider.Inject(EntityPlayer.class)
public class MetaEntityPlayer extends BasicMetaProvider<EntityPlayer> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull EntityPlayer object) {
		Map<Object, Object> result = new HashMap<Object, Object>();

		FoodStats stats = object.getFoodStats();
		Map<String, Object> foodMap = Maps.newHashMap();
		result.put("food", foodMap);
		foodMap.put("hunger", stats.getFoodLevel());
		foodMap.put("saturation", stats.getSaturationLevel());
		foodMap.put("hungry", stats.needFood());

		PlayerCapabilities capabilities = object.capabilities;
		result.put("isFlying", capabilities.isFlying);
		result.put("allowFlying", capabilities.allowFlying);
		result.put("walkSpeed", capabilities.getWalkSpeed());
		result.put("flySpeed", capabilities.getFlySpeed());

		return result;
	}

	@Nullable
	@Override
	public EntityPlayer getExample() {
		return new EntityPlayerDummy(WorldDummy.INSTANCE);
	}
}
