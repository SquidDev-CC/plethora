package org.squiddev.plethora.integration.vanilla.meta;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.PlethoraAPI;
import org.squiddev.plethora.api.meta.BaseMetaProvider;
import org.squiddev.plethora.api.method.IPartialContext;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

@Injects
public final class MetaBlockState extends BaseMetaProvider<IBlockState> {
	public MetaBlockState() {
		super("Provides some very basic information about a block and its associated state.");
	}

	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull IPartialContext<IBlockState> context) {
		IBlockState state = context.getTarget();
		Block block = state.getBlock();

		Map<Object, Object> data = new HashMap<>();
		fillBasicMeta(data, state);

		Material material = state.getMaterial();
		data.put("material", PlethoraAPI.instance().metaRegistry().getMeta(context.makePartialChild(material)));

		int level = block.getHarvestLevel(state);
		if (level >= 0) data.put("harvestLevel", level);
		data.put("harvestTool", block.getHarvestTool(state));

		return data;
	}

	public static void fillBasicMeta(@Nonnull Map<? super String, Object> data, @Nonnull IBlockState state) {
		Block block = state.getBlock();

		data.put("metadata", block.getMetaFromState(state));

		HashMap<Object, Object> stateProperties = new HashMap<>();
		data.put("state", stateProperties);
		for (Map.Entry<IProperty<?>, Comparable<?>> item : state.getProperties().entrySet()) {
			Object value = item.getValue();
			if (!(value instanceof String) && !(value instanceof Number) && !(value instanceof Boolean)) {
				value = value.toString();
			}
			stateProperties.put(item.getKey().getName(), value);
		}
	}

	@Override
	public IBlockState getExample() {
		return Blocks.STONE.getDefaultState();
	}
}
