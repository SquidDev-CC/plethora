package org.squiddev.plethora.gameplay.neural;

import baubles.api.BaubleType;
import baubles.api.BaublesApi;
import baubles.api.IBauble;
import baubles.api.cap.IBaublesItemHandler;
import baubles.common.Baubles;
import com.google.common.base.Objects;
import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.filesystem.IMount;
import dan200.computercraft.api.media.IMedia;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.items.IComputerItem;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.ISpecialArmor;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.squiddev.cctweaks.CCTweaks;
import org.squiddev.cctweaks.api.computer.ICustomRomItem;
import org.squiddev.plethora.gameplay.ItemBase;
import org.squiddev.plethora.gameplay.Plethora;
import org.squiddev.plethora.gameplay.client.ModelInterface;
import org.squiddev.plethora.gameplay.registry.IClientModule;
import org.squiddev.plethora.utils.Helpers;
import org.squiddev.plethora.utils.PlayerHelpers;
import org.squiddev.plethora.utils.TinySlot;

import javax.annotation.Nonnull;
import java.util.List;

import static org.squiddev.plethora.gameplay.neural.ItemComputerHandler.COMPUTER_ID;
import static org.squiddev.plethora.gameplay.neural.ItemComputerHandler.DIRTY;
import static org.squiddev.plethora.gameplay.neural.NeuralHelpers.ARMOR_SLOT;

@Optional.InterfaceList({
	@Optional.Interface(iface = "baubles.api.IBauble", modid = Baubles.MODID),
	@Optional.Interface(iface = "org.squiddev.cctweaks.api.computer.ICustomRomItem", modid = CCTweaks.ID)
})
public class ItemNeuralInterface extends ItemArmor implements IClientModule, ISpecialArmor, IComputerItem, IMedia, IBauble, ICustomRomItem {
	private static final ArmorMaterial FAKE_ARMOUR = EnumHelper.addArmorMaterial("FAKE_ARMOUR", "iwasbored_fake", -1, new int[]{0, 0, 0, 0}, 0, SoundEvents.ITEM_ARMOR_EQUIP_CHAIN, 2);
	private static final ISpecialArmor.ArmorProperties FAKE_PROPERTIES = new ISpecialArmor.ArmorProperties(0, 0, 0);
	private static final String NAME = "neuralInterface";

	public ItemNeuralInterface() {
		super(FAKE_ARMOUR, 0, ARMOR_SLOT);

		setUnlocalizedName(Plethora.RESOURCE_DOMAIN + "." + NAME);
		setCreativeTab(Plethora.getCreativeTab());
	}

	@Override
	public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer player, EntityLivingBase entity, EnumHand hand) {
		if (!NeuralRegistry.instance.canEquip(entity)) return false;

		if (entity.getItemStackFromSlot(NeuralHelpers.ARMOR_SLOT).isEmpty() && stack.getCount() == 1) {
			if (!player.getEntityWorld().isRemote) {
				entity.setItemStackToSlot(NeuralHelpers.ARMOR_SLOT, stack.copy());

				// Force dropping when killed
				if (entity instanceof EntityLiving) {
					EntityLiving living = (EntityLiving) entity;
					living.setDropChance(NeuralHelpers.ARMOR_SLOT, 2);
					living.enablePersistence();
				}

				if (!player.capabilities.isCreativeMode) {
					stack.setCount(0);
				}
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	@Nonnull
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, @Nonnull EnumHand hand) {
		// Check if the entity we've just hit has a stack in the first slot. If so, use that instead.
		ItemStack stack = player.getHeldItem(hand);
		RayTraceResult hit = PlayerHelpers.findHitGuess(player);
		Entity entity = hit.entityHit;
		if (hit.typeOfHit == RayTraceResult.Type.ENTITY && !(entity instanceof EntityPlayer) && entity instanceof EntityLivingBase) {
			if (((EntityLivingBase) entity).getItemStackFromSlot(NeuralHelpers.ARMOR_SLOT).isEmpty() && stack.getCount() == 1) {
				return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
			}
		}

		if (Loader.isModLoaded(Baubles.MODID)) {
			IBaublesItemHandler handler = BaublesApi.getBaublesHandler(player);
			for (int slot : NeuralHelpers.getBaubleType().getValidSlots()) {
				if (handler.getStackInSlot(slot).isEmpty()) {
					if (!world.isRemote) {
						handler.setStackInSlot(slot, stack.copy());
						if (!player.capabilities.isCreativeMode) stack.grow(-1);
					}

					return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
				}
			}
		}

		return super.onItemRightClick(world, player, hand);
	}

	private void onUpdate(ItemStack stack, TinySlot inventory, EntityLivingBase player, boolean forceActive) {
		if (player.getEntityWorld().isRemote) {
			if (forceActive && player instanceof EntityPlayer) ItemComputerHandler.getClient(stack);
		} else {
			NBTTagCompound tag = ItemBase.getTag(stack);
			NeuralComputer neural;

			// Fetch computer
			if (forceActive) {
				neural = ItemComputerHandler.getServer(stack, player, inventory);
				neural.turnOn();
				neural.keepAlive();
			} else {
				neural = ItemComputerHandler.tryGetServer(stack);
				if (neural == null) return;
			}

			boolean dirty = false;

			// Sync computer ID
			int newId = neural.getID();
			if (!tag.hasKey(COMPUTER_ID) || tag.getInteger(COMPUTER_ID) != newId) {
				tag.setInteger(COMPUTER_ID, newId);
				dirty = true;
			}

			// Sync Label
			String newLabel = neural.getLabel();
			String label = stack.hasDisplayName() ? stack.getDisplayName() : null;
			if (!Objects.equal(newLabel, label)) {
				if (newLabel == null || newLabel.isEmpty()) {
					stack.clearCustomName();
				} else {
					stack.setStackDisplayName(newLabel);
				}
				dirty = true;
			}

			// Sync and update peripherals
			short dirtyStatus = tag.getShort(DIRTY);
			if (dirtyStatus != 0) {
				tag.setShort(DIRTY, (short) 0);
				dirty = true;
			}


			if (neural.update(player, stack, dirtyStatus)) {
				dirty = true;
			}

			if (dirty && inventory != null) {
				inventory.markDirty();
			}
		}
	}

	@Nonnull
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound oldCapNbt) {
		return new InvProvider(stack);
	}

	@Override
	public boolean setLabel(@Nonnull ItemStack stack, String name) {
		if (name == null) {
			stack.clearCustomName();
		} else {
			stack.setStackDisplayName(name);
		}
		return true;
	}

	@Override
	public String getAudioTitle(@Nonnull ItemStack stack) {
		return null;
	}

	@Override
	public SoundEvent getAudio(@Nonnull ItemStack stack) {
		return null;
	}

	@Override
	public IMount createDataMount(@Nonnull ItemStack stack, @Nonnull World world) {
		int id = getComputerID(stack);
		if (id >= 0) {
			return ComputerCraft.createSaveDirMount(world, "computer/" + id, (long) ComputerCraft.computerSpaceLimit);
		}

		return null;
	}

	@Override
	public int getComputerID(@Nonnull ItemStack stack) {
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey(COMPUTER_ID)) {
			return stack.getTagCompound().getInteger(COMPUTER_ID);
		} else {
			return -1;
		}
	}

	@Override
	public String getLabel(@Nonnull ItemStack stack) {
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("display", 10)) {
			NBTTagCompound nbttagcompound = stack.getTagCompound().getCompoundTag("display");
			if (nbttagcompound.hasKey("Name", 8)) return nbttagcompound.getString("Name");
		}
		return null;
	}

	@Override
	public ComputerFamily getFamily(@Nonnull ItemStack stack) {
		return ComputerFamily.Advanced;
	}

	@Override
	@Optional.Method(modid = Baubles.MODID)
	public BaubleType getBaubleType(ItemStack stack) {
		return NeuralHelpers.getBaubleType();
	}

	@Override
	@Optional.Method(modid = Baubles.MODID)
	public void onWornTick(ItemStack stack, EntityLivingBase player) {
		if (!(player instanceof EntityPlayer)) return;

		IBaublesItemHandler handler = BaublesApi.getBaublesHandler((EntityPlayer) player);
		for (int slot : NeuralHelpers.getBaubleType().getValidSlots()) {
			ItemStack slotStack = handler.getStackInSlot(slot);
			if (slotStack == stack) {
				onUpdate(stack, new TinySlot.BaublesSlot(stack, handler, slot), player, true);
			}
		}
	}

	@Override
	@Optional.Method(modid = Baubles.MODID)
	public void onEquipped(ItemStack stack, EntityLivingBase player) {
	}

	@Override
	@Optional.Method(modid = Baubles.MODID)
	public void onUnequipped(ItemStack stack, EntityLivingBase player) {
	}

	@Override
	@Optional.Method(modid = Baubles.MODID)
	public boolean canEquip(ItemStack stack, EntityLivingBase player) {
		return true;
	}

	@Override
	@Optional.Method(modid = Baubles.MODID)
	public boolean canUnequip(ItemStack stack, EntityLivingBase player) {
		return true;
	}

	@Override
	@Optional.Method(modid = Baubles.MODID)
	public boolean willAutoSync(ItemStack itemstack, EntityLivingBase player) {
		return false;
	}

	@Override
	@Optional.Method(modid = CCTweaks.ID)
	public boolean hasCustomRom(@Nonnull ItemStack stack) {
		return stack.hasTagCompound() && stack.getTagCompound().hasKey("rom_id", 99);
	}

	@Override
	@Optional.Method(modid = CCTweaks.ID)
	public int getCustomRom(@Nonnull ItemStack stack) {
		return stack.getTagCompound().getInteger("rom_id");
	}

	@Override
	@Optional.Method(modid = CCTweaks.ID)
	public void clearCustomRom(@Nonnull ItemStack stack) {
		NBTTagCompound tag = stack.getTagCompound();
		if (tag != null) {
			tag.removeTag("rom_id");
			tag.removeTag("instanceID");
			tag.removeTag("sessionID");
		}
	}

	@Override
	@Optional.Method(modid = CCTweaks.ID)
	public void setCustomRom(@Nonnull ItemStack stack, int id) {
		NBTTagCompound tag = ItemBase.getTag(stack);
		tag.setInteger("rom_id", id);
		tag.removeTag("instanceID");
		tag.removeTag("sessionID");
	}

	private static class InvProvider implements ICapabilityProvider {
		private final IItemHandler inv;

		private InvProvider(ItemStack stack) {
			this.inv = new NeuralItemHandler(stack);
		}

		@Override
		public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing) {
			return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
			if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
				return (T) inv;
			} else {
				return null;
			}
		}
	}

	//region Armor stuff
	@Override
	public ArmorProperties getProperties(EntityLivingBase entityLivingBase, @Nonnull ItemStack itemStack, DamageSource damageSource, double v, int i) {
		return FAKE_PROPERTIES;
	}

	@Override
	public int getArmorDisplay(EntityPlayer entityPlayer, @Nonnull ItemStack itemStack, int i) {
		return 0;
	}

	@Override
	public void damageArmor(EntityLivingBase entityLivingBase, @Nonnull ItemStack itemStack, DamageSource damageSource, int damage, int slot) {
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> out, ITooltipFlag flag) {
		super.addInformation(stack, world, out, flag);
		out.add(Helpers.translateToLocal(getUnlocalizedName(stack) + ".desc"));

		NBTTagCompound tag = stack.getTagCompound();
		if (flag.isAdvanced()) {
			if (tag != null && tag.hasKey(COMPUTER_ID)) {
				out.add("Computer ID " + tag.getInteger(COMPUTER_ID));
			}
		}

		// Include ROM id (CCTweaks compat)
		if (tag != null && tag.hasKey("rom_id") && Loader.isModLoaded(CCTweaks.ID)) {
			int id = tag.getInteger("rom_id");
			if (flag.isAdvanced() && id >= 0) {
				out.add("Has custom ROM (disk ID: " + id + ")");
			} else {
				out.add("Has custom ROM");
			}
		}
	}

	@Override
	public void onArmorTick(World world, EntityPlayer player, ItemStack stack) {
		super.onArmorTick(world, player, stack);
		onUpdate(stack, new TinySlot.InventorySlot(stack, player.inventory), player, true);
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int um1, boolean um2) {
		super.onUpdate(stack, world, entity, um1, um2);
		if (entity instanceof EntityLivingBase) {
			TinySlot slot;
			if (entity instanceof EntityPlayer) {
				slot = new TinySlot.InventorySlot(stack, ((EntityPlayer) entity).inventory);
			} else {
				slot = new TinySlot(stack);
			}
			onUpdate(stack, slot, (EntityLivingBase) entity, false);
		}
	}

	@Nonnull
	@Override
	@SideOnly(Side.CLIENT)
	public ModelBiped getArmorModel(EntityLivingBase entity, ItemStack stack, EntityEquipmentSlot slot, ModelBiped existing) {
		return ModelInterface.getNormal();
	}

	@Nonnull
	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String existing) {
		return Plethora.RESOURCE_DOMAIN + ":textures/models/neural_interface.png";
	}


	/**
	 * Force armor ticks for entities
	 *
	 * @param event Entity armor ticks
	 */
	@SubscribeEvent
	public void onEntityLivingUpdate(LivingEvent.LivingUpdateEvent event) {
		if (event.getEntityLiving() instanceof EntityPlayer) return;

		TinySlot slot = NeuralHelpers.getSlot(event.getEntityLiving());
		if (slot != null) {
			onUpdate(slot.getStack(), slot, event.getEntityLiving(), true);
		}
	}

	/**
	 * Call the right click event earlier on.
	 *
	 * @param event The event to handle
	 */
	@SubscribeEvent
	public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
		if (!event.isCanceled() && Helpers.onEntityInteract(this, event.getEntityPlayer(), event.getTarget(), event.getHand())) {
			event.setCanceled(true);
		}
	}
	//endregion

	@Override
	public boolean canLoad() {
		return true;
	}

	@Override
	public void preInit() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void registerItems(RegistryEvent.Register<Item> event) {
		event.getRegistry().register(this.setRegistryName(new ResourceLocation(Plethora.RESOURCE_DOMAIN, NAME)));
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
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void clientPreInit() {
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void registerModels(ModelRegistryEvent event) {
		Helpers.setupModel(this, 0, NAME);
	}
	//endregion
}
