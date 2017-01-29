package org.squiddev.plethora.gameplay;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import javax.annotation.Nonnull;


public class ItemBlockBase extends ItemBlock {
	public ItemBlockBase(Block block) {
		super(block);

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
		if (block instanceof BlockBase) {
			return ((BlockBase) block).getUnlocalizedName(stack.getItemDamage());
		} else {
			return block.getUnlocalizedName();
		}
	}
}
