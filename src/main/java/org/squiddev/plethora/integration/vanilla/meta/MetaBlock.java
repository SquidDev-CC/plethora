package org.squiddev.plethora.integration.vanilla.meta;

import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

@IMetaProvider.Inject(Block.class)
public class MetaBlock extends BasicMetaProvider<Block> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull Block block) {
		HashMap<Object, Object> data = Maps.newHashMap();

		String name = block.getRegistryName().toString();
		data.put("name", name == null ? "unknown" : name);

		data.put("displayName", block.getLocalizedName());
		data.put("unlocalizedName", block.getUnlocalizedName());

		return data;
	}
}
