package org.squiddev.plethora.integration.tconstruct;

import com.google.common.collect.Maps;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.materials.HandleMaterialStats;

import javax.annotation.Nonnull;
import java.util.Map;

@Injects(TConstruct.modID)
public class MetaHandleMaterialStats extends BasicMetaProvider<HandleMaterialStats> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull HandleMaterialStats object) {
		Map<Object, Object> out = Maps.newHashMap();
		out.put("durability", object.durability);
		out.put("modifier", object.modifier);
		return out;
	}
}
