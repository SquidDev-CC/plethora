package org.squiddev.plethora.neural;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.ISpecialArmor;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.squiddev.plethora.ItemBase;
import org.squiddev.plethora.Plethora;
import org.squiddev.plethora.client.ModelInterface;
import org.squiddev.plethora.registry.IClientModule;
import org.squiddev.plethora.utils.DebugLogger;
import org.squiddev.plethora.utils.Helpers;

import java.util.List;

public class ItemNeuralInterface extends ItemArmor implements IClientModule, ISpecialArmor {
	private static final ArmorMaterial FAKE_ARMOUR = EnumHelper.addArmorMaterial("FAKE_ARMOUR", "iwasbored_fake", 33, new int[]{0, 0, 0, 0}, 0);
	private static final ISpecialArmor.ArmorProperties FAKE_PROPERTIES = new ISpecialArmor.ArmorProperties(0, 0, 0);
	private static final String NAME = "neuralInterface";

	public ItemNeuralInterface() {
		super(FAKE_ARMOUR, 0, 0);

		setUnlocalizedName(Plethora.RESOURCE_DOMAIN + "." + NAME);
		setCreativeTab(Plethora.getCreativeTab());
	}

	@Override
	public ArmorProperties getProperties(EntityLivingBase entityLivingBase, ItemStack itemStack, DamageSource damageSource, double v, int i) {
		return FAKE_PROPERTIES;
	}

	@Override
	public int getArmorDisplay(EntityPlayer entityPlayer, ItemStack itemStack, int i) {
		return 0;
	}

	@Override
	public void damageArmor(EntityLivingBase entityLivingBase, ItemStack itemStack, DamageSource damageSource, int i, int i1) {
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List<String> out, boolean additional) {
		super.addInformation(stack, player, out, additional);
		out.add(StatCollector.translateToLocal(getUnlocalizedName(stack) + ".desc"));
	}

	@Override
	public void onArmorTick(World world, EntityPlayer player, ItemStack stack) {
		super.onArmorTick(world, player, stack);
		onUpdate(stack, player, true);
	}

	@Override
	public void onUpdate(ItemStack stauck, World world, Entity entity, int um1, boolean um2) {
		super.onUpdate(stack, world, entity, um1, um2);
		if (entity instanceof EntityLivingBase) {
			onUpdate(stack, (EntityLivingBase) entity, false);
		}
	}

	private void onUpdate(ItemStack stack, EntityLivingBase player, boolean forceActive) {
		if (player.worldObj.isRemote) {
			if (forceActive) NeuralManager.getClient(stack);
		} else {
			NBTTagCompound tag = ItemBase.getTag(stack);
			NeuralInterface neuralInterface;

			if (forceActive) {
				neuralInterface = NeuralManager.get(tag, player);
				neuralInterface.turnOn();
				neuralInterface.update();
			} else {
				neuralInterface = NeuralManager.tryGet(tag, player);
				if (neuralInterface == null) return;
			}

			if (neuralInterface.isDirty()) {
				neuralInterface.toNBT(tag);
				DebugLogger.debug("Is dirty: saving " + tag);
				String label = tag.getString("label");
				if (label == null || label.isEmpty()) {
					stack.clearCustomName();
				} else {
					stack.setStackDisplayName(label);
				}

				if (player instanceof EntityPlayer) {
					((EntityPlayer) player).inventory.markDirty();
				}
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ModelBiped getArmorModel(EntityLivingBase entity, ItemStack stack, int slot, ModelBiped existing) {
		return ModelInterface.get();
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, int slot, String existing) {
		return Plethora.RESOURCE_DOMAIN + ":textures/models/neuralInterface.png";
	}

	@Override
	public boolean canLoad() {
		return true;
	}

	@Override
	public void preInit() {
		GameRegistry.registerItem(this, NAME);
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
		Helpers.setupModel(this, 0, NAME);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void clientPreInit() {
	}
}
