package org.squiddev.plethora.integration.forestry;

import forestry.api.apiculture.IBee;
import forestry.core.config.Constants;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@IMetaProvider.Inject(value = IBee.class, modId = Constants.MOD_ID, namespace = "bee")
public class MetaBee extends BasicMetaProvider<IBee> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull IBee bee) {
		if (bee.isAnalyzed()) {
			Map<Object, Object> out = new HashMap<>(3);
			out.put("canSpawn", bee.canSpawn());
			out.put("generation", bee.getGeneration());
			out.put("pristine", bee.isNatural());
			return out;
		} else {
			return Collections.emptyMap();
		}
	}
}
