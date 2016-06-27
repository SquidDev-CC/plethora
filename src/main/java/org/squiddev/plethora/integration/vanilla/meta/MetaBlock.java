package org.squiddev.plethora.integration.vanilla.meta;

import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.api.meta.MetaProvider;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

@MetaProvider(Block.class)
public class MetaBlock implements IMetaProvider<Block> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull Block block) {
		HashMap<Object, Object> data = Maps.newHashMap();

		String name = block.getRegistryName();
		data.put("name", name == null ? "unknown" : name);

		data.put("displayName", block.getLocalizedName());
		data.put("unlocalizedName", block.getUnlocalizedName());

		return data;
	}
}
