package org.squiddev.plethora.gameplay;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.squiddev.plethora.gameplay.registry.IClientModule;
import org.squiddev.plethora.utils.Helpers;

import java.util.List;

public abstract class ItemBase extends Item implements IClientModule {
	protected final String name;

	public ItemBase(String itemName, int stackSize) {
		name = itemName;

		setUnlocalizedName(Plethora.RESOURCE_DOMAIN + "." + name);

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
	public void addInformation(ItemStack stack, EntityPlayer player, List<String> out, boolean um) {
		super.addInformation(stack, player, out, um);
		out.add(StatCollector.translateToLocal(getUnlocalizedName(stack) + ".desc"));
	}

	@Override
	public boolean canLoad() {
		return true;
	}

	@Override
	public void preInit() {
		GameRegistry.registerItem(this, name);
	}

	@Override
	public void init() {
	}

	@Override
	public void postInit() {
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void clientInit() {
		Helpers.setupModel(this, 0, name);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void clientPreInit() {
	}
}
