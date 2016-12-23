package org.squiddev.plethora.gameplay;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;


public class ItemBlockBase extends ItemBlock {
	public ItemBlockBase(Block block) {
		super(block);
		if (!(block instanceof BlockBase<?>)) {
			throw new IllegalStateException("Cannot register " + block.getClass() + " with ItemBlockBase");
		}

		setHasSubtypes(true);
		setMaxDamage(0);
	}

	@Override
	public int getMetadata(int meta) {
		return meta;
	}

	@Nonnull
	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return ((BlockBase) block).getUnlocalizedName(stack.getItemDamage());
	}
}
