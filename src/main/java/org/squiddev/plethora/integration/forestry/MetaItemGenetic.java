package org.squiddev.plethora.integration.forestry;

import forestry.api.genetics.AlleleManager;
import forestry.api.genetics.IIndividual;
import forestry.core.config.Constants;
import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.meta.BaseMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.api.method.IPartialContext;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

@IMetaProvider.Inject(value = ItemStack.class, modId = Constants.MOD_ID, namespace = "individual")
public class MetaItemGenetic extends BaseMetaProvider<ItemStack> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull IPartialContext<ItemStack> context) {
		ItemStack stack = context.getTarget();

		IIndividual individual = AlleleManager.alleleRegistry.getIndividual(stack);
		return individual != null
			? context.makePartialChild(individual).getMeta()
			: Collections.emptyMap();
	}
}
