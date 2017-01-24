package org.squiddev.plethora.gameplay.modules;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import dan200.computercraft.shared.peripheral.PeripheralType;
import dan200.computercraft.shared.peripheral.common.PeripheralItemFactory;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import org.squiddev.plethora.api.Constants;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.WorldLocation;
import org.squiddev.plethora.api.method.*;
import org.squiddev.plethora.api.module.BasicModuleContainer;
import org.squiddev.plethora.api.module.IModuleAccess;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.api.module.IModuleHandler;
import org.squiddev.plethora.api.reference.IReference;
import org.squiddev.plethora.core.*;
import org.squiddev.plethora.gameplay.BlockBase;
import org.squiddev.plethora.gameplay.client.tile.RenderManipulator;
import org.squiddev.plethora.utils.Helpers;
import org.squiddev.plethora.utils.RenderHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.squiddev.plethora.api.reference.Reference.tile;
import static org.squiddev.plethora.gameplay.modules.ManipulatorType.VALUES;

public final class BlockManipulator extends BlockBase<TileManipulator> implements IPeripheralProvider {
	private static final PropertyEnum<ManipulatorType> TYPE = PropertyEnum.create("type", ManipulatorType.class);

	public static final double OFFSET = 10.0 / 16.0;
	public static final double PIX = 1 / 16.0;

	public BlockManipulator() {
		super("manipulator", TileManipulator.class);
		setBlockBounds(0, 0, 0, 1, (float) OFFSET, 1);
		setDefaultState(getBlockState().getBaseState().withProperty(TYPE, ManipulatorType.MARK_1));
	}


	@Override
	public void getSubBlocks(Item item, CreativeTabs tab, List<ItemStack> itemStacks) {
		for (ManipulatorType type : VALUES) {
			itemStacks.add(new ItemStack(this, 1, type.ordinal()));
		}
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		IBlockState state = super.getStateFromMeta(meta);
		return state.withProperty(TYPE, VALUES[meta < 0 || meta >= VALUES.length ? 0 : meta]);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(TYPE).ordinal();
	}

	@Override
	protected BlockState createBlockState() {
		return new BlockState(this, TYPE);
	}

	@Override
	public String getUnlocalizedName(int meta) {
		return getUnlocalizedName() + "." + VALUES[meta < 0 || meta >= VALUES.length ? 0 : meta].getName();
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileManipulator(VALUES[meta < 0 || meta >= VALUES.length ? 0 : meta]);
	}

	@Override
	public void init() {
		super.init();
		ComputerCraftAPI.registerPeripheralProvider(this);

		// Prevent wrapping by accident
		FMLInterModComms.sendMessage(PlethoraCore.ID, Constants.IMC_BLACKLIST_PERIPHERAL, TileManipulator.class.getName());

		GameRegistry.addShapedRecipe(new ItemStack(this, 1, 0),
			"GCG",
			"RMR",
			"III",
			'G', Blocks.glass,
			'C', Items.gold_ingot,
			'R', Items.redstone,
			'M', PeripheralItemFactory.create(PeripheralType.WiredModem, null, 1),
			'I', Items.iron_ingot
		);

		GameRegistry.addShapedRecipe(new ItemStack(this, 1, 1),
			"CCC",
			"RMR",
			"III",
			'C', Items.gold_ingot,
			'R', Items.redstone,
			'M', new ItemStack(this, 1, 0),
			'I', Items.iron_ingot
		);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void clientInit() {
		for (ManipulatorType type : VALUES) {
			Helpers.setupModel(Item.getItemFromBlock(this), type.ordinal(), name + "." + type.getName());
		}

		ClientRegistry.bindTileEntitySpecialRenderer(TileManipulator.class, new RenderManipulator());

		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public boolean isFullBlock() {
		return false;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public IPeripheral getPeripheral(World world, BlockPos blockPos, EnumFacing enumFacing) {
		final TileEntity te = world.getTileEntity(blockPos);
		if (!(te instanceof TileManipulator)) return null;
		final TileManipulator manipulator = (TileManipulator) te;

		if (manipulator.getType() == null) return null;
		final int size = manipulator.getType().size();

		final ItemStack[] stacks = new ItemStack[size];
		Set<ResourceLocation> modules = Sets.newHashSet();
		Set<IModuleHandler> moduleHandlers = Sets.newHashSet();
		for (int i = 0; i < size; i++) {
			ItemStack stack = manipulator.getStack(i);
			if (stack == null) continue;

			stack = stacks[i] = stack.copy();

			IModuleHandler moduleHandler = stack.getCapability(Constants.MODULE_HANDLER_CAPABILITY, null);
			if (moduleHandler == null) continue;

			ResourceLocation module = moduleHandler.getModule();
			if (ConfigCore.Blacklist.blacklistModules.contains(module.toString())) continue;

			modules.add(module);
			moduleHandlers.add(moduleHandler);
		}

		if (modules.isEmpty()) return null;

		final IModuleContainer container = new BasicModuleContainer(modules);
		Map<ResourceLocation, ManipulatorAccess> accessMap = Maps.newHashMap();

		BasicContextBuilder builder = new BasicContextBuilder();
		for (IModuleHandler handler : moduleHandlers) {
			ResourceLocation module = handler.getModule();
			ManipulatorAccess access = accessMap.get(module);
			if (access == null) {
				accessMap.put(module, access = new ManipulatorAccess(manipulator, handler, container));
			}

			handler.getAdditionalContext(access, builder);
		}

		builder.addContext(te, tile(te));
		builder.<IWorldLocation>addContext(new WorldLocation(world, blockPos));

		ICostHandler cost = CostHelpers.getCostHandler(manipulator);
		IReference<IModuleContainer> containerRef = new IReference<IModuleContainer>() {
			@Nonnull
			@Override
			public IModuleContainer get() throws LuaException {
				for (int i = 0; i < size; i++) {
					ItemStack oldStack = stacks[i];
					ItemStack newStack = manipulator.getStack(i);
					if (oldStack != null && !ItemStack.areItemStacksEqual(stacks[i], newStack)) {
						IModuleHandler moduleHandler = oldStack.getCapability(Constants.MODULE_HANDLER_CAPABILITY, null);
						throw new LuaException("The " + moduleHandler.getModule() + " module has been removed");
					}
				}

				return container;
			}
		};

		IUnbakedContext<IModuleContainer> context = MethodRegistry.instance.makeContext(
			containerRef, cost, containerRef, builder.getReferenceArray()
		);

		IPartialContext<IModuleContainer> baked = new PartialContext<IModuleContainer>(
			container, cost, builder.getObjectsArray(), container
		);

		Pair<List<IMethod<?>>, List<IUnbakedContext<?>>> paired = MethodRegistry.instance.getMethodsPaired(context, baked);
		if (paired.getLeft().size() > 0) {
			TrackingWrapperPeripheral peripheral = new TrackingWrapperPeripheral("plethora:modules", te, paired, manipulator.getFactory());
			for (ManipulatorAccess access : accessMap.values()) {
				access.wrapper = peripheral;
			}
			return peripheral;
		} else {
			return null;
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void drawHighlight(DrawBlockHighlightEvent event) {
		if (event.target.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return;

		BlockPos blockPos = event.target.getBlockPos();

		IBlockState state = event.player.worldObj.getBlockState(blockPos);
		if (state.getBlock() != this) return;

		Vec3 hit = event.target.hitVec.subtract(blockPos.getX(), blockPos.getY(), blockPos.getZ());
		ManipulatorType type = state.getValue(TYPE);
		for (AxisAlignedBB box : type.boxes) {
			if (hit.yCoord > OFFSET - PIX &&
				hit.xCoord >= box.minX && hit.xCoord <= box.maxX &&
				hit.zCoord >= box.minZ && hit.zCoord <= box.maxZ) {

				RenderHelper.renderBoundingBox(event.player, box, event.target.getBlockPos(), event.partialTicks);
				event.setCanceled(true);
				break;
			}
		}
	}

	private static final class ManipulatorAccess implements IModuleAccess {
		private TrackingWrapperPeripheral wrapper;

		private final TileManipulator tile;
		private final IWorldLocation location;
		private final ResourceLocation module;
		private final IModuleContainer container;

		private ManipulatorAccess(TileManipulator tile, IModuleHandler module, IModuleContainer container) {
			this.tile = tile;
			this.location = new WorldLocation(tile.getWorld(), tile.getPos());
			this.module = module.getModule();
			this.container = container;
		}

		@Nonnull
		@Override
		public Object getOwner() {
			return tile;
		}

		@Nonnull
		@Override
		public IWorldLocation getLocation() {
			return location;
		}

		@Nonnull
		@Override
		public IModuleContainer getContainer() {
			return container;
		}

		@Nonnull
		@Override
		public NBTTagCompound getData() {
			return tile.getModuleData(module);
		}

		@Override
		public void markDataDirty() {
			tile.markModuleDataDirty();
		}

		@Override
		public void queueEvent(@Nonnull String event, @Nullable Object... args) {
			if (wrapper != null) wrapper.queueEvent(event, args);
		}
	}
}
