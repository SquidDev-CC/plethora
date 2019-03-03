package org.squiddev.plethora.integration.appliedenergistics;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridBlock;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AEPartLocation;
import appeng.core.AppEng;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.converter.ConstantConverter;
import org.squiddev.plethora.api.converter.DynamicConverter;

@Injects(AppEng.MOD_ID)
public final class ConverterAppliedEnergistics {
	private ConverterAppliedEnergistics() {
	}

	public static final DynamicConverter<IAEFluidStack, FluidStack> GET_AE_FLUID_STACK = IAEFluidStack::getFluidStack;

	public static final DynamicConverter<IAEItemStack, ItemStack> GET_AE_ITEM_STACK =
		from -> from.getStackSize() == 0 ? from.getDefinition() : from.createItemStack();

	public static final DynamicConverter<IGridNode, IGrid> GET_NODE_GRID = IGridNode::getGrid;

	public static final ConstantConverter<IGridNode, IGridBlock> GET_NODE_BLOCK = IGridNode::getGridBlock;

	public static final DynamicConverter<IGridHost, IGridNode> GET_GRID_NODE = from -> from.getGridNode(AEPartLocation.INTERNAL);
}
