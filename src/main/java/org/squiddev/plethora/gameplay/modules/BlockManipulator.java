package org.squiddev.plethora.gameplay.modules;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import dan200.computercraft.shared.peripheral.PeripheralType;
import dan200.computercraft.shared.peripheral.common.PeripheralItemFactory;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
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
import org.squiddev.plethora.api.Constants;
import org.squiddev.plethora.api.WorldLocation;
import org.squiddev.plethora.api.method.CostHelpers;
import org.squiddev.plethora.api.method.IMethod;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.api.module.IModuleHandler;
import org.squiddev.plethora.api.reference.IReference;
import org.squiddev.plethora.api.reference.Reference;
import org.squiddev.plethora.core.MethodRegistry;
import org.squiddev.plethora.core.MethodWrapperPeripheral;
import org.squiddev.plethora.core.PlethoraCore;
import org.squiddev.plethora.core.UnbakedContext;
import org.squiddev.plethora.gameplay.BlockBase;
import org.squiddev.plethora.gameplay.client.tile.RenderManipulator;
import org.squiddev.plethora.utils.Helpers;
import org.squiddev.plethora.utils.RenderHelper;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.squiddev.plethora.api.reference.Reference.tile;
import static org.squiddev.plethora.gameplay.modules.ManipulatorType.VALUES;

public final class BlockManipulator extends BlockBase<TileManipulator> implements IPeripheralProvider {
	private static final PropertyEnum<ManipulatorType> TYPE = PropertyEnum.create("type", ManipulatorType.class);

	public static final double OFFSET = 10.0 / 16.0;
	public static final double PIX = 1 / 16.0;

	private static final AxisAlignedBB BOX = new AxisAlignedBB(0, 0, 0, 1, (float) OFFSET, 1);

	public BlockManipulator() {
		super("manipulator", TileManipulator.class);
		setDefaultState(getBlockState().getBaseState().withProperty(TYPE, ManipulatorType.MARK_1));
	}

	@Override
	@SuppressWarnings({"NullableProblems", "deprecation"})
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return BOX;
	}

	@Override
	public void getSubBlocks(@Nonnull Item item, CreativeTabs tab, List<ItemStack> itemStacks) {
		for (ManipulatorType type : VALUES) {
			itemStacks.add(new ItemStack(this, 1, type.ordinal()));
		}
	}

	@Override
	@SuppressWarnings("deprecation")
	public IBlockState getStateFromMeta(int meta) {
		IBlockState state = super.getStateFromMeta(meta);
		return state.withProperty(TYPE, VALUES[meta < 0 || meta >= VALUES.length ? 0 : meta]);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(TYPE).ordinal();
	}

	@Nonnull
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, TYPE);
	}

	@Override
	public String getUnlocalizedName(int meta) {
		return getUnlocalizedName() + "." + VALUES[meta < 0 || meta >= VALUES.length ? 0 : meta].getName();
	}

	@Nonnull
	@Override
	public TileEntity createNewTileEntity(@Nonnull World world, int meta) {
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
			'G', new ItemStack(Blocks.GLASS),
			'C', new ItemStack(Items.GOLD_INGOT),
			'R', new ItemStack(Items.REDSTONE),
			'M', PeripheralItemFactory.create(PeripheralType.WiredModem, null, 1),
			'I', new ItemStack(Items.IRON_INGOT)
		);

		GameRegistry.addShapedRecipe(new ItemStack(this, 1, 1),
			"CCC",
			"RMR",
			"III",
			'C', new ItemStack(Items.GOLD_INGOT),
			'R', new ItemStack(Items.REDSTONE),
			'M', new ItemStack(this, 1, 0),
			'I', new ItemStack(Items.IRON_INGOT)
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
	@SuppressWarnings("deprecation")
	public boolean isFullBlock(IBlockState state) {
		return false;
	}

	@Override
	@SuppressWarnings("deprecation")
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public IPeripheral getPeripheral(World world, BlockPos blockPos, EnumFacing enumFacing) {
		final TileEntity te = world.getTileEntity(blockPos);
		if (!(te instanceof TileManipulator)) return null;
		final TileManipulator manipulator = (TileManipulator) te;

		final int size = manipulator.getType().size();

		boolean exists = false;
		final ItemStack[] stacks = new ItemStack[size];
		Set<ResourceLocation> modules = Sets.newHashSet();
		List<IReference<?>> additionalContext = Lists.newArrayList();
		for (int i = 0; i < size; i++) {
			ItemStack stack = manipulator.getStack(i);
			if (stack == null) continue;

			stack = stacks[i] = stack.copy();

			IModuleHandler moduleHandler = stack.getCapability(Constants.MODULE_HANDLER_CAPABILITY, null);
			if (moduleHandler == null) continue;

			exists = true;
			modules.add(moduleHandler.getModule());
			additionalContext.addAll(moduleHandler.getAdditionalContext());
		}

		if (!exists) return null;

		IReference<?>[] contextData = new IReference[additionalContext.size() + 2];
		additionalContext.toArray(contextData);
		contextData[contextData.length - 2] = tile(te);
		contextData[contextData.length - 1] = new WorldLocation(world, blockPos);

		final Set<ResourceLocation> moduleSet = Collections.unmodifiableSet(modules);

		IModuleContainer container = new IModuleContainer() {
			@Nonnull
			@Override
			public Set<ResourceLocation> get() throws LuaException {
				for (int i = 0; i < size; i++) {
					ItemStack oldStack = stacks[i];
					ItemStack newStack = manipulator.getStack(i);
					if (oldStack != null && !ItemStack.areItemStacksEqual(stacks[i], newStack)) {
						IModuleHandler moduleHandler = oldStack.getCapability(Constants.MODULE_HANDLER_CAPABILITY, null);
						throw new LuaException("The " + moduleHandler.getModule() + " module has been removed");
					}
				}

				return moduleSet;
			}
		};

		IUnbakedContext<IModuleContainer> context = MethodRegistry.instance.makeContext(
			Reference.id(container),
			CostHelpers.getCostHandler(manipulator),
			container,
			contextData
		);

		Tuple<List<IMethod<?>>, List<IUnbakedContext<?>>> paired = MethodRegistry.instance.getMethodsPaired(context, UnbakedContext.tryBake(context));
		if (paired.getFirst().size() > 0) {
			return new MethodWrapperPeripheral("plethora:modules", te, paired.getFirst(), paired.getSecond());
		} else {
			return null;
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void drawHighlight(DrawBlockHighlightEvent event) {
		if (event.getTarget().typeOfHit != RayTraceResult.Type.BLOCK) return;

		BlockPos blockPos = event.getTarget().getBlockPos();

		IBlockState state = event.getPlayer().worldObj.getBlockState(blockPos);
		if (state.getBlock() != this) return;

		Vec3d hit = event.getTarget().hitVec.subtract(blockPos.getX(), blockPos.getY(), blockPos.getZ());
		ManipulatorType type = state.getValue(TYPE);
		for (AxisAlignedBB box : type.boxes) {
			if (hit.yCoord > OFFSET - PIX &&
				hit.xCoord >= box.minX && hit.xCoord <= box.maxX &&
				hit.zCoord >= box.minZ && hit.zCoord <= box.maxZ) {

				RenderHelper.renderBoundingBox(event.getPlayer(), box, event.getTarget().getBlockPos(), event.getPartialTicks());
				event.setCanceled(true);
				break;
			}
		}
	}
}
