package org.squiddev.plethora.gameplay.neural;

import com.google.common.base.Objects;
import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.filesystem.IMount;
import dan200.computercraft.api.media.IMedia;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.core.ServerComputer;
import dan200.computercraft.shared.computer.items.IComputerItem;
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
import net.minecraft.util.Vec3;
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
import org.squiddev.plethora.api.Constants;
import org.squiddev.plethora.api.IPeripheralHandler;
import org.squiddev.plethora.gameplay.ItemBase;
import org.squiddev.plethora.gameplay.Plethora;
import org.squiddev.plethora.gameplay.client.ModelInterface;
import org.squiddev.plethora.gameplay.registry.IClientModule;
import org.squiddev.plethora.utils.Helpers;

import java.util.List;

import static org.squiddev.plethora.api.Constants.*;
import static org.squiddev.plethora.gameplay.neural.ItemComputerHandler.*;
import static org.squiddev.plethora.gameplay.neural.NeuralHelpers.BACK;

public class ItemNeuralInterface extends ItemArmor implements IClientModule, ISpecialArmor, IComputerItem, IMedia {
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

		if (entity.getEquipmentInSlot(NeuralHelpers.ARMOR_SLOT) == null && stack.stackSize == 1) {
			if (!player.worldObj.isRemote) {
				entity.setCurrentItemOrArmor(NeuralHelpers.ARMOR_SLOT, stack.copy());

				// Force dropping when killed
				if (entity instanceof EntityLiving) {
					EntityLiving living = (EntityLiving) entity;
					living.setEquipmentDropChance(NeuralHelpers.ARMOR_SLOT, 2);
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

			// Force an update on each peripheral item
			IItemHandler handler = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
			for (int slot = 0; slot < NeuralHelpers.PERIPHERAL_SIZE; slot++) {
				ItemStack module = handler.getStackInSlot(slot);
				if (module != null) {
					IPeripheralHandler peripheralHandler = module.getCapability(Constants.PERIPHERAL_HANDLER_CAPABILITY, null);
					if (peripheralHandler != null) {
						peripheralHandler.update(
							player.worldObj,
							new Vec3(player.posX, player.posY + player.getEyeHeight(), player.posZ),
							player
						);
					}
				}
			}

			// Sync peripherals
			if (tag.getShort(DIRTY) != 0) {
				short dirtyStatus = tag.getShort(DIRTY);
				tag.setShort(DIRTY, (short) 0);
				dirty = true;

				for (int slot = 0; slot < NeuralHelpers.PERIPHERAL_SIZE; slot++) {
					if ((dirtyStatus & (1 << slot)) == 1 << slot) {
						// We skip the "back" slot
						computer.setPeripheral(slot < BACK ? slot : slot + 1, NeuralHelpers.buildPeripheral(handler, slot));
					}
				}

				// If the modules have changed.
				dirtyStatus >>= NeuralHelpers.PERIPHERAL_SIZE;
				if (dirtyStatus != 0) {
					computer.setPeripheral(BACK, NeuralHelpers.buildModules(handler, player));
				}
			}

			if (dirty && inventory != null) inventory.markDirty();
		}
	}

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
	public String getAudioRecordName(ItemStack stack) {
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

	private static class InvProvider implements ICapabilitySerializable<NBTBase> {
		private final ItemStack stack;
		private final IItemHandler inv = new ItemStackHandler(NeuralHelpers.INV_SIZE) {
			@Override
			public ItemStack insertItem(int slot, ItemStack toInsert, boolean simulate) {
				if (toInsert == null || getStackInSlot(slot) != null) return toInsert;

				if (slot < NeuralHelpers.PERIPHERAL_SIZE) {
					if (toInsert.hasCapability(PERIPHERAL_HANDLER_CAPABILITY, null) ||
						toInsert.hasCapability(PERIPHERAL_CAPABILITY, null)) {
						return super.insertItem(slot, toInsert, simulate);
					} else {
						return toInsert;
					}
				} else if (toInsert.hasCapability(MODULE_HANDLER_CAPABILITY, null)) {
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
				tag.setShort(DIRTY, (short) (tag.getShort("dirty") | 1 << slot));
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

		ItemStack stack = NeuralHelpers.getStack(event.entityLiving);
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
