package org.squiddev.plethora.integration.tconstruct;

import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.ItemStackMetaProvider;
import org.squiddev.plethora.api.meta.SimpleMetaProvider;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.materials.ExtraMaterialStats;
import slimeknights.tconstruct.library.materials.HandleMaterialStats;
import slimeknights.tconstruct.library.materials.HeadMaterialStats;
import slimeknights.tconstruct.library.materials.IMaterialStats;
import slimeknights.tconstruct.library.modifiers.IToolMod;
import slimeknights.tconstruct.library.tools.IToolPart;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Injects(TConstruct.modID)
public final class MetaTConstruct {
	public static final SimpleMetaProvider<IMaterialStats> META_MATERIAL_STATS = stats -> {
		Map<String, Object> out = new HashMap<>(2);
		out.put("id", stats.getIdentifier());
		out.put("name", stats.getLocalizedName());
		return out;
	};

	public static final SimpleMetaProvider<ExtraMaterialStats> META_EXTRA_MATERIAL_STATS = stats ->
		Collections.singletonMap("extraDurability", stats.extraDurability);

	public static final SimpleMetaProvider<HandleMaterialStats> META_HANDLE_STATS = stats -> {
		Map<String, Object> out = new HashMap<>(2);
		out.put("durability", stats.durability);
		out.put("modifier", stats.modifier);
		return out;
	};


	public static final SimpleMetaProvider<HeadMaterialStats> META_HEAD_STATS = stats -> {
		Map<String, Object> out = new HashMap<>(4);
		out.put("attack", stats.attack);
		out.put("durability", stats.durability);
		out.put("miningSpeed", stats.miningspeed);
		out.put("miningLevel", stats.harvestLevel);
		return out;
	};

	public static final SimpleMetaProvider<IToolMod> META_TOOL_MOD = mod -> {
		Map<String, Object> out = new HashMap<>(2);
		out.put("id", mod.getIdentifier());
		out.put("name", mod.getLocalizedName());
		return out;
	};

	public static final ItemStackMetaProvider<IToolPart> META_TOOL_PART = new ItemStackMetaProvider<IToolPart>("toolPart", IToolPart.class) {
		@Nonnull
		@Override
		public Map<String, ?> getMeta(@Nonnull ItemStack stack, @Nonnull IToolPart toolPart) {
			return Collections.singletonMap("cost", toolPart.getCost());
		}
	};

	private MetaTConstruct() {
	}
}
