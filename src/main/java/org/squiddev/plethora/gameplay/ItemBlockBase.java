package org.squiddev.plethora.gameplay;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import java.util.List;


public class ItemBlockBase extends ItemBlock {
	public ItemBlockBase(Block block) {
		super(block);

		setHasSubtypes(true);
		setMaxDamage(0);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List<String> out, boolean um) {
		super.addInformation(stack, player, out, um);
		out.add(StatCollector.translateToLocal(getUnlocalizedName(stack) + ".desc"));
	}

	@Override
	public int getMetadata(int meta) {
		return meta;
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		if (block instanceof BlockBase) {
			return ((BlockBase) block).getUnlocalizedName(stack.getItemDamage());
		} else {
			return block.getUnlocalizedName();
		}
	}
}
