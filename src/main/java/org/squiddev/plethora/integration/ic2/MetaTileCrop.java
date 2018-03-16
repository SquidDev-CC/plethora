package org.squiddev.plethora.integration.ic2;

import ic2.api.crops.CropCard;
import ic2.api.crops.Crops;
import ic2.api.crops.ICropTile;
import ic2.core.IC2;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

@IMetaProvider.Inject(value = ICropTile.class, modId = IC2.MODID, namespace = "crop")
public class MetaTileCrop extends BasicMetaProvider<ICropTile> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull ICropTile object) {
		if (Crops.instance == null) return Collections.emptyMap();

		CropCard card = object.getCrop();
		if (card == null) return Collections.emptyMap();

		int level = object.getScanLevel();
		Map<Object, Object> out = MetaItemCrop.getMeta(card, level);
		if (level >= 4) {
			out.put("growth", object.getStatGrowth());
			out.put("gain", object.getStatGain());
			out.put("resistance", object.getStatResistance());
		}

		out.put("growthPoints", object.getGrowthPoints());
		out.put("size", object.getCurrentSize());
		out.put("humidity", object.getTerrainHumidity());
		out.put("airQuality", object.getTerrainAirQuality());
		out.put("nutrients", object.getTerrainNutrients());

		return out;
	}
}
