package org.squiddev.plethora.integration.tconstruct;

import com.google.common.collect.Maps;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.materials.ExtraMaterialStats;

import javax.annotation.Nonnull;
import java.util.Map;

@Injects(TConstruct.modID)
public class MetaExtraMaterialStats extends BasicMetaProvider<ExtraMaterialStats> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull ExtraMaterialStats object) {
		Map<Object, Object> out = Maps.newHashMap();
		out.put("extraDurability", object.extraDurability);
		return out;
	}
}
