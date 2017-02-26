package org.squiddev.plethora.gameplay.neural;

import baubles.api.BaubleType;
import baubles.api.BaublesApi;
import baubles.api.IBauble;
import baubles.common.Baubles;
import com.google.common.base.Objects;
import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.filesystem.IMount;
import dan200.computercraft.api.media.IMedia;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.items.IComputerItem;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.ISpecialArmor;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.oredict.RecipeSorter;
import org.squiddev.plethora.gameplay.ItemBase;
import org.squiddev.plethora.gameplay.Plethora;
import org.squiddev.plethora.gameplay.client.ModelInterface;
import org.squiddev.plethora.gameplay.registry.IClientModule;
import org.squiddev.plethora.utils.Helpers;
import org.squiddev.plethora.utils.TinySlot;
import org.squiddev.plethora.utils.PlayerHelpers;

import javax.annotation.Nonnull;
import java.util.List;

import static org.squiddev.plethora.gameplay.neural.ItemComputerHandler.COMPUTER_ID;
import static org.squiddev.plethora.gameplay.neural.ItemComputerHandler.DIRTY;
import static org.squiddev.plethora.gameplay.neural.NeuralHelpers.ARMOR_SLOT;

@Optional.Interface(iface = "baubles.api.IBauble", modid = Baubles.MODID)
public class ItemNeuralInterface extends ItemArmor implements IClientModule, ISpecialArmor, IComputerItem, IMedia, IBauble {
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

		if (entity.getItemStackFromSlot(NeuralHelpers.ARMOR_SLOT) == null && stack.stackSize == 1) {
			if (!player.worldObj.isRemote) {
				entity.setItemStackToSlot(NeuralHelpers.ARMOR_SLOT, stack.copy());

				// Force dropping when killed
				if (entity instanceof EntityLiving) {
					EntityLiving living = (EntityLiving) entity;
					living.setDropChance(NeuralHelpers.ARMOR_SLOT, 2);
					living.enablePersistence();
				}

				if (!player.capabilities.isCreativeMode) {
					stack.stackSize = 0;
				}
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	@Nonnull
	public ActionResult<ItemStack> onItemRightClick(@Nonnull ItemStack stack, World world, EntityPlayer player, EnumHand hand) {
		// Check if the entity we've just hit has a stack in the first slot. If so, use that instead.
		RayTraceResult hit = PlayerHelpers.findHitGuess(player);
		Entity entity = hit.entityHit;
		if (hit.typeOfHit == RayTraceResult.Type.ENTITY && !(entity instanceof EntityPlayer) && entity instanceof EntityLivingBase) {
			if (((EntityLivingBase) entity).getItemStackFromSlot(NeuralHelpers.ARMOR_SLOT) == null && stack.stackSize == 1) {
				return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
			}
		}

		return super.onItemRightClick(stack, world, player, hand);
	}

	private void onUpdate(ItemStack stack, IInventory inventory, EntityLivingBase player, boolean forceActive) {
		if (player.worldObj.isRemote) {
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
	public boolean setLabel(ItemStack stack, String name) {
		if (name == null) {
			stack.clearCustomName();
		} else {
			stack.setStackDisplayName(name);
		}
		return true;
	}

	@Override
	public String getAudioTitle(ItemStack stack) {
		return null;
	}

	@Override
	public SoundEvent getAudio(ItemStack stack) {
		return null;
	}

	@Override
	public IMount createDataMount(ItemStack stack, World world) {
		int id = getComputerID(stack);
		if (id >= 0) {
			return ComputerCraft.createSaveDirMount(world, "computer/" + id, (long) ComputerCraft.computerSpaceLimit);
		}

		return null;
	}

	@Override
	public int getComputerID(ItemStack stack) {
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey(COMPUTER_ID)) {
			return stack.getTagCompound().getInteger(COMPUTER_ID);
		} else {
			return -1;
		}
	}

	@Override
	public String getLabel(ItemStack stack) {
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("display", 10)) {
			NBTTagCompound nbttagcompound = stack.getTagCompound().getCompoundTag("display");
			if (nbttagcompound.hasKey("Name", 8)) return nbttagcompound.getString("Name");
		}
		return null;
	}

	@Override
	public ComputerFamily getFamily(ItemStack stack) {
		return ComputerFamily.Advanced;
	}

	@Override
	@Optional.Method(modid = Baubles.MODID)
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		IInventory inventory = BaublesApi.getBaubles(player);
		if (inventory.getStackInSlot(NeuralHelpers.BAUBLES_SLOT) == null) {
			if (!world.isRemote) {
				inventory.setInventorySlotContents(NeuralHelpers.BAUBLES_SLOT, stack.copy());
				if (!player.capabilities.isCreativeMode) stack.stackSize--;
			}

			return stack;
		}

		return super.onItemRightClick(stack, world, player);
	}

	@Override
	@Optional.Method(modid = Baubles.MODID)
	public BaubleType getBaubleType(ItemStack stack) {
		return BaubleType.AMULET;
	}

	@Override
	@Optional.Method(modid = Baubles.MODID)
	public void onWornTick(ItemStack stack, EntityLivingBase player) {
		if (!(player instanceof EntityPlayer)) return;
		onUpdate(stack, BaublesApi.getBaubles((EntityPlayer) player), player, true);
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

	private static class InvProvider implements ICapabilityProvider {
		private final IItemHandler inv;

		private InvProvider(ItemStack stack) {
			this.inv = new NeuralItemHandler(stack);
		}

		@Override
		public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
			return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
			if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
				return (T) inv;
			} else {
				return null;
			}
		}
	}

	//region Armor stuff
	@Override
	public ArmorProperties getProperties(EntityLivingBase entityLivingBase, ItemStack itemStack, DamageSource damageSource, double v, int i) {
		return FAKE_PROPERTIES;
	}

	@Override
	public int getArmorDisplay(EntityPlayer entityPlayer, ItemStack itemStack, int i) {
		return 0;
	}

	@Override
	public void damageArmor(EntityLivingBase entityLivingBase, ItemStack itemStack, DamageSource damageSource, int damage, int slot) {
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List<String> out, boolean additional) {
		super.addInformation(stack, player, out, additional);
		out.add(Helpers.translateToLocal(getUnlocalizedName(stack) + ".desc"));

		if (additional) {
			if (stack.hasTagCompound() && stack.getTagCompound().hasKey(COMPUTER_ID)) {
				out.add("Computer ID " + stack.getTagCompound().getInteger(COMPUTER_ID));
			}
		}
	}

	@Override
	public void onArmorTick(World world, EntityPlayer player, ItemStack stack) {
		super.onArmorTick(world, player, stack);
		onUpdate(stack, player.inventory, player, true);
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int um1, boolean um2) {
		super.onUpdate(stack, world, entity, um1, um2);
		if (entity instanceof EntityLivingBase) {
			IInventory inventory = entity instanceof EntityPlayer ? ((EntityPlayer) entity).inventory : null;
			onUpdate(stack, inventory, (EntityLivingBase) entity, false);
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
			onUpdate(slot.getStack(), slot.getInventory(), event.getEntityLiving(), true);
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
		GameRegistry.register(this, new ResourceLocation(Plethora.RESOURCE_DOMAIN, NAME));
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public void init() {
		RecipeSorter.register(
			Plethora.RESOURCE_DOMAIN + ":neural_interface_crafting",
			CraftingNeuralInterface.class,
			RecipeSorter.Category.SHAPED,
			"after:minecraft:shaped"
		);

		GameRegistry.addRecipe(new CraftingNeuralInterface());
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
		Helpers.setupModel(this, 0, NAME);
	}
	//endregion
}
