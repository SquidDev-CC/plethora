package org.squiddev.plethora.integration.tconstruct;

import com.google.common.collect.Maps;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.materials.HeadMaterialStats;

import javax.annotation.Nonnull;
import java.util.Map;

@IMetaProvider.Inject(value = HeadMaterialStats.class, modId = TConstruct.modID)
public class MetaHeadMaterialStats extends BasicMetaProvider<HeadMaterialStats> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull HeadMaterialStats stats) {
		Map<Object, Object> out = Maps.newHashMap();
		out.put("attack", stats.attack);
		out.put("durability", stats.durability);
		out.put("miningSpeed", stats.miningspeed);
		out.put("miningLevel", stats.harvestLevel);
		return out;
	}
}
