package org.squiddev.plethora.integration.forestry;

import com.google.common.collect.Maps;
import forestry.api.genetics.*;
import forestry.core.config.Constants;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.common.EnumPlantType;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;

import javax.annotation.Nonnull;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@IMetaProvider.Inject(value = IGenome.class, modId = Constants.MOD_ID)
public class MetaGenome extends BasicMetaProvider<IGenome> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull IGenome genome) {
		IChromosomeType[] types = genome.getSpeciesRoot().getKaryotype();

		Map<String, Object> active = Maps.newHashMapWithExpectedSize(types.length);
		Map<String, Object> inactive = Maps.newHashMapWithExpectedSize(types.length);

		for (IChromosomeType type : types) {
			active.put(type.getName(), getAllele(genome.getActiveAllele(type)));
			inactive.put(type.getName(), getAllele(genome.getInactiveAllele(type)));
		}

		Map<Object, Object> out = Maps.newHashMap();
		out.put("active", active);
		out.put("inactive", inactive);
		return out;
	}

	private Object getAllele(IAllele allele) {
		if (allele == null) {
			return "missing";
		} else if (allele instanceof IAlleleArea) {
			Vec3i vec = ((IAlleleArea) allele).getValue();

			Map<String, Integer> out = Maps.newHashMap();
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
		} else if (allele instanceof IAllelePlantType) {
			Set<EnumPlantType> types = ((IAllelePlantType) allele).getPlantTypes();


			int i = 0;
			Map<Integer, String> out = Maps.newHashMapWithExpectedSize(types.size());
			for (EnumPlantType type : types) {
				out.put(++i, type.toString().toLowerCase(Locale.ENGLISH));
			}
			return out;
		} else if (allele instanceof IAlleleSpecies) {
			IAlleleSpecies species = (IAlleleSpecies) allele;

			Map<String, Object> data = Maps.newHashMap();
			data.put("id", allele.getUID());
			data.put("displayName", allele.getName());
			data.put("authority", species.getAuthority());
			data.put("binomial", species.getBinomial());
			data.put("complexity", species.getComplexity());
			data.put("humidity", species.getHumidity().toString().toLowerCase(Locale.ENGLISH));
			data.put("temperature", species.getTemperature().toString().toLowerCase(Locale.ENGLISH));
			return data;
		} else if (allele instanceof IAlleleTolerance) {
			return ((IAlleleTolerance) allele).getValue().toString().toLowerCase(Locale.ENGLISH);
		} else {
			return allele.getUID();
		}
	}
}
