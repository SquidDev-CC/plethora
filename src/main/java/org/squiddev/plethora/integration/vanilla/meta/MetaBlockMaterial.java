package org.squiddev.plethora.integration.vanilla.meta;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import org.squiddev.plethora.api.PlethoraAPI;
import org.squiddev.plethora.api.meta.BaseMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.api.method.IPartialContext;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

@IMetaProvider.Inject(value = Block.class, namespace = "material")
public class MetaBlockMaterial extends BaseMetaProvider<Block> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull IPartialContext<Block> context) {
		Material material = context.getTarget().getMaterial();
		if (material != null) {
			return PlethoraAPI.instance().metaRegistry().getMeta(context.makePartialChild(material));
		} else {
			return Collections.emptyMap();
		}
	}
}
