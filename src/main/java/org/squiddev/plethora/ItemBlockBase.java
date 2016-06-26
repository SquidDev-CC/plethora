package org.squiddev.plethora;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import java.util.List;


public class ItemBlockBase extends ItemBlock {
	public ItemBlockBase(Block block) {
		super(block);
		if (!(block instanceof BlockBase<?>)) {
			throw new IllegalStateException("Cannot register " + block.getClass() + " with ItemBlockBase");
		}
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List<String> out, boolean um) {
		super.addInformation(stack, player, out, um);
		((BlockBase<?>) block).addInformation(stack, player, out, um);
	}
}
