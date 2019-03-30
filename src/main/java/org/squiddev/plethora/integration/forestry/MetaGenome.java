package org.squiddev.plethora.integration.forestry;

import forestry.api.genetics.*;
import forestry.core.config.Constants;
import net.minecraft.util.math.Vec3i;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.BasicMetaProvider;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Injects(Constants.MOD_ID)
public final class MetaGenome extends BasicMetaProvider<IGenome> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull IGenome genome) {
		IChromosomeType[] types = genome.getSpeciesRoot().getKaryotype();

		Map<String, Object> active = new HashMap<>(types.length);
		Map<String, Object> inactive = new HashMap<>(types.length);

		for (IChromosomeType type : types) {
			active.put(type.getName(), getAlleleMeta(genome.getActiveAllele(type)));
			inactive.put(type.getName(), getAlleleMeta(genome.getInactiveAllele(type)));
		}

		Map<Object, Object> out = new HashMap<>(2);
		out.put("active", active);
		out.put("inactive", inactive);
		return out;
	}

	public static Object getAlleleMeta(IAllele allele) {
		if (allele == null) {
			return null;
		} else if (allele instanceof IAlleleArea) {
			Vec3i vec = ((IAlleleArea) allele).getValue();

			Map<String, Integer> out = new HashMap<>();
			out.put("width", vec.getX());
			out.put("height", vec.getY());
			out.put("depth", vec.getZ());
			return out;
		} else if (allele instanceof IAlleleBoolean) {
			return ((IAlleleBoolean) allele).getValue();
		} else if (allele instanceof IAlleleFloat) {
			return ((IAlleleFloat) allele).getValue();
		} else if (allele instanceof IAlleleFlowers) {
			return ((IAlleleFlowers) allele).getProvider().getFlowerType();
		} else if (allele instanceof IAlleleInteger) {
			return ((IAlleleInteger) allele).getValue();
		} else if (allele instanceof IAlleleSpecies) {
			IAlleleSpecies species = (IAlleleSpecies) allele;

			Map<String, Object> data = new HashMap<>();
			data.put("id", allele.getUID());
			data.put("displayName", allele.getAlleleName());
			data.put("authority", species.getAuthority());
			data.put("binomial", species.getBinomial());
			data.put("complexity", species.getComplexity());
			data.put("humidity", species.getHumidity().getName());
			data.put("temperature", species.getTemperature().getName());

			return data;
		} else if (allele instanceof IAlleleTolerance) {
			return ((IAlleleTolerance) allele).getValue().toString().toLowerCase(Locale.ENGLISH);
		} else {
			return allele.getUID();
		}
	}
}
