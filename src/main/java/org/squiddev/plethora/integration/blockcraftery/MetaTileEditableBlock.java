package org.squiddev.plethora.integration.blockcraftery;

import epicsquid.blockcraftery.Blockcraftery;
import epicsquid.blockcraftery.tile.TileEditableBlock;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.BaseMetaProvider;
import org.squiddev.plethora.api.method.IPartialContext;
import org.squiddev.plethora.integration.vanilla.meta.MetaItemBasic;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Injects(Blockcraftery.MODID)
public final class MetaTileEditableBlock extends BaseMetaProvider<TileEditableBlock> {
	@Nonnull
	@Override
	public Map<String, ?> getMeta(@Nonnull IPartialContext<TileEditableBlock> context) {
		Map<String, Object> result = new HashMap<>(2);
		TileEditableBlock object = context.getTarget();
		if (!object.stack.isEmpty()) {
			result.put("stack", MetaItemBasic.getBasicMeta(object.stack));
		}

		if (object.state.getBlock() != Blocks.AIR) {
			result.put("block", context.makePartialChild(object.state).getMeta());
		}
		return Collections.singletonMap("frames", result);
	}

	@Nonnull
	@Override
	public TileEditableBlock getExample() {
		TileEditableBlock tile = new TileEditableBlock();
		tile.stack = new ItemStack(Blocks.DIRT);
		tile.state = Blocks.DIRT.getDefaultState();
		return tile;
	}
}
