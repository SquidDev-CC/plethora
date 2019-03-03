package org.squiddev.plethora.integration.tconstruct;

import com.google.common.collect.Maps;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.materials.IMaterialStats;

import javax.annotation.Nonnull;
import java.util.Map;

@Injects(TConstruct.modID)
public class MetaMaterialStats extends BasicMetaProvider<IMaterialStats> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull IMaterialStats stats) {
		Map<Object, Object> out = Maps.newHashMap();
		out.put("id", stats.getIdentifier());
		out.put("name", stats.getLocalizedName());
		return out;
	}
}
