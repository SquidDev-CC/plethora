package org.squiddev.plethora.integration.xnet;


import dan200.computercraft.api.lua.LuaException;
import mcjty.xnet.XNet;
import mcjty.xnet.api.channels.IControllerContext;
import mcjty.xnet.api.keys.SidedPos;
import mcjty.xnet.blocks.cables.ConnectorTileEntity;
import mcjty.xnet.blocks.controller.TileEntityController;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.method.ContextKeys;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.LuaList;
import org.squiddev.plethora.api.method.wrapper.FromContext;
import org.squiddev.plethora.api.method.wrapper.FromTarget;
import org.squiddev.plethora.api.method.wrapper.Optional;
import org.squiddev.plethora.api.method.wrapper.PlethoraMethod;
import org.squiddev.plethora.integration.vanilla.meta.MetaBlock;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

	/*
	 *TODO Can we convert this to use existing item/fluid/energy handler code?
	 * A decent portion of this class focuses on listing the items/fluids/energy exposed by tiles,
	 * and transferring such to other tiles...
	 *
	 *REFINE Extract the LuaException constructors into helper methods, since the same phrases are re-used
	 *
	 *TODO The capability checks do not account for the ability of Advanced Connectors to perform sneaky sidedness...
	 */

	/*
	 *----------
	 *Start OC XNet Driver stubs
	 *----------
	 *This block is intended to simplify adapting the code from the OC XNet Driver mod,
	 *and may be removed at a later time.
	 */

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
	private static BlockPos toAbsolute(@Nonnull BlockPos pos, @Nonnull BlockPos controllerPos) {
		return pos.add(controllerPos);
	}

	@Nullable
	private static SidedPos getSidedPos(@Nonnull BlockPos pos, @Nonnull IControllerContext context) {
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
	public static Map<Integer, Object> getConnectedBlocks(@Nonnull @FromTarget TileEntityController controller,
														  @Nonnull @FromContext(ContextKeys.ORIGIN) IWorldLocation location) {
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
	@Nullable
	private static String getConnectorName(@Nonnull World world, @Nonnull BlockPos connectorPos) {
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


	@Nullable
	@PlethoraMethod(
		modId = XNet.MODID,
		doc = "-- List all capability of the given block at the given or connected side"
	)
	public static Map<Integer, String> getSupportedCapabilities(@Nonnull @FromTarget TileEntityController controller,
																@Nonnull @FromContext(ContextKeys.ORIGIN) IWorldLocation location,
																@Nonnull BlockPos posArg,
																@Optional EnumFacing sideArg) throws LuaException {
		//REFINE Yes, the BlockPos and EnumFacing args need better names...
		BlockPos pos = toAbsolute(posArg, location.getPos());

		SidedPos sidedPos = getSidedPos(pos, controller);
		if (sidedPos == null) throw badPosition();

		EnumFacing side = (sideArg != null) ? sideArg : sidedPos.getSide();

		TileEntity tile = location.getWorld().getTileEntity(pos);
		if (tile == null) throw nonTile();

		LuaList<String> out = new LuaList<>(3);

		if (tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side)) out.add("items");
		if (tile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side)) out.add("fluid");
		if (tile.hasCapability(CapabilityEnergy.ENERGY, side)) out.add("energy");

		return out.asMap();
	}

	@PlethoraMethod(
		modId = XNet.MODID,
		doc = "-- List all items in the given inventory"
	)
	public static Map<String, ?> getItems(@Nonnull @FromTarget TileEntityController controller,
										  @Nonnull @FromContext(ContextKeys.ORIGIN) IWorldLocation location,
										  @Nonnull BlockPos posArg,
										  @Optional EnumFacing sideArg) throws LuaException {
		BlockPos pos = toAbsolute(posArg, location.getPos());

		SidedPos sidedPos = getSidedPos(pos, controller);
		if (sidedPos == null) throw badPosition();

		EnumFacing side = (sideArg != null) ? sideArg : sidedPos.getSide();

		TileEntity tile = location.getWorld().getTileEntity(pos);
		if (tile == null) throw nonTile();

		if (!tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side)) throw nonItemHandler();

		IItemHandler handler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
		List<ItemStack> result = new ArrayList<>();
		for (int slot = 0; slot < handler.getSlots(); slot++) {
			ItemStack stack = handler.getStackInSlot(slot);
			result.add(stack.copy());
		}

		//FIXME set the actual return
		// OC XNet driver wrapped the `result` list in an Object array, but CC doesn't have a native ItemStack type,
		// and Plethora already handles listing items in an inventory...

		return null;
	}

	@PlethoraMethod(
		modId = XNet.MODID,
		doc = "-- Transfer items between two inventories"
	)
	public static int transferItem(@Nonnull @FromTarget TileEntityController controller,
								   @Nonnull @FromContext(ContextKeys.ORIGIN) IWorldLocation location,
								   @Nonnull BlockPos sourcePosArg,
								   int sourceSlot,
								   int amount,
								   @Nonnull BlockPos targetPosArg,
								   @Optional EnumFacing sourceSideArg,
								   @Optional EnumFacing targetSideArg) throws LuaException {
		BlockPos pos = toAbsolute(sourcePosArg, location.getPos());

		SidedPos sidedPos = getSidedPos(pos, controller);
		if(sidedPos == null) throw badPosition("source");

		EnumFacing side = (sourceSideArg != null) ? sourceSideArg : sidedPos.getSide();

		TileEntity tileEntity = location.getWorld().getTileEntity(pos);
		if(tileEntity == null) throw nonTile("source");

		if(!tileEntity.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side)) {
			throw nonItemHandler("source");
		}

		BlockPos targetPos = toAbsolute(targetPosArg, location.getPos());

		SidedPos targetSidedPos = getSidedPos(targetPos, controller);
		if(targetSidedPos == null) throw badPosition("target");

		EnumFacing targetSide = (targetSideArg != null) ? targetSideArg : targetSidedPos.getSide();

		TileEntity targetTileEntity = location.getWorld().getTileEntity(targetPos);
		if(targetTileEntity == null) throw nonTile("target");

		if(!targetTileEntity.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, targetSide)) {
			throw nonItemHandler("target");
		}

		IItemHandler handler = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
		IItemHandler targetHandler = targetTileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, targetSide);


		//`handler` should be nonnull, as we already checked if the tile had the capability;
		// only edge case would be someone modifying the tile mid-tick...
		//noinspection ConstantConditions
		ItemStack sourceStackSim = handler.extractItem(sourceSlot-1, amount, true);
		if(sourceStackSim.isEmpty()) throw new LuaException("can not extract from source slot");

		ItemStack returnStackSim = ItemHandlerHelper.insertItemStacked(targetHandler, sourceStackSim, true);

		int transferableAmount = returnStackSim.isEmpty() ? amount : sourceStackSim.getCount() - returnStackSim.getCount();

		if (transferableAmount <= 0) throw new LuaException("can not insert into target");

		ItemStack sourceStackReal = handler.extractItem(sourceSlot - 1, transferableAmount, false);
		ItemHandlerHelper.insertItemStacked(targetHandler, sourceStackReal, false);

		return transferableAmount;
	}

	@PlethoraMethod(
		modId = XNet.MODID,
		doc = "-- List all fluids in the given tank"
	)
	public static Map<Integer, ?> getFluids(@Nonnull @FromTarget TileEntityController controller,
											@Nonnull @FromContext(ContextKeys.ORIGIN) IWorldLocation location,
											@Nonnull BlockPos posArg,
											@Optional EnumFacing sideArg) throws LuaException {
		BlockPos pos = toAbsolute(posArg, location.getPos());

		SidedPos sidedPos = getSidedPos(pos, controller);
		if(sidedPos == null) throw badPosition();

		EnumFacing side = (sideArg != null) ? sideArg : sidedPos.getSide();

		TileEntity tileEntity = location.getWorld().getTileEntity(pos);
		if(tileEntity == null) throw nonTile();

		if(!tileEntity.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side)) {
			throw nonFluidHandler();
		}

		IFluidHandler handler = tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side);

		LuaList<Object> result = new LuaList<>();
		for(IFluidTankProperties tank : handler.getTankProperties()) {
			HashMap<String, Object> map = new HashMap<>();
			map.put("capacity", tank.getCapacity());
			map.put("content", tank.getContents());
			result.add(map);
		}

		return result.asMap();
	}

	@PlethoraMethod(
		modId = XNet.MODID,
		doc = "-- Transfer fluids between two tanks"
	)
	public static int transferFluid(@Nonnull IContext<TileEntityController> context,
									@Nonnull @FromTarget TileEntityController controller,
									@Nonnull @FromContext(ContextKeys.ORIGIN) IWorldLocation location,
									@Nonnull BlockPos sourcePosArg,
									int amount,
									@Nonnull BlockPos targetPosArg,
									@Optional String fluidName,
									@Optional EnumFacing sourceSideArg,
									@Optional EnumFacing targetSideArg) throws LuaException {
		//MEMO Check if you need the full `IContext` or can make due with `@FromTarget`
		BlockPos pos = toAbsolute(sourcePosArg, location.getPos());

		SidedPos sidedPos = getSidedPos(pos, controller);
		if(sidedPos == null) throw badPosition("source");

		EnumFacing side = (sourceSideArg != null) ? sourceSideArg : sidedPos.getSide();

		TileEntity tileEntity = location.getWorld().getTileEntity(pos);
		if(tileEntity == null) throw nonTile("source");

		if(!tileEntity.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side)) {
			throw nonFluidHandler("source");
		}

		BlockPos targetPos = toAbsolute(targetPosArg, location.getPos());

		SidedPos targetSidedPos = getSidedPos(targetPos, controller);
		if(targetSidedPos == null) throw badPosition("target");

		EnumFacing targetSide = (targetSideArg != null) ? targetSideArg : targetSidedPos.getSide();

		TileEntity targetTileEntity = location.getWorld().getTileEntity(targetPos);
		if(targetTileEntity == null) throw nonTile("target");

		if(!targetTileEntity.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, targetSide)) {
			throw nonFluidHandler("target");
		}

		FluidStack extractStack = null;
		if(fluidName != null) {
			extractStack = FluidRegistry.getFluidStack(fluidName, amount);
			if(extractStack == null) {
				throw new LuaException("Unkown fluid: " + fluidName);
			}
		}

		IFluidHandler handler = tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side);
		IFluidHandler targetHandler = targetTileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, targetSide);

		FluidStack simStack;
		if(extractStack != null) {
			simStack = handler.drain(extractStack, false);
		} else {
			simStack = handler.drain(amount, false);
		}

		if(simStack == null) {
			throw new LuaException("can not drain from source tank");
		}

		int simAmount = targetHandler.fill(simStack, false);
		if(simAmount <= 0) {
			throw new LuaException("can not fill target tank");
		}

		FluidStack realStack;
		if(extractStack != null) {
			extractStack.amount = simAmount;
			realStack = handler.drain(extractStack, true);
		} else {
			realStack = handler.drain(simAmount, true);
		}

		targetHandler.fill(realStack, true);

		return simAmount;
	}

	@PlethoraMethod(
		modId = XNet.MODID,
		doc = "-- function(pos:table[, side: number]):table -- Get capacity and stored energy of the given energy handler"
	)
	public static Map<String, ?> getEnergy(@Nonnull IContext<TileEntityController> context,
										   @Nonnull @FromTarget TileEntityController controller,
										   @Nonnull @FromContext(ContextKeys.ORIGIN) IWorldLocation location,
										   @Nonnull BlockPos posArg,
										   @Optional EnumFacing sideArg) throws LuaException {
		//MEMO Check if you need the full `IContext` or can make due with `@FromTarget`

		BlockPos pos = toAbsolute(posArg, location.getPos());
		SidedPos sidedPos = getSidedPos(pos, controller);

		if(sidedPos == null) throw badPosition();

		EnumFacing side = (sideArg != null) ? sideArg : sidedPos.getSide(); //TODO Verify correct variables

		TileEntity tileEntity = location.getWorld().getTileEntity(pos);
		if(tileEntity == null) throw nonTile();

		if(!tileEntity.hasCapability(CapabilityEnergy.ENERGY, side)) {
			throw nonEnergyHandler();
		}

		IEnergyStorage handler = tileEntity.getCapability(CapabilityEnergy.ENERGY, side);

		HashMap<String, Object> result = new HashMap<>();
		result.put("capacity", handler.getMaxEnergyStored());
		result.put("stored", handler.getEnergyStored());
		result.put("canExtract", handler.canExtract());
		result.put("canReceive", handler.canReceive());

		return result;
	}

	@PlethoraMethod(
		modId = XNet.MODID,
		doc = "-- Transfer energy between two energy handlers"
	)
	public static int transferEnergy(@Nonnull IContext<TileEntityController> context,
												@Nonnull @FromTarget TileEntityController controller,
												@Nonnull @FromContext(ContextKeys.ORIGIN) IWorldLocation location,
												@Nonnull BlockPos sourcePosArg,
												int amount,
												@Nonnull BlockPos targetPosArg,
												@Optional EnumFacing sourceSideArg,
												@Optional EnumFacing targetSideArg) throws LuaException {
		//MEMO Check if you need the full `IContext` or can make due with `@FromTarget`
		BlockPos pos = toAbsolute(sourcePosArg, location.getPos());

		SidedPos sidedPos = getSidedPos(pos, controller);
		if(sidedPos == null) throw badPosition("source");

		EnumFacing side = (sourceSideArg != null) ? sourceSideArg : sidedPos.getSide();

		TileEntity tileEntity = location.getWorld().getTileEntity(pos);
		if(tileEntity == null) throw nonTile("source");

		if(!tileEntity.hasCapability(CapabilityEnergy.ENERGY, side)) {
			throw nonEnergyHandler("source");
		}

		BlockPos targetPos = toAbsolute(targetPosArg, location.getPos());
		SidedPos targetSidedPos = getSidedPos(targetPos, controller);
		if(targetSidedPos == null) throw badPosition("target");

		EnumFacing targetSide = (targetSideArg != null) ? targetSideArg : targetSidedPos.getSide();

		TileEntity targetTileEntity = location.getWorld().getTileEntity(targetPos);
		if(targetTileEntity == null) throw nonTile("target");

		if(!targetTileEntity.hasCapability(CapabilityEnergy.ENERGY, targetSide)) {
			throw nonEnergyHandler("target");
		}

		IEnergyStorage handler = tileEntity.getCapability(CapabilityEnergy.ENERGY, side);
		IEnergyStorage targetHandler = targetTileEntity.getCapability(CapabilityEnergy.ENERGY, targetSide);

		if(!handler.canExtract()) {
			throw new LuaException("can not extract energy from source");
		}

		if(!targetHandler.canReceive()) {
			throw new LuaException("can not insert energy into target");
		}

		int transferred = 0;
		int simulatedTicks = 0;
		List<String> errors = new ArrayList<>();
		while(transferred < amount && simulatedTicks < 1) {
			int simAmount = handler.extractEnergy(amount - transferred, true);
			if(simAmount <= 0) {
				errors.add("extractable amount from source is 0");
				break;
			}

			int simReceived = targetHandler.receiveEnergy(simAmount, true);
			if(simReceived <= 0) {
				errors.add("insertable amount into target is 0");
				break;
			}

			simulatedTicks++;
			handler.extractEnergy(simReceived, false);
			targetHandler.receiveEnergy(simReceived, false);

			transferred += simReceived;
		}

		//return new Object[]{ transferred, errors };
		return transferred; //FIXME set the actual return
	}

	@Nonnull
	private static LuaException badPosition() {
		return badPosition("");
	}

	@Nonnull
	private static LuaException badPosition(@Nonnull String description) {
		//REFINE Is there a better way to handle the appending a space for non-empty strings?
		String descriptionPadded = !description.isEmpty() ? description + " " : "";
		return new LuaException("Given " + descriptionPadded + "position is not connected to the network");
	}

	@Nonnull
	private static LuaException nonTile() {
		//REFINE Does this exception even make sense?
		// I mean, if a block is connected to the network, shouldn't it be a TE?
		return nonTile("");
	}

	@Nonnull
	private static LuaException nonTile(@Nonnull String description) {
		String descriptionPlus = !description.isEmpty() ? " - " + description : "";
		return new LuaException("Not a tile entity" + descriptionPlus);
	}

	@Nonnull
	private static LuaException nonItemHandler() {
		return nonItemHandler("");
	}

	@Nonnull
	private static LuaException nonItemHandler(@Nonnull String description) {
		String descriptionPlus = !description.isEmpty() ? " - " + description : "";
		return new LuaException("Not an item handler" + descriptionPlus);
	}

	@Nonnull
	private static LuaException nonFluidHandler() {
		return nonFluidHandler("");
	}

	@Nonnull
	private static LuaException nonFluidHandler(@Nonnull String description) {
		String descriptionPlus = !description.isEmpty() ? " - " + description : "";
		return new LuaException("Not a fluid handler" + descriptionPlus);
	}

	@Nonnull
	private static LuaException nonEnergyHandler() {
		return nonEnergyHandler("");
	}

	@Nonnull
	private static LuaException nonEnergyHandler(@Nonnull String description) {
		String descriptionPlus = !description.isEmpty() ? " - " + description : "";
		return new LuaException("Not an energy handler" + descriptionPlus);
	}
}
