package org.squiddev.plethora.integration.mcmultipart;

import dan200.computercraft.api.lua.LuaException;
import mcmultipart.MCMultiPart;
import mcmultipart.api.container.IPartInfo;
import mcmultipart.api.multipart.IMultipartTile;
import mcmultipart.api.slot.IPartSlot;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.converter.ConstantConverter;
import org.squiddev.plethora.api.converter.DynamicConverter;
import org.squiddev.plethora.api.meta.SimpleMetaProvider;
import org.squiddev.plethora.api.method.wrapper.ArgumentType;
import org.squiddev.plethora.api.method.wrapper.ArgumentTypes;
import org.squiddev.plethora.integration.vanilla.meta.MetaBlock;

import javax.annotation.Nonnull;
import java.util.Map;

@Injects(MCMultiPart.MODID)
public final class IntegrationMcMultipart {
	private IntegrationMcMultipart() {
	}

	public static final DynamicConverter<IPartInfo, IBlockState> PART_INFO_TO_BLOCK_STATE = part -> {
		World world = part.getPartWorld();
		BlockPos pos = part.getPartPos();
		return part.getPart().getExtendedState(world, pos, part, part.getPart().getActualState(world, pos, part));
	};

	public static final ConstantConverter<IPartInfo, TileEntity> PART_INTO_TO_TILE = part -> {
		IMultipartTile tile = part.getTile();
		return tile == null ? null : tile.getTileEntity();
	};

	public static final SimpleMetaProvider<IPartInfo> META_MULTIPART = IntegrationMcMultipart::getBasicMeta;

	@Nonnull
	public static Map<Object, Object> getBasicMeta(@Nonnull IPartInfo part) {
		return MetaBlock.getBasicMeta(part.getPart().getBlock());
	}

	public static final ArgumentType<IPartSlot> ARGUMENT_PART_SLOT = ArgumentTypes.RESOURCE.map(slotName -> {
		final IPartSlot slot = MCMultiPart.slotRegistry.getValue(slotName);
		if (slot == null) throw new LuaException("Bad slot '" + slotName + "'");
		return slot;
	});
}
