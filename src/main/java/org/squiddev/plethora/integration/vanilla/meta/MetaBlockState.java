package org.squiddev.plethora.integration.vanilla.meta;

import com.google.common.collect.Maps;
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
	public Map<Object, Object> getMeta(@Nonnull IBlockState object) {
		HashMap<Object, Object> data = Maps.newHashMap();
		data.put("metadata", object.getBlock().getMetaFromState(object));

		HashMap<Object, Object> state = Maps.newHashMap();
		data.put("state", state);
		for (Map.Entry<IProperty, Comparable> item : object.getProperties().entrySet()) {
			Object value = item.getValue();
			if (!(value instanceof String) && !(value instanceof Number) && !(value instanceof Boolean)) {
				value = value.toString();
			}
			state.put(item.getKey().getName(), value);
		}

		return data;
	}
}
