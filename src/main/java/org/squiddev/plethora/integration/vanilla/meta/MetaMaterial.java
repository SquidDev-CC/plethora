package org.squiddev.plethora.integration.vanilla.meta;

import com.google.common.collect.Maps;
import net.minecraft.block.material.Material;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;

import javax.annotation.Nonnull;
import java.util.Map;

@IMetaProvider.Inject(Material.class)
public class MetaMaterial extends BasicMetaProvider<Material> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull Material material) {
		Map<Object, Object> data = Maps.newHashMap();

		data.put("replaceable", material.isReplaceable());
		data.put("liquid", material.isLiquid());
		data.put("opaque", material.isOpaque());
		data.put("solid", material.isSolid());
		data.put("blocksMovement", material.blocksMovement());
		data.put("blocksLight", material.blocksMovement());
		data.put("requiresTool", !material.isToolNotRequired());
		data.put("burns", material.getCanBurn());

		return data;
	}
}
