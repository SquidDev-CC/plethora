package org.squiddev.plethora.integration.vanilla.converter;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecartMobSpawner;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.WorldLocation;
import org.squiddev.plethora.api.converter.ConstantConverter;
import org.squiddev.plethora.api.converter.DynamicConverter;
import org.squiddev.plethora.api.reference.BlockReference;
import org.squiddev.plethora.api.reference.ItemSlot;
import org.squiddev.plethora.integration.EntityIdentifier;
import org.squiddev.plethora.integration.vanilla.NullableItemStack;
import org.squiddev.plethora.utils.TypedField;

import static org.squiddev.plethora.api.converter.Converters.ofCapability;

@Injects
public final class VanillaConverters {
	private VanillaConverters() {
	}

	public static final ConstantConverter<ItemSlot, ItemStack> GET_SLOT_STACK = ItemSlot::getStack;

	public static final ConstantConverter<ItemStack, Item> GET_STACK_ITEM = ItemStack::getItem;

	public static final DynamicConverter<BlockReference, IBlockState> GET_BLOCK_REFERENCE_BLOCK = BlockReference::getState;

	public static final ConstantConverter<BlockReference, TileEntity> GET_BLOCK_REFERENCE_TILE = BlockReference::getTileEntity;

	public static final ConstantConverter<TileEntity, BlockReference> GET_TILE_REFERENCE = from -> {
		World world = from.getWorld();
		BlockPos pos = from.getPos();

		// Ensure we're referencing a valid TE
		return world == null || pos == null || world.getTileEntity(pos) != from
			? null
			: new BlockReference(new WorldLocation(world, pos), world.getBlockState(pos), from);
	};

	public static final ConstantConverter<IBlockState, Block> GET_BLOCK_STATE_BLOCK = IBlockState::getBlock;

	public static final DynamicConverter<IFluidTank, FluidStack> GET_TANK_FLUID = IFluidTank::getFluid;

	public static final DynamicConverter<IFluidTankProperties, FluidStack> GET_FLUID_TANK_PROPERTIES_FLUID = IFluidTankProperties::getContents;

	public static final DynamicConverter<ICapabilityProvider, IFluidHandler> GET_FLUID_HANDLER_CAP = ofCapability(() -> CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);

	public static final DynamicConverter<ICapabilityProvider, IEnergyStorage> GET_ENERGY_STORAGE_CAP = ofCapability(() -> CapabilityEnergy.ENERGY);

	public static final DynamicConverter<ICapabilityProvider, IItemHandler> GET_ITEM_HANDLER_CAP = from ->
		// ConverterInventory will handle IInventories, as that guarantees it'll be a constant object.
		!(from instanceof IInventory) && from.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)
			? from.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)
			: null;

	public static final ConstantConverter<IInventory, IItemHandler> INVENTORY_TO_ITEM_HANDLER = from -> {
		// Skip things we've already converted
		if (from instanceof IItemHandler) return null;

		// We could do this in ITEM_HANDLER_CAP, but this is (hopefully) constant.
		if (from instanceof ICapabilityProvider && ((ICapabilityProvider) from).hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
			IItemHandler handler = ((ICapabilityProvider) from).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
			// SidedInvWrapper for a null side will cause an NPE. While this is a problem with the mod returning such
			// an instance, it's happened enough times that we should ignore these instances.
			if (handler != null && !(handler instanceof SidedInvWrapper)) return handler;
		}

		return new InvWrapper(from);
	};

	public static final ConstantConverter<NullableItemStack, ItemStack> GET_NULLABLE_ITEM_STACK = NullableItemStack::getFilledStack;

	public static final ConstantConverter<Entity, EntityIdentifier> GET_ENTITY_IDENTIFIER = from ->
		from instanceof EntityPlayer
			? new EntityIdentifier.Player(((EntityPlayer) from).getGameProfile())
			: new EntityIdentifier(from);

	public static final ConstantConverter<TileEntityMobSpawner, MobSpawnerBaseLogic> GET_TILE_SPAWNER_LOGIC = TileEntityMobSpawner::getSpawnerBaseLogic;

	public static final ConstantConverter<EntityMinecartMobSpawner, MobSpawnerBaseLogic> GET_ENTITY_SPAWNER_LOGIC =
		TypedField.<EntityMinecartMobSpawner, MobSpawnerBaseLogic>of(EntityMinecartMobSpawner.class, "mobSpawnerLogic", "field_98040_a")::get;
}
