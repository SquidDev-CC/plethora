package org.squiddev.plethora.integration.vanilla.meta;

import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

@IMetaProvider.Inject(value = IBlockState.class)
public class MetaBlockState extends BasicMetaProvider<IBlockState> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull IBlockState state) {
		Block block = state.getBlock();

		HashMap<Object, Object> data = Maps.newHashMap();
		data.put("metadata", block.getMetaFromState(state));

		HashMap<Object, Object> stateProperties = Maps.newHashMap();
		data.put("state", stateProperties);
		for (Map.Entry<IProperty, Comparable> item : state.getProperties().entrySet()) {
			Object value = item.getValue();
			if (!(value instanceof String) && !(value instanceof Number) && !(value instanceof Boolean)) {
				value = value.toString();
			}
			stateProperties.put(item.getKey().getName(), value);
		}

		int level = block.getHarvestLevel(state);
		if (level >= 0) data.put("harvestLevel", level);
		data.put("harvestTool", block.getHarvestTool(state));

		MapColor mapCol = block.getMapColor(state);
		if (mapCol != null) {
			int col = mapCol.colorValue;

			Map<String, Integer> colour = Maps.newHashMap();
			colour.put("r", (col >> 16) & 0xFF);
			colour.put("g", (col >> 8) & 0xFF);
			colour.put("b", col & 0xFF);

			data.put("colour", colour);
			data.put("color", Maps.newHashMap(colour));
		}

		return data;
	}
}
