package org.squiddev.plethora.integration.tconstruct;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.ItemStackContextMetaProvider;
import org.squiddev.plethora.api.method.IPartialContext;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.materials.IMaterialStats;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.tinkering.IMaterialItem;
import slimeknights.tconstruct.library.tinkering.PartMaterialType;
import slimeknights.tconstruct.library.traits.ITrait;
import slimeknights.tconstruct.tools.TinkerTools;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

@Injects(TConstruct.modID)
public final class MetaMaterial extends ItemStackContextMetaProvider<IMaterialItem> {
	public MetaMaterial() {
		super("toolMaterial", IMaterialItem.class);
	}

	@Nonnull
	@Override
	public Map<String, ?> getMeta(@Nonnull IPartialContext<ItemStack> context, IMaterialItem materialItem) {
		ItemStack stack = context.getTarget();
		Item item = stack.getItem();
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

	@Nonnull
	@Override
	public ItemStack getExample() {
		return new ItemStack(TinkerTools.pickHead);
	}
}
