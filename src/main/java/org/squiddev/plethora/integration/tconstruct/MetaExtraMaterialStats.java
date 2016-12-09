package org.squiddev.plethora.integration.tconstruct;

import com.google.common.collect.Maps;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.materials.ExtraMaterialStats;

import javax.annotation.Nonnull;
import java.util.Map;

@IMetaProvider.Inject(value = ExtraMaterialStats.class, modId = TConstruct.modID)
public class MetaExtraMaterialStats extends BasicMetaProvider<ExtraMaterialStats> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull ExtraMaterialStats object) {
		Map<Object, Object> out = Maps.newHashMap();
		out.put("extraDurability", object.extraDurability);
		return out;
	}
}
