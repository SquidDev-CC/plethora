package org.squiddev.plethora.integration.blockcraftery;

import epicsquid.blockcraftery.Blockcraftery;
import epicsquid.blockcraftery.tile.TileEditableBlock;
import net.minecraft.init.Blocks;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.integration.vanilla.meta.MetaBlock;
import org.squiddev.plethora.integration.vanilla.meta.MetaBlockState;
import org.squiddev.plethora.integration.vanilla.meta.MetaItemBasic;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

@IMetaProvider.Inject(value = TileEditableBlock.class, namespace = "frames", modId = Blockcraftery.MODID)
public class MetaTileEditableBlock extends BasicMetaProvider<TileEditableBlock> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull TileEditableBlock object) {
		Map<Object, Object> result = new HashMap<>();
		if (!object.stack.isEmpty()) {
			result.put("stack", MetaItemBasic.getBasicMeta(object.stack));
		}

		if (object.state.getBlock() != Blocks.AIR) {
			Map<Object, Object> blockData = MetaBlock.getBasicMeta(object.state.getBlock());
			MetaBlockState.fillBasicMeta(blockData, object.state);
			result.put("block", blockData);
		}

		return result;
	}
}
