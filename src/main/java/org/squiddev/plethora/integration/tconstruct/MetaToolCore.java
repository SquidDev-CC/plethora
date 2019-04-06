package org.squiddev.plethora.integration.tconstruct;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.squiddev.plethora.api.meta.BaseMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.api.method.IPartialContext;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.modifiers.IModifier;
import slimeknights.tconstruct.library.modifiers.ModifierNBT;
import slimeknights.tconstruct.library.tinkering.Category;
import slimeknights.tconstruct.library.tinkering.PartMaterialType;
import slimeknights.tconstruct.library.tools.IToolPart;
import slimeknights.tconstruct.library.tools.ToolCore;
import slimeknights.tconstruct.library.utils.TagUtil;
import slimeknights.tconstruct.library.utils.TinkerUtil;
import slimeknights.tconstruct.library.utils.ToolHelper;

import javax.annotation.Nonnull;
import java.util.*;

@IMetaProvider.Inject(value = ItemStack.class, modId = TConstruct.modID, namespace = "tool")
public class MetaToolCore extends BaseMetaProvider<ItemStack> {
	@Nonnull
	@Override
	public Map<String, ?> getMeta(@Nonnull IPartialContext<ItemStack> context) {
		ItemStack stack = context.getTarget();
		if (!(stack.getItem() instanceof ToolCore)) return Collections.emptyMap();

		ToolCore tool = (ToolCore) stack.getItem();

		Map<String, Object> out = new HashMap<>();

		if (tool.hasCategory(Category.HARVEST)) {
			out.put("miningSpeed", ToolHelper.getActualMiningSpeed(stack));
		}

		out.put("attack", ToolHelper.getActualAttack(stack));
		out.put("freeModifiers", ToolHelper.getFreeModifiers(stack));

		out.put("maxDurability", ToolHelper.getDurabilityStat(stack));
		out.put("durability", ToolHelper.getCurrentDurability(stack));


		{
			// Gather a list of all modifiers. We don't provide what they do, but this should be enough.
			Map<Integer, Map<String, String>> modifiers = new HashMap<>();
			int modIndex = 0;

			NBTTagList tagList = TagUtil.getModifiersTagList(stack);
			for (int i = 0; i < tagList.tagCount(); i++) {
				NBTTagCompound tag = tagList.getCompoundTagAt(i);
				ModifierNBT data = ModifierNBT.readTag(tag);

				// get matching modifier
				IModifier modifier = TinkerRegistry.getModifier(data.identifier);
				if (modifier == null || modifier.isHidden()) continue;

				Map<String, String> modifierData = new HashMap<>();
				modifierData.put("id", modifier.getIdentifier());
				modifierData.put("name", modifier.getLocalizedName());

				modifiers.put(++modIndex, modifierData);
			}
			out.put("modifiers", modifiers);
		}

		{
			// Gather a list of all parts for this tool
			Map<Integer, Object> parts = new HashMap<>();

			List<Material> materials = TinkerUtil.getMaterialsFromTagList(TagUtil.getBaseMaterialsTagList(stack));
			List<PartMaterialType> component = tool.getRequiredComponents();

			if (materials.size() >= component.size()) {
				int partIdx = 0;
				for (int i = 0; i < component.size(); i++) {
					PartMaterialType pmt = component.get(i);
					Material material = materials.get(i);

					// get (one possible) toolpart used to craft the thing
					Iterator<IToolPart> partIter = pmt.getPossibleParts().iterator();
					if (!partIter.hasNext()) continue;

					IToolPart part = partIter.next();
					ItemStack partStack = part.getItemstackWithMaterial(material);
					if (partStack != null) {
						parts.put(++partIdx, context.makePartialChild(pmt).makePartialChild(partStack).getMeta());
					}
				}
			}

			out.put("parts", parts);
		}


		return out;
	}
}
