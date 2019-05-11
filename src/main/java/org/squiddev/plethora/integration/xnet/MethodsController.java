package org.squiddev.plethora.integration.xnet;


import mcjty.xnet.XNet;
import mcjty.xnet.api.channels.IControllerContext;
import mcjty.xnet.api.keys.SidedPos;
import mcjty.xnet.blocks.cables.ConnectorTileEntity;
import mcjty.xnet.blocks.controller.TileEntityController;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.method.ContextKeys;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.LuaList;
import org.squiddev.plethora.api.method.wrapper.FromContext;
import org.squiddev.plethora.api.method.wrapper.FromTarget;
import org.squiddev.plethora.api.method.wrapper.PlethoraMethod;
import org.squiddev.plethora.integration.vanilla.meta.MetaBlock;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

// Based heavily off of the OC XNet Driver mod https://minecraft.curseforge.com/projects/oc-xnet-driver
public final class MethodsController {
	private MethodsController() {
	}

	/*
	 *Links for convenience in pulling up references:
	 * https://raw.githubusercontent.com/thraaawn/OCXNetDriver/master/src/main/java/org/dave/ocxnetdriver/driver/controller/EnvironmentXnetController.java
	 * https://github.com/thraaawn/OCXNetDriver/raw/master/src/main/java/org/dave/ocxnetdriver/converter/ConverterBlockPos.java
	 *
	 */

	/*
	 *Methods defined in the OC XNet Driver:
	 * getConnectedBlocks
	 * getSupportedCapabilities(pos[, side])
	 * getItems(pos[, side])
	 * getFluids(pos[, side])
	 * getEnergy(pos[, side])
	 * transferItem(sourcePos, sourceSlot, amount, targetPos[, sourceSide[, targetSide]])
	 * transferFluids(sourcePos, amount, targetPos[, fluidName][, sourceSide[, targetSide]])
	 * transferEnergy(sourcePos, amount, targetPos[, sourceSide[, targetSide]])
	 * store(sourcePos, sourceSlot, database, entry[, sourceSide]) - Stores an itemstack in an OC database upgrade
	 *
	 *Additional data that we may expose, either method or meta
	 * NetworkId
	 * Channels
	 * RF storage - should already be exposed
	 * Logic state - the colors are defined in mcjty.xnet.api.channels.Color,
	 *   usage shown in mcjty.xnet.api.helper.AbstractConnectorSettings.calculateColorsMask
	 *   Alternatively, a player could set a Redstone Proxy and read the state;
	 *   we could theoretically expose a
	 */


	//----------
	//Start OC XNet Driver stubs
	//----------
	// This block is intended to simplify adapting the code from the OC XNet Driver mod,
	// and may be removed at a later time.

	/**
	 * Converts the internal absolute coords into user-facing relative coords
	 */
	@Nonnull
	private static BlockPos toRelative(@Nonnull BlockPos pos, @Nonnull BlockPos controllerPos) {
		return pos.subtract(controllerPos);
	}

	/**
	 * Converts the user's input relative coords into internal absolute coords
	 */
	@Nonnull
	private static BlockPos toAbsolute(@Nonnull BlockPos pos, BlockPos controllerPos) {
		return pos.add(controllerPos);
	}

	@Nullable
	private static SidedPos getSidedPos(BlockPos pos, @Nonnull IControllerContext context) {
		return context.getConnectedBlockPositions().stream()
			.filter(sp -> sp.getPos().equals(pos)).findFirst()
			.orElse(null);
	}

	//----------
	//End OC XNet Driver stubs
	//----------

	//TODO Check if this (and the other methods in this class)
	// can use IControllerContext instead of TileEntityController
	@PlethoraMethod(
		modId = XNet.MODID,
		doc = "-- List all blocks connected to the XNet network"
	)
	public static Map<Integer, Object> getConnectedBlocks(@FromTarget TileEntityController controller,
														  @FromContext(ContextKeys.ORIGIN) IWorldLocation location) {
		LuaList<Object> out = new LuaList<>();
		World world = location.getWorld();
		BlockPos controllerPos = location.getPos();

		for (SidedPos pos : controller.getConnectedBlockPositions()) {
			Map<String, Object> inner = new HashMap<>();
			IBlockState state = world.getBlockState(pos.getPos());

			//REFINE Some of this could probably be exposed using our existing meta providers
			inner.put("pos", toRelative(pos.getPos(), controllerPos));
			inner.put("side", pos.getSide());

			//inner.put("name", state.getBlock().getRegistryName());
			inner.putAll(MetaBlock.getBasicMeta(state.getBlock()));
			inner.put("meta", state.getBlock().getMetaFromState(state));

			BlockPos connectorPos = pos.getPos().offset(pos.getSide());

			String connectorName = getConnectorName(world, connectorPos);
			if (connectorName != null) inner.put("connector", connectorName);

			out.add(inner);
		}

		return out.asMap();
	}

	// Extracted to try and find a cleaner structure; not much luck, as I end up with either
	// nested if statements, or multiple returns...
	private static String getConnectorName(World world, BlockPos connectorPos) {
		ResourceLocation resourceLocation = world.getBlockState(connectorPos).getBlock().getRegistryName();
		if (resourceLocation == null) {
			return null;
		}

		String registryName = resourceLocation.toString();
		// REFINE IDEA suggests extracting a Set from this condition, and using Set#contains
		//  However, I'm not sure about the trade-offs; it _may_ perform better,
		//  in exchange for increased memory overhead.
		if ("xnet:advanced_connector".equals(registryName) || "xnet:connector".equals(registryName)) {
			TileEntity tile = world.getTileEntity(connectorPos);
			if (tile instanceof ConnectorTileEntity) {
				ConnectorTileEntity connectorTile = (ConnectorTileEntity) tile;
				String connectorName = connectorTile.getConnectorName();
				if (connectorName != null && !connectorName.isEmpty()) {
					return connectorName;
				}
			}
		}
		return null;
	}


	@PlethoraMethod(
		modId = XNet.MODID,
		doc = "-- FIXME Describe the method"
	)
	public static Map<String, ?> getSupportedCapabilities(@Nonnull IContext<TileEntityController> context) {
		//MEMO Check if you need the full `IContext` or can make due with `@FromTarget`

		//TODO Fill in this code; OC XNet Driver parses the target position and side, gets the TE, then checks for
		// item, fluid, and energy capabilities.
		// To expose a similar behavior, I will need to define a custom ArgumentType

		return null; //FIXME set the actual return
	}
}
