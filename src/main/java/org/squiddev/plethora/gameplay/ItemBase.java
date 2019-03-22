package org.squiddev.plethora.gameplay;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.squiddev.plethora.utils.Helpers;

import java.util.List;

public abstract class ItemBase extends Item {
	private final String name;

	public ItemBase(String itemName, int stackSize) {
		name = itemName;

		setTranslationKey(Plethora.RESOURCE_DOMAIN + "." + name);
		setRegistryName(new ResourceLocation(Plethora.ID, name));

		setCreativeTab(Plethora.getCreativeTab());
		setMaxStackSize(stackSize);
	}

	public ItemBase(String itemName) {
		this(itemName, 64);
	}

	public static NBTTagCompound getTag(ItemStack stack) {
		NBTTagCompound tag = stack.getTagCompound();
		if (tag == null) stack.setTagCompound(tag = new NBTTagCompound());
		return tag;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World world, List<String> out, ITooltipFlag flag) {
		super.addInformation(stack, world, out, flag);
		out.add(Helpers.translateToLocal(getTranslationKey(stack) + ".desc"));
	}
}
