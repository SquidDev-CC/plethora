package org.squiddev.plethora.integration.tconstruct;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.meta.BaseMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.api.method.IPartialContext;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.materials.IMaterialStats;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.tinkering.IMaterialItem;
import slimeknights.tconstruct.library.tinkering.PartMaterialType;
import slimeknights.tconstruct.library.traits.ITrait;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@IMetaProvider.Inject(value = ItemStack.class, namespace = "toolMaterial", modId = TConstruct.modID)
public class MetaMaterial extends BaseMetaProvider<ItemStack> {
	@Nonnull
	@Override
	public Map<String, ?> getMeta(@Nonnull IPartialContext<ItemStack> context) {
		ItemStack stack = context.getTarget();
		Item item = stack.getItem();
		if (!(item instanceof IMaterialItem)) return Collections.emptyMap();

		IMaterialItem materialItem = (IMaterialItem) item;
		Map<String, Object> out = new HashMap<>();

		Material material = materialItem.getMaterial(stack);
		out.put("id", material.getIdentifier());
		out.put("name", material.getLocalizedName());

		{
			PartMaterialType pmt = context.getContext(PartMaterialType.class);

			// Gather a list of all stats. We don't provide what they do, but this should be enough.
			int i = 0;
			Map<Integer, Object> stats = new HashMap<>();
			for (IMaterialStats stat : material.getAllStats()) {
				if (pmt == null || pmt.usesStat(stat.getIdentifier())) {
					stats.put(++i, context.makePartialChild(stat).getMeta());
				}
			}

			out.put("stats", stats);
		}

		{
			// Gather a list of all traits. We don't provide what they do, but this should be enough.
			int i = 0;
			Map<Integer, Object> traits = new HashMap<>();
			for (ITrait trait : material.getAllTraits()) {
				if (!trait.isHidden()) {
					traits.put(++i, context.makePartialChild(trait).getMeta());
				}
			}

			out.put("traits", traits);
		}

		return out;
	}
}
