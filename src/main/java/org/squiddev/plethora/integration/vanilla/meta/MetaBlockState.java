package org.squiddev.plethora.integration.vanilla.meta;

import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import org.squiddev.plethora.api.PlethoraAPI;
import org.squiddev.plethora.api.meta.BaseMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.api.method.IPartialContext;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

@IMetaProvider.Inject(value = IBlockState.class)
public class MetaBlockState extends BaseMetaProvider<IBlockState> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull IPartialContext<IBlockState> context) {
		HashMap<Object, Object> data = Maps.newHashMap();

		IBlockState state = context.getTarget();
		Block block = state.getBlock();
		data.put("metadata", block.getMetaFromState(state));

		HashMap<Object, Object> stateProperties = Maps.newHashMap();
		data.put("state", stateProperties);
		for (Map.Entry<IProperty<?>, Comparable<?>> item : state.getProperties().entrySet()) {
			Object value = item.getValue();
			if (!(value instanceof String) && !(value instanceof Number) && !(value instanceof Boolean)) {
				value = value.toString();
			}
			stateProperties.put(item.getKey().getName(), value);
		}

		Material material = state.getMaterial();
		data.put("material", PlethoraAPI.instance().metaRegistry().getMeta(context.makePartialChild(material)));

		int level = block.getHarvestLevel(state);
		if (level >= 0) data.put("harvestLevel", level);
		data.put("harvestTool", block.getHarvestTool(state));

		return data;
	}
}
