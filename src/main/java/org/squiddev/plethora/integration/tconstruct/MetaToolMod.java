package org.squiddev.plethora.integration.tconstruct;

import com.google.common.collect.Maps;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.modifiers.IToolMod;

import javax.annotation.Nonnull;
import java.util.Map;

@IMetaProvider.Inject(value = IToolMod.class, modId = TConstruct.modID)
public class MetaToolMod extends BasicMetaProvider<IToolMod> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull IToolMod mod) {
		Map<Object, Object> out = Maps.newHashMap();
		out.put("id", mod.getIdentifier());
		out.put("name", mod.getLocalizedName());
		return out;
	}
}
