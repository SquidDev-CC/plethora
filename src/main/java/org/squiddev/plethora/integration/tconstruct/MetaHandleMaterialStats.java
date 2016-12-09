package org.squiddev.plethora.integration.tconstruct;

import com.google.common.collect.Maps;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.materials.HandleMaterialStats;

import javax.annotation.Nonnull;
import java.util.Map;

@IMetaProvider.Inject(value = HandleMaterialStats.class, modId = TConstruct.modID)
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
