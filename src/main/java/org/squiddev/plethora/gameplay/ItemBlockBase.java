package org.squiddev.plethora.gameplay;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Objects;

public class ItemBlockBase extends ItemBlock {
	public ItemBlockBase(Block block) {
		super(block);

		setRegistryName(Objects.requireNonNull(block.getRegistryName()));
		setHasSubtypes(true);
		setMaxDamage(0);
	}

	@Override
	public int getMetadata(int meta) {
		return meta;
	}

	@Nonnull
	@Override
	public String getTranslationKey(ItemStack stack) {
		if (block instanceof BlockBase) {
			return ((BlockBase) block).getTranslationKey(stack.getItemDamage());
		} else {
			return block.getTranslationKey();
		}
	}
}
