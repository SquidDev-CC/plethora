package org.squiddev.plethora.integration.forestry;

import forestry.api.apiculture.EnumBeeType;
import forestry.api.genetics.AlleleManager;
import forestry.api.genetics.IIndividual;
import forestry.apiculture.genetics.BeeDefinition;
import forestry.core.config.Constants;
import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.BaseMetaProvider;
import org.squiddev.plethora.api.method.IPartialContext;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

@Injects(Constants.MOD_ID)
public final class MetaItemGenetic extends BaseMetaProvider<ItemStack> {
	@Nonnull
	@Override
	public Map<String, ?> getMeta(@Nonnull IPartialContext<ItemStack> context) {
		ItemStack stack = context.getTarget();

		IIndividual individual = AlleleManager.alleleRegistry.getIndividual(stack);
		return individual != null
			? Collections.singletonMap("individual", context.makePartialChild(individual).getMeta())
			: Collections.emptyMap();
	}

	@Nonnull
	@Override
	public ItemStack getExample() {
		IIndividual individual = BeeDefinition.FOREST.getIndividual();
		individual.analyze();
		return BeeDefinition.FOREST.getGenome().getSpeciesRoot().getMemberStack(individual, EnumBeeType.PRINCESS);
	}
}
