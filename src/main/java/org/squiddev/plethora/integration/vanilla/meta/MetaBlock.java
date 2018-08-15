package org.squiddev.plethora.integration.vanilla.meta;

import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

@IMetaProvider.Inject(Block.class)
public class MetaBlock extends BasicMetaProvider<Block> {
	public MetaBlock() {
		super("Provide the registry name, display name and translation key of a block.");
	}

	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull Block block) {
		return getBasicMeta(block);
	}

	public static Map<Object, Object> getBasicMeta(@Nonnull Block block) {
		HashMap<Object, Object> data = Maps.newHashMap();

		ResourceLocation name = block.getRegistryName();
		data.put("name", name == null ? "unknown" : name.toString());

		data.put("displayName", block.getLocalizedName());
		data.put("TranslationKey", block.getTranslationKey());

		return data;
	}

	@Override
	public Block getExample() {
		return Blocks.DIRT;
	}
}
