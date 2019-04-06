package org.squiddev.plethora.integration.forestry;

import forestry.api.genetics.IChromosomeType;
import forestry.api.genetics.IIndividual;
import forestry.core.config.Constants;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.BaseMetaProvider;
import org.squiddev.plethora.api.method.IPartialContext;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Injects(Constants.MOD_ID)
public final class MetaIndividual extends BaseMetaProvider<IIndividual> {
	@Nonnull
	@Override
	public Map<String, ?> getMeta(@Nonnull IPartialContext<IIndividual> context) {
		IIndividual individual = context.getTarget();
		Map<String, Object> out = new HashMap<>();
		out.put("id", individual.getIdent());
		out.put("analyzed", individual.isAnalyzed());

		if (individual.isAnalyzed()) {
			out.put("genome", context.makePartialChild(individual.getGenome()).getMeta());

			Map<String, Boolean> pureBred = new HashMap<>();
			for (IChromosomeType type : individual.getGenome().getSpeciesRoot().getKaryotype()) {
				pureBred.put(type.getName().toLowerCase(Locale.ENGLISH), individual.isPureBred(type));
			}

			out.put("pureBred", pureBred);
		}

		return out;
	}
}
