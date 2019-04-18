package org.squiddev.plethora.integration.forestry;

import forestry.api.lepidopterology.IButterfly;
import forestry.core.config.Constants;
import forestry.lepidopterology.genetics.ButterflyDefinition;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.BasicMetaProvider;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

@Injects(Constants.MOD_ID)
public final class MetaButterfly extends BasicMetaProvider<IButterfly> {
	@Nonnull
	@Override
	public Map<String, ?> getMeta(@Nonnull IButterfly butterfly) {
		if (!butterfly.isAnalyzed()) return Collections.emptyMap();

		return Collections.singletonMap("butterfly", Collections.singletonMap("size", butterfly.getSize()));
	}

	@Nonnull
	@Override
	public IButterfly getExample() {
		IButterfly butterfly = ButterflyDefinition.Batesia.getIndividual();
		butterfly.analyze();
		return butterfly;
	}
}
