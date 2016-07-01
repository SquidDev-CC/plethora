package org.squiddev.plethora.neural;

import com.google.common.base.Objects;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.core.ServerComputer;
import dan200.computercraft.shared.peripheral.PeripheralType;
import dan200.computercraft.shared.peripheral.common.PeripheralItemFactory;
import dan200.computercraft.shared.pocket.items.PocketComputerItemFactory;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.ISpecialArmor;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.squiddev.plethora.ItemBase;
import org.squiddev.plethora.Plethora;
import org.squiddev.plethora.api.module.IModuleItem;
import org.squiddev.plethora.client.ModelInterface;
import org.squiddev.plethora.registry.IClientModule;
import org.squiddev.plethora.utils.Helpers;

import java.util.List;

import static org.squiddev.plethora.neural.ItemComputerHandler.*;
import static org.squiddev.plethora.neural.NeuralHelpers.*;

public class ItemNeuralInterface extends ItemArmor implements IClientModule, ISpecialArmor {
	private static final ArmorMaterial FAKE_ARMOUR = EnumHelper.addArmorMaterial("FAKE_ARMOUR", "iwasbored_fake", -1, new int[]{0, 0, 0, 0}, 0);
	private static final ISpecialArmor.ArmorProperties FAKE_PROPERTIES = new ISpecialArmor.ArmorProperties(0, 0, 0);
	private static final String NAME = "neuralInterface";

	public ItemNeuralInterface() {
		super(FAKE_ARMOUR, 0, 0);

		setUnlocalizedName(Plethora.RESOURCE_DOMAIN + "." + NAME);
		setCreativeTab(Plethora.getCreativeTab());
	}

	@Override
	public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer player, EntityLivingBase entity) {
		if (entity.isChild() || entity instanceof EntityPlayer) return false;

		if (entity.getEquipmentInSlot(ARMOR_SLOT) == null && stack.stackSize == 1) {
			if (!player.worldObj.isRemote) {
				entity.setCurrentItemOrArmor(ARMOR_SLOT, stack.copy());

				// Force dropping when killed
				if (entity instanceof EntityLiving) {
					EntityLiving living = (EntityLiving) entity;
					living.setEquipmentDropChance(ARMOR_SLOT, 2);
					living.enablePersistence();
				}

				stack.stackSize = 0;
			}
			return true;
		} else {
			return false;
		}
	}

	private void onUpdate(ItemStack stack, EntityLivingBase player, boolean forceActive) {
		if (player.worldObj.isRemote) {
			if (forceActive && player instanceof EntityPlayer) ItemComputerHandler.getClient(stack);
		} else {
			NBTTagCompound tag = ItemBase.getTag(stack);
			ServerComputer computer;

			// Fetch computer
			InventoryPlayer inventory = player instanceof EntityPlayer ? ((EntityPlayer) player).inventory : null;
			if (forceActive) {
				computer = ItemComputerHandler.getServer(stack, player, inventory);
				computer.turnOn();
				computer.keepAlive();
			} else {
				computer = ItemComputerHandler.tryGetServer(stack);
				if (computer == null) return;
			}

			boolean dirty = false;

			// Sync entity
			long newMost = player.getUniqueID().getMostSignificantBits();
			long newLeast = player.getUniqueID().getLeastSignificantBits();
			if (tag.getLong(ENTITY_MOST) != newMost || tag.getLong(ENTITY_LEAST) != newLeast) {
				ItemComputerHandler.setEntity(stack, computer, player);
				dirty = true;
			}

			// Sync computer ID
			int newId = computer.getID();
			if (!tag.hasKey(COMPUTER_ID) || tag.getInteger(COMPUTER_ID) != newId) {
				tag.setInteger(COMPUTER_ID, newId);
				dirty = true;
			}

			// Sync Label
			String newLabel = computer.getLabel();
			String label = stack.hasDisplayName() ? stack.getDisplayName() : null;
			if (!Objects.equal(newLabel, label)) {
				if (newLabel == null || newLabel.isEmpty()) {
					stack.clearCustomName();
				} else {
					stack.setStackDisplayName(newLabel);
				}
				dirty = true;
			}

			// Sync peripherals
			if (tag.getByte(DIRTY) != 0) {
				byte dirtyStatus = tag.getByte(DIRTY);
				tag.setByte(DIRTY, (byte) 0);
				dirty = true;

				IItemHandler handler = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
				for (int slot = 0; slot < INV_SIZE; slot++) {
					if ((dirtyStatus & (1 << slot)) == 1 << slot) {
						computer.setPeripheral(slot, NeuralHelpers.buildPeripheral(handler, slot, player));
					}
				}
			}

			if (dirty && inventory != null) inventory.markDirty();
		}
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound oldCapNbt) {
		return new InvProvider(stack);
	}

	private static class InvProvider implements ICapabilitySerializable<NBTBase> {
		private final ItemStack stack;
		private final IItemHandler inv = new ItemStackHandler(INV_SIZE) {
			@Override
			public ItemStack insertItem(int slot, ItemStack toInsert, boolean simulate) {
				if (toInsert != null && toInsert.getItem() instanceof IModuleItem && getStackInSlot(slot) == null) {
					return super.insertItem(slot, toInsert, simulate);
				} else {
					return toInsert;
				}
			}

			@Override
			protected int getStackLimit(int slot, ItemStack stack) {
				return 1;
			}

			@Override
			protected void onContentsChanged(int slot) {
				super.onContentsChanged(slot);

				NBTTagCompound tag = ItemBase.getTag(stack);
				tag.setByte(DIRTY, (byte) (tag.getByte("dirty") | 1 << slot));
			}
		};

		private InvProvider(ItemStack stack) {
			this.stack = stack;
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

		@Override
		public NBTBase serializeNBT() {
			Capability<IItemHandler> capability = CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
			return capability.getStorage().writeNBT(capability, inv, null);
		}

		@Override
		public void deserializeNBT(NBTBase nbt) {
			Capability<IItemHandler> capability = CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
			capability.getStorage().readNBT(capability, inv, null, nbt);
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
		out.add(StatCollector.translateToLocal(getUnlocalizedName(stack) + ".desc"));

		if (additional) {
			if (stack.hasTagCompound() && stack.getTagCompound().hasKey(COMPUTER_ID)) {
				out.add("Computer ID " + stack.getTagCompound().getInteger(COMPUTER_ID));
			}
		}
	}

	@Override
	public void onArmorTick(World world, EntityPlayer player, ItemStack stack) {
		super.onArmorTick(world, player, stack);
		onUpdate(stack, player, true);
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int um1, boolean um2) {
		super.onUpdate(stack, world, entity, um1, um2);
		if (entity instanceof EntityLivingBase) {
			onUpdate(stack, (EntityLivingBase) entity, false);
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


	/**
	 * Force armor ticks for entities
	 *
	 * @param event Entity armor ticks
	 */
	@SubscribeEvent
	public void onEntityLivingUpdate(LivingEvent.LivingUpdateEvent event) {
		if (event.entityLiving instanceof EntityPlayer) return;

		ItemStack stack = getStack(event.entityLiving);
		if (stack != null) {
			onUpdate(stack, event.entityLiving, true);
		}
	}
	//endregion

	@Override
	public boolean canLoad() {
		return true;
	}

	@Override
	public void preInit() {
		GameRegistry.registerItem(this, NAME);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public void init() {
		GameRegistry.addShapedRecipe(new ItemStack(this),
			"  G",
			"IPR",
			" GM",
			'G', new ItemStack(Items.gold_ingot),
			'I', new ItemStack(Items.iron_ingot),
			'R', new ItemStack(Items.redstone),
			'M', PeripheralItemFactory.create(PeripheralType.WiredModem, null, 1),
			'P', PocketComputerItemFactory.create(-1, null, ComputerFamily.Advanced, false)
		);
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
	//endregion
}
