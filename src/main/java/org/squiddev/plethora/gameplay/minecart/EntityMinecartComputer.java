package org.squiddev.plethora.gameplay.minecart;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.computer.blocks.BlockCommandComputer;
import dan200.computercraft.shared.computer.blocks.BlockComputer;
import dan200.computercraft.shared.computer.blocks.ComputerState;
import dan200.computercraft.shared.computer.core.*;
import dan200.computercraft.shared.computer.items.ComputerItemFactory;
import dan200.computercraft.shared.computer.items.IComputerItem;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.item.EntityMinecartEmpty;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.minecart.MinecartInteractEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.squiddev.cctweaks.CCTweaks;
import org.squiddev.cctweaks.api.computer.IExtendedServerComputer;
import org.squiddev.plethora.gameplay.GuiHandler;
import org.squiddev.plethora.gameplay.ItemBase;
import org.squiddev.plethora.gameplay.Plethora;
import org.squiddev.plethora.gameplay.registry.Module;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.squiddev.plethora.gameplay.Plethora.ID;

public class EntityMinecartComputer extends EntityMinecart {
	private static final ComputerFamily[] FAMILIES = ComputerFamily.values();

	private static final int INSTANCE_SLOT = 23;
	private static final int SESSION_SLOT = 24;
	private static final int FAMILY_SLOT = 25;

	private int id;
	private boolean on;
	private boolean startOn;

	private int romId = -1;

	@SideOnly(Side.CLIENT)
	private Integer lastClientId;

	public EntityMinecartComputer(World worldIn) {
		super(worldIn);
	}

	public EntityMinecartComputer(EntityMinecartEmpty minecart, int id, String label, ComputerFamily family, int romId) {
		super(minecart.worldObj, minecart.posX, minecart.posY, minecart.posZ);

		rotationPitch = minecart.rotationPitch;
		rotationYaw = minecart.rotationYaw;
		motionX = minecart.motionX;
		motionY = minecart.motionY;
		motionZ = minecart.motionZ;

		setCurrentCartSpeedCapOnRail(minecart.getCurrentCartSpeedCapOnRail());
		setMaxSpeedAirLateral(minecart.getMaxSpeedAirLateral());
		setMaxSpeedAirVertical(minecart.getMaxSpeedAirVertical());
		setDragAir(minecart.getDragAir());

		setRollingAmplitude(minecart.getRollingAmplitude());
		setRollingDirection(minecart.getRollingDirection());

		this.id = id;
		setFamily(family);
		if (label != null) setCustomNameTag(label + " foo");
		this.romId = romId;
	}

	protected void entityInit() {
		super.entityInit();
		dataWatcher.addObject(INSTANCE_SLOT, -1);
		dataWatcher.addObject(SESSION_SLOT, -1);
		dataWatcher.addObject(FAMILY_SLOT, (byte) 0);
	}

	private int getInstanceId() {
		return dataWatcher.getWatchableObjectInt(INSTANCE_SLOT);
	}

	private int getSessionId() {
		return dataWatcher.getWatchableObjectInt(SESSION_SLOT);
	}

	public ComputerFamily getFamily() {
		return FAMILIES[dataWatcher.getWatchableObjectByte(FAMILY_SLOT)];
	}

	private void setFamily(ComputerFamily family) {
		dataWatcher.updateObject(FAMILY_SLOT, (byte) family.ordinal());
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		if (worldObj.isRemote) return;

		ServerComputer computer = getServerComputer();
		computer.setWorld(getEntityWorld());
		computer.setPosition(getPosition());

		if (startOn) {
			startOn = false;
			computer.turnOn();
		}

		computer.keepAlive();

		setCustomNameTag(computer.getLabel());
		on = computer.isOn();
	}


	@Override
	public boolean interactFirst(EntityPlayer player) {
		if (MinecraftForge.EVENT_BUS.post(new MinecartInteractEvent(this, player))) return true;

		if (!this.worldObj.isRemote) {
			if (isUsable(player)) {
				ServerComputer computer = getServerComputer();
				computer.turnOn();
				// computer.sendState(player); // We manually send the state as sometimes it doesn't sync correctly
				GuiHandler.openMinecart(player, player.worldObj, this);
			}
		}

		return true;
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound tag) {
		super.writeEntityToNBT(tag);

		tag.setInteger("computerId", id);
		tag.setByte("family", (byte) getFamily().ordinal());
		tag.setBoolean("on", startOn || on);
		if (romId >= 0) {
			tag.setInteger("rom_id", romId);
		} else {
			tag.removeTag("rom_id");
		}
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound tag) {
		super.readEntityFromNBT(tag);

		id = tag.getInteger("computerId");
		setFamily(FAMILIES[tag.getByte("family")]);
		startOn |= tag.getBoolean("on");
		romId = tag.hasKey("rom_id", 99) ? tag.getInteger("rom_id") : -1;
	}

	@Nonnull
	public ServerComputer getServerComputer() {
		final ServerComputerRegistry manager = ComputerCraft.serverComputerRegistry;
		final int sessionId = manager.getSessionID();
		int instanceId = getInstanceId();

		ServerComputer computer = null;
		if (instanceId >= 0 && getSessionId() == sessionId) computer = manager.get(instanceId);

		if (computer == null) {
			instanceId = manager.getUnusedInstanceID();
			computer = new ServerComputer(worldObj, id, getCustomNameTag(), instanceId, getFamily(), 51, 19);
			computer.setWorld(getEntityWorld());
			computer.setPosition(getPosition());

			if (romId >= 0 && Loader.isModLoaded(CCTweaks.ID)) {
				((IExtendedServerComputer) computer).setCustomRom(romId);
			}

			// TODO: Inject command API where required

			manager.add(instanceId, computer);

			dataWatcher.updateObject(SESSION_SLOT, sessionId);
			dataWatcher.updateObject(INSTANCE_SLOT, instanceId);
		}

		return computer;
	}

	@Nullable
	public ClientComputer getClientComputer() {
		final ClientComputerRegistry manager = ComputerCraft.clientComputerRegistry;
		int instanceId = getInstanceId();

		ClientComputer computer = null;
		if (instanceId >= 0) computer = manager.get(instanceId);
		if (computer == null && (lastClientId == null || lastClientId != instanceId)) {
			// Sometimes the computer doesn't exist, so if we haven't attempted this before, try to fetch it.
			// It is possible that the computer has been deleted on the server but not on the client yet so we
			// store the last computer ID: ensuring that we don't re-create computers.
			computer = new ClientComputer(instanceId);
			manager.add(instanceId, computer);
		}

		if (computer != null) {
			lastClientId = instanceId;
		}

		return computer;
	}

	@Override
	public EnumMinecartType getMinecartType() {
		return EnumMinecartType.RIDEABLE;
	}

	@Override
	public boolean canBeRidden() {
		return false;
	}

	@Override
	@SuppressWarnings("unchecked")
	public IBlockState getDisplayTile() {
		ComputerFamily family = getFamily();
		IComputer computer = worldObj.isRemote ? getClientComputer() : getServerComputer();

		ComputerState state = ComputerState.Off;
		if (computer != null) {
			if (computer.isCursorDisplayed()) {
				state = ComputerState.Blinking;
			} else if (computer.isOn()) {
				state = ComputerState.On;
			}
		}

		IBlockState blockState;
		switch (family) {
			case Advanced:
			case Normal:
			default:
				blockState = ComputerCraft.Blocks.computer
					.getDefaultState()
					.withProperty(BlockComputer.Properties.ADVANCED, family == ComputerFamily.Advanced)
					.withProperty(BlockComputer.Properties.STATE, state);
				break;
			case Command:
				blockState = ComputerCraft.Blocks.commandComputer
					.getDefaultState()
					.withProperty(BlockCommandComputer.Properties.STATE, state);
				break;
		}

		return blockState;
	}

	public void killMinecart(DamageSource source) {
		setDead();

		if (worldObj.getGameRules().getBoolean("doEntityDrops")) {
			entityDropItem(new ItemStack(Items.minecart, 1), 0);

			ItemStack stack = ComputerItemFactory.create(id, getCustomNameTag(), getFamily());
			if (romId >= 0) ItemBase.getTag(stack).setInteger("rom_id", romId);
			entityDropItem(stack, 0);
		}
	}

	public boolean isUsable(EntityPlayer player) {
		if (isDead || player.getDistanceSqToEntity(this) > 64.0D) return false;

		if (getFamily() == ComputerFamily.Command) {
			if (worldObj.isRemote) return true;

			MinecraftServer server = MinecraftServer.getServer();
			if (server == null || !server.isCommandBlockEnabled()) {
				player.addChatComponentMessage(new ChatComponentTranslation("advMode.notEnabled"));
				return false;
			}

			if (ComputerCraft.canPlayerUseCommands(player) && player.capabilities.isCreativeMode) {
				return true;
			} else {
				player.addChatComponentMessage(new ChatComponentTranslation("advMode.notAllowed"));
				return false;
			}
		} else {
			return true;
		}
	}

	public static class MinecartModule extends Module {
		@Override
		public void preInit() {
			EntityRegistry.registerModEntity(EntityMinecartComputer.class, ID + ":minecartComputer", 2, Plethora.instance, 80, 3, true);
			MinecraftForge.EVENT_BUS.register(this);
		}

		@SubscribeEvent
		public void onEntityInteraction(EntityInteractEvent event) {
			EntityPlayer player = event.entityPlayer;

			ItemStack stack = player.getHeldItem();
			if (stack == null) return;

			Item item = stack.getItem();
			if (item != Item.getItemFromBlock(ComputerCraft.Blocks.commandComputer) && item != Item.getItemFromBlock(ComputerCraft.Blocks.computer)) {
				return;
			}

			Entity target = event.target;
			if (!(target instanceof EntityMinecartEmpty)) return;

			EntityMinecartEmpty minecart = (EntityMinecartEmpty) target;
			if (minecart.hasDisplayTile()) return;

			IComputerItem computerItem = (IComputerItem) item;

			int id = computerItem.getComputerID(stack);
			String label = computerItem.getLabel(stack);
			ComputerFamily family = computerItem.getFamily(stack);

			// Copy ROM id (CCTweaks compat)
			NBTTagCompound tag = stack.getTagCompound();
			int romId = tag != null && tag.hasKey("rom_id", 99) ? tag.getInteger("rom_id") : -1;

			player.swingItem();
			if (minecart.worldObj.isRemote) return;

			event.setCanceled(true);
			minecart.setDead();
			minecart.worldObj.spawnEntityInWorld(new EntityMinecartComputer(minecart, id, label, family, romId));

			if (!player.capabilities.isCreativeMode) {
				stack.stackSize--;
				if (stack.stackSize <= 0) player.setCurrentItemOrArmor(0, null);
			}
		}
	}
}
