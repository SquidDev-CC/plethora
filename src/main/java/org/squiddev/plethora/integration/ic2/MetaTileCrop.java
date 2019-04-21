package org.squiddev.plethora.integration.ic2;

import ic2.api.crops.CropCard;
import ic2.api.crops.Crops;
import ic2.api.crops.ICropTile;
import ic2.core.IC2;
import ic2.core.crop.IC2Crops;
import ic2.core.crop.TileEntityCrop;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.BasicMetaProvider;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

@Injects(IC2.MODID)
public final class MetaTileCrop extends BasicMetaProvider<ICropTile> {
	@Nonnull
	@Override
	public Map<String, ?> getMeta(@Nonnull ICropTile object) {
		if (Crops.instance == null) return Collections.emptyMap();

		CropCard card = object.getCrop();
		if (card == null) return Collections.emptyMap();

		int level = object.getScanLevel();
		Map<String, Object> out = MetaItemCrop.getMeta(card, level);
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

		return Collections.singletonMap("crop", out);
	}

	@Nonnull
	@Override
	public ICropTile getExample() {
		TileEntityCrop tile = new TileEntityCrop();
		tile.setCrop(IC2Crops.cropFerru);
		return tile;
	}
}
