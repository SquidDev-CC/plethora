package org.squiddev.plethora.integration.forestry;

import forestry.api.apiculture.IBee;
import forestry.apiculture.genetics.BeeDefinition;
import forestry.core.config.Constants;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.BasicMetaProvider;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Injects(Constants.MOD_ID)
public final class MetaBee extends BasicMetaProvider<IBee> {
	@Nonnull
	@Override
	public Map<String, ?> getMeta(@Nonnull IBee bee) {
		if (!bee.isAnalyzed()) return Collections.emptyMap();

		Map<String, Object> out = new HashMap<>(3);
		out.put("canSpawn", bee.canSpawn());
		out.put("generation", bee.getGeneration());
		out.put("pristine", bee.isNatural());
		return Collections.singletonMap("bee", out);
	}

	@Nonnull
	@Override
	public IBee getExample() {
		IBee bee = BeeDefinition.FOREST.getIndividual();
		bee.analyze();
		return bee;
	}
}
