package org.squiddev.plethora.gameplay.redstone;

import com.google.common.collect.Sets;
import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import dan200.computercraft.shared.common.BlockGeneric;
import dan200.computercraft.shared.common.TileGeneric;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.squiddev.plethora.gameplay.ItemBlockBase;
import org.squiddev.plethora.gameplay.Plethora;
import org.squiddev.plethora.gameplay.registry.IClientModule;
import org.squiddev.plethora.utils.Helpers;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.Set;

/**
 * We extends {@link BlockGeneric} as the bundled redstone provider requires it:
 * {@link ComputerCraftAPI#getBundledRedstoneOutput(World, BlockPos, EnumFacing)}.
 */
public class BlockRedstoneIntegrator extends BlockGeneric implements IClientModule, IPeripheralProvider {
	private static final Set<TileRedstoneIntegrator> toTick = Sets.newConcurrentHashSet();

	private static final String NAME = "redstone_integrator";

	public BlockRedstoneIntegrator() {
		super(Material.ROCK);

		setHardness(2);
		setUnlocalizedName(Plethora.RESOURCE_DOMAIN + "." + NAME);
		setCreativeTab(Plethora.getCreativeTab());
	}

	@Nonnull
	@Override
	@Optional.Method(modid = "This mod should never exist: just a hack to remove this method")
	public TileEntity createNewTileEntity(@Nonnull World worldIn, int meta) {
		return new TileRedstoneIntegrator();
	}

	@Override
	protected IBlockState getDefaultBlockState(int meta, EnumFacing direction) {
		return blockState.getBaseState();
	}

	@Override
	protected TileGeneric createTile(IBlockState blockState) {
		return new TileRedstoneIntegrator();
	}

	@Override
	protected TileGeneric createTile(int meta) {
		return new TileRedstoneIntegrator();
	}

	@Nonnull
	@Override
	@Deprecated
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public boolean canLoad() {
		return true;
	}

	@Override
	public void preInit() {
		GameRegistry.registerTileEntity(TileRedstoneIntegrator.class, Plethora.RESOURCE_DOMAIN + ":" + NAME);

		MinecraftForge.EVENT_BUS.register(this);
		ComputerCraftAPI.registerPeripheralProvider(this);
	}

	@SubscribeEvent
	public void registerBlocks(RegistryEvent.Register<Block> event) {
		event.getRegistry().register(this.setRegistryName(new ResourceLocation(Plethora.RESOURCE_DOMAIN, NAME)));
	}

	@SubscribeEvent
	public void registerItems(RegistryEvent.Register<Item> event) {
		event.getRegistry().register(new ItemBlockBase(this).setRegistryName(new ResourceLocation(Plethora.RESOURCE_DOMAIN, NAME)));
	}

	@Override
	public void init() {
	}

	@Override
	public void postInit() {
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void clientInit() {
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void clientPreInit() {
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void registerModels(ModelRegistryEvent event) {
		Helpers.setupModel(Item.getItemFromBlock(this), 0, NAME);
	}

	public static void enqueueTick(TileRedstoneIntegrator tile) {
		toTick.add(tile);
	}

	@SubscribeEvent
	public void handleTick(TickEvent.ServerTickEvent e) {
		if (e.phase == TickEvent.Phase.START) {
			Iterator<TileRedstoneIntegrator> iterator = toTick.iterator();
			while (iterator.hasNext()) {
				TileRedstoneIntegrator tile = iterator.next();
				tile.updateOnce();
				iterator.remove();
			}
		}
	}

	@SubscribeEvent
	public void handleUnload(WorldEvent.Unload e) {
		World eventWorld = e.getWorld();
		if (!eventWorld.isRemote) {
			Iterator<TileRedstoneIntegrator> iterator = toTick.iterator();
			while (iterator.hasNext()) {
				World world = iterator.next().getWorld();
				if (world == null || world == eventWorld) iterator.remove();
			}
		}
	}

	@Override
	public IPeripheral getPeripheral(@Nonnull World world, @Nonnull BlockPos blockPos, @Nonnull EnumFacing enumFacing) {
		TileEntity te = world.getTileEntity(blockPos);
		return te instanceof TileRedstoneIntegrator ? (IPeripheral) te : null;
	}
}
