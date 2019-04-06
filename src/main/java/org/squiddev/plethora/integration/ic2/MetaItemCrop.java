package org.squiddev.plethora.integration.ic2;

import ic2.api.crops.CropCard;
import ic2.api.crops.CropProperties;
import ic2.api.crops.Crops;
import ic2.api.crops.ICropSeed;
import ic2.core.IC2;
import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.ItemStackMetaProvider;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Injects(IC2.MODID)
public final class MetaItemCrop extends ItemStackMetaProvider<ICropSeed> {
	public MetaItemCrop() {
		super("crop", ICropSeed.class);
	}

	@Nonnull
	@Override
	public Map<String, ?> getMeta(@Nonnull ItemStack object, @Nonnull ICropSeed seed) {
		if (Crops.instance == null) return Collections.emptyMap();

		CropCard card = Crops.instance.getCropCard(object);
		if (card == null) return Collections.emptyMap();

		int level = seed.getScannedFromStack(object);
		Map<String, Object> out = getMeta(card, level);
		if (level >= 4) {
			out.put("growth", seed.getGrowthFromStack(object));
			out.put("gain", seed.getGainFromStack(object));
			out.put("resistance", seed.getResistanceFromStack(object));
		}

		return out;
	}

	public static Map<String, Object> getMeta(CropCard card, int level) {
		Map<String, Object> out = new HashMap<>();

		out.put("scanLevel", level);

		CropProperties properties = card.getProperties();

		if (level >= 1) {
			out.put("name", card.getUnlocalizedName());
			out.put("owner", card.getOwner());
		}

		if (level >= 2) {
			out.put("discoverer", card.getDiscoveredBy());
			out.put("tier", properties.getTier());

			Map<String, Object> props = new HashMap<>();
			out.put("props", props);
			props.put("chemistry", properties.getChemistry());
			props.put("weed", properties.getWeed());
			props.put("colourful", properties.getColorful());
			props.put("colorful", properties.getColorful());
			props.put("consumable", properties.getConsumable());
			props.put("defensive", properties.getDefensive());

			out.put("maxSize", card.getMaxSize());
		}

		if (level >= 3) {
			String[] attributes = card.getAttributes();
			Map<Integer, String> list = new HashMap<>();
			for (int i = 0; i < attributes.length; i++) {
				list.put(i + 1, attributes[i]);
			}
			out.put("attributes", list);
		}

		return out;
	}
}
