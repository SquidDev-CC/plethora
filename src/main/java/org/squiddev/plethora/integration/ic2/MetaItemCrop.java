package org.squiddev.plethora.integration.ic2;

import com.google.common.collect.Maps;
import ic2.api.crops.CropCard;
import ic2.api.crops.CropProperties;
import ic2.api.crops.Crops;
import ic2.api.crops.ICropSeed;
import ic2.core.IC2;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

@IMetaProvider.Inject(value = ItemStack.class, modId = IC2.MODID, namespace = "crop")
public class MetaItemCrop extends BasicMetaProvider<ItemStack> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull ItemStack object) {
		if (Crops.instance == null) return Collections.emptyMap();

		Item item = object.getItem();
		if (!(item instanceof ICropSeed)) return Collections.emptyMap();
		ICropSeed seed = (ICropSeed) item;

		CropCard card = Crops.instance.getCropCard(object);
		if (card == null) return Collections.emptyMap();

		int level = seed.getScannedFromStack(object);
		Map<Object, Object> out = getMeta(card, level);
		if (level >= 4) {
			out.put("growth", seed.getGrowthFromStack(object));
			out.put("gain", seed.getGainFromStack(object));
			out.put("resistance", seed.getResistanceFromStack(object));
		}

		return out;
	}

	public static Map<Object, Object> getMeta(CropCard card, int level) {
		Map<Object, Object> out = Maps.newHashMap();

		out.put("scanLevel", level);

		CropProperties properties = card.getProperties();

		if (level >= 1) {
			out.put("name", card.getUnlocalizedName());
			out.put("owner", card.getOwner());
		}

		if (level >= 2) {
			out.put("discoverer", card.getDiscoveredBy());
			out.put("tier", properties.getTier());

			Map<String, Object> props = Maps.newHashMap();
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
			Map<Integer, String> list = Maps.newHashMap();
			for (int i = 0; i < attributes.length; i++) {
				list.put(i + 1, attributes[i]);
			}
			out.put("attributes", attributes);
		}

		return out;
	}
}
