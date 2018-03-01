package org.squiddev.plethora.gameplay.modules;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import dan200.computercraft.shared.peripheral.PeripheralType;
import dan200.computercraft.shared.peripheral.common.PeripheralItemFactory;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
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
import org.squiddev.plethora.api.method.ContextKeys;
import org.squiddev.plethora.api.method.CostHelpers;
import org.squiddev.plethora.api.method.IMethod;
import org.squiddev.plethora.api.module.BasicModuleContainer;
import org.squiddev.plethora.api.module.IModuleAccess;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.api.module.IModuleHandler;
import org.squiddev.plethora.api.reference.ConstantReference;
import org.squiddev.plethora.api.reference.IReference;
import org.squiddev.plethora.core.*;
import org.squiddev.plethora.gameplay.BlockBase;
import org.squiddev.plethora.gameplay.client.tile.RenderManipulator;
import org.squiddev.plethora.utils.Helpers;
import org.squiddev.plethora.utils.MatrixHelpers;
import org.squiddev.plethora.utils.PlayerHelpers;
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
	public static final PropertyDirection FACING = BlockDirectional.FACING;

	public static final double OFFSET = 10.0 / 16.0;
	public static final double PIX = 1 / 16.0;
	public static final double BOX_EXPAND = 0.002;

	private static final AxisAlignedBB[] BOXES = new AxisAlignedBB[6];

	static {
		AxisAlignedBB box = new AxisAlignedBB(0, 0, 0, 1, (float) OFFSET, 1);
		for (EnumFacing facing : EnumFacing.VALUES) {
			BOXES[facing.ordinal()] = MatrixHelpers.transform(box, MatrixHelpers.matrixFor(facing));
		}
	}

	public BlockManipulator() {
		super("manipulator", TileManipulator.class);
		setDefaultState(getBlockState().getBaseState().withProperty(TYPE, ManipulatorType.MARK_1));
	}

	@Nonnull
	@Override
	@Deprecated
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return BOXES[state.getValue(FACING).ordinal()];
	}

	@Nullable
	@Override
	@Deprecated
	public RayTraceResult collisionRayTrace(IBlockState blockState, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull Vec3d start, @Nonnull Vec3d end) {
		ManipulatorType type = blockState.getValue(TYPE);
		EnumFacing facing = blockState.getValue(FACING);

		Vec3d startOff = start.subtract(pos.getX(), pos.getY(), pos.getZ());
		Vec3d endOff = end.subtract(pos.getX(), pos.getY(), pos.getZ());

		// Compute the intersection with the main box
		AxisAlignedBB primary = getBoundingBox(blockState, world, pos);
		RayTraceResult result = primary.calculateIntercept(startOff, endOff);
		double distance = result == null ? Double.POSITIVE_INFINITY : result.hitVec.squareDistanceTo(startOff);

		// Look for one of our inputs if possible.
		for (AxisAlignedBB child : type.boxesFor(facing)) {
			RayTraceResult hit = child.calculateIntercept(startOff, endOff);
			if (hit != null) {
				double newDistance = hit.hitVec.squareDistanceTo(startOff);
				if (newDistance <= distance) {
					result = hit;
					distance = newDistance;
				}
			}
		}

		return result == null ? null : new RayTraceResult(result.hitVec.addVector(pos.getX(), pos.getY(), pos.getZ()), result.sideHit, pos);
	}

	@Override
	public void getSubBlocks(@Nonnull Item item, CreativeTabs tab, NonNullList<ItemStack> itemStacks) {
		for (ManipulatorType type : VALUES) {
			itemStacks.add(new ItemStack(this, 1, type.ordinal()));
		}
	}

	@Override
	@SuppressWarnings("deprecation")
	public IBlockState getStateFromMeta(int meta) {
		ManipulatorType type = VALUES[meta & 1];
		EnumFacing facing = (meta >> 1) <= 6 ? EnumFacing.VALUES[meta >> 1] : EnumFacing.DOWN;

		return super.getStateFromMeta(meta).withProperty(TYPE, type).withProperty(FACING, facing);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(TYPE).ordinal() | state.getValue(FACING).ordinal() << 1;
	}

	@Override
	@Deprecated
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		return getStateFromMeta(meta).withProperty(FACING, facing.getOpposite());
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		super.onBlockPlacedBy(world, pos, state, placer, stack);

		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileManipulator) {
			((TileManipulator) te).setOwningProfile(PlayerHelpers.getProfile(placer));
		}
	}

	@Nonnull
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, TYPE, FACING);
	}

	@Override
	public String getUnlocalizedName(int meta) {
		return getUnlocalizedName() + "." + VALUES[meta & 1].getName();
	}

	@Nonnull
	@Override
	public TileEntity createNewTileEntity(@Nonnull World world, int meta) {
		return new TileManipulator(VALUES[meta & 1]);
	}

	@Override
	public int damageDropped(IBlockState state) {
		return state.getValue(TYPE).ordinal();
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
			'G', Blocks.GLASS,
			'C', Items.GOLD_INGOT,
			'R', Items.REDSTONE,
			'M', PeripheralItemFactory.create(PeripheralType.WiredModem, null, 1),
			'I', new ItemStack(Items.IRON_INGOT)
		);

		GameRegistry.addShapedRecipe(new ItemStack(this, 1, 1),
			"CCC",
			"RMR",
			"III",
			'C', Items.GOLD_INGOT,
			'R', Items.REDSTONE,
			'M', new ItemStack(this, 1, 0),
			'I', Items.IRON_INGOT
		);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void clientPreInit() {
		for (ManipulatorType type : VALUES) {
			Helpers.setupModel(Item.getItemFromBlock(this), type.ordinal(), name + "." + type.getName());
		}

		ClientRegistry.bindTileEntitySpecialRenderer(TileManipulator.class, new RenderManipulator());

		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	@Deprecated
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	@Deprecated
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public IPeripheral getPeripheral(@Nonnull World world, @Nonnull BlockPos blockPos, @Nonnull EnumFacing enumFacing) {
		final TileEntity te = world.getTileEntity(blockPos);
		if (!(te instanceof TileManipulator)) return null;
		final TileManipulator manipulator = (TileManipulator) te;

		if (manipulator.getType() == null) return null;
		final int size = manipulator.getType().size();

		final int stackHash = manipulator.getStackHash();

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

		IReference<IModuleContainer> containerRef = new ConstantReference<IModuleContainer>() {
			@Nonnull
			@Override
			public IModuleContainer get() throws LuaException {
				if (manipulator.isInvalid()) throw new LuaException("Manipulator is no longer there");

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

			@Nonnull
			@Override
			public IModuleContainer safeGet() throws LuaException {
				if (manipulator.isInvalid()) throw new LuaException("Manipulator is no longer there");

				if (stackHash != manipulator.getStackHash()) {
					throw new LuaException("A module has changed");
				}

				return container;
			}
		};

		ContextFactory<IModuleContainer> factory = ContextFactory.of(container, containerRef)
			.withCostHandler(CostHelpers.getCostHandler(manipulator))
			.withModules(container, containerRef)
			.addContext(ContextKeys.ORIGIN, te, tile(te))
			.<IWorldLocation>addContext(ContextKeys.ORIGIN, new WorldLocation(world, blockPos));

		for (IModuleHandler handler : moduleHandlers) {
			ResourceLocation module = handler.getModule();
			ManipulatorAccess access = accessMap.get(module);
			if (access == null) {
				accessMap.put(module, access = new ManipulatorAccess(manipulator, handler, container));
			}

			handler.getAdditionalContext(access, factory);
		}

		Pair<List<IMethod<?>>, List<UnbakedContext<?>>> paired = MethodRegistry.instance.getMethodsPaired(factory.getBaked());
		if (paired.getLeft().size() > 0) {
			ModulePeripheral peripheral = new ModulePeripheral("manipulator", te, paired, manipulator.getFactory(), factory.getAttachments(), stackHash);
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
		if (event.getTarget().typeOfHit != RayTraceResult.Type.BLOCK) return;

		BlockPos blockPos = event.getTarget().getBlockPos();

		IBlockState state = event.getPlayer().getEntityWorld().getBlockState(blockPos);
		if (state.getBlock() != this) return;

		EnumFacing facing = state.getValue(FACING);

		Vec3d hit = event.getTarget().hitVec.subtract(blockPos.getX(), blockPos.getY(), blockPos.getZ());
		ManipulatorType type = state.getValue(TYPE);
		for (AxisAlignedBB box : type.boxesFor(facing)) {
			if (box.expand(BOX_EXPAND, BOX_EXPAND, BOX_EXPAND).isVecInside(hit)) {
				RenderHelper.renderBoundingBox(event.getPlayer(), box, event.getTarget().getBlockPos(), event.getPartialTicks());
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
