package org.squiddev.plethora.gameplay.minecart;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.computer.blocks.BlockCommandComputer;
import dan200.computercraft.shared.computer.blocks.BlockComputer;
import dan200.computercraft.shared.computer.blocks.ComputerState;
import dan200.computercraft.shared.computer.core.*;
import dan200.computercraft.shared.computer.items.ComputerItemFactory;
import dan200.computercraft.shared.computer.items.IComputerItem;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.item.EntityMinecartEmpty;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.minecart.MinecartInteractEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.squiddev.cctweaks.CCTweaks;
import org.squiddev.cctweaks.api.computer.IExtendedServerComputer;
import org.squiddev.plethora.api.minecart.IMinecartAccess;
import org.squiddev.plethora.api.minecart.IMinecartUpgradeHandler;
import org.squiddev.plethora.gameplay.GuiHandler;
import org.squiddev.plethora.gameplay.ItemBase;
import org.squiddev.plethora.gameplay.Plethora;
import org.squiddev.plethora.gameplay.client.entity.RenderMinecartComputer;
import org.squiddev.plethora.gameplay.registry.IClientModule;
import org.squiddev.plethora.gameplay.registry.Module;
import org.squiddev.plethora.gameplay.registry.Packets;
import org.squiddev.plethora.utils.Helpers;
import org.squiddev.plethora.utils.RenderHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.util.Arrays;

import static org.squiddev.plethora.api.Constants.MINECART_UPGRADE_HANDLER_CAPABILITY;
import static org.squiddev.plethora.gameplay.Plethora.ID;

public class EntityMinecartComputer extends EntityMinecart {
	private static final ComputerFamily[] FAMILIES = ComputerFamily.values();

	private static final int[] PERIPHERAL_MAPPINGS = new int[]{1, 5, 4, 2};

	private static final AxisAlignedBB[] BOUNDS = new AxisAlignedBB[]{
		// The main block: simply there to avoid reaching "through" the block.
		new AxisAlignedBB(0, 0, -1, 1, 1, 0),

		new AxisAlignedBB(0.125, 1, -0.875, 0.875, 1.125, -0.125),
		new AxisAlignedBB(0.125, 0.125, -1.125, 0.875, 0.875, -1),
		new AxisAlignedBB(0.125, 0.125, 0, 0.875, 0.875, 0.125),
		new AxisAlignedBB(1, 0.125, -0.875, 1.125, 0.875, -0.125),
	};

	private static final int INSTANCE_SLOT = 23;
	private static final int SESSION_SLOT = 24;
	private static final int FAMILY_SLOT = 25;

	private static final int SLOTS = 4;

	private int id;
	private boolean on;
	private boolean startOn;

	/**
	 * The item handler, representing all upgrades in the minecart
	 */
	private final UpgradeItemHandler itemHandler = new UpgradeItemHandler(SLOTS);

	/**
	 * All peripherals provided by the items in {@link #itemHandler}.
	 */
	private final IPeripheral[] peripherals = new IPeripheral[SLOTS];

	/**
	 * The minecart access object for each peripheral slot.
	 */
	private final MinecartAccess[] accesses = new MinecartAccess[SLOTS];

	private int romId = -1;

	@SideOnly(Side.CLIENT)
	private Integer lastClientId;

	public EntityMinecartComputer(World worldIn) {
		super(worldIn);
		setSize(0.98F, 0.98F);

		// Initialise the upgrades
		for (int i = 0; i < SLOTS; i++) accesses[i] = new MinecartAccess(this);
	}

	public EntityMinecartComputer(EntityMinecartEmpty minecart, int id, String label, ComputerFamily family, int romId) {
		this(minecart.worldObj);

		setPositionAndRotation(minecart.posX, minecart.posY, minecart.posZ, minecart.rotationYaw, minecart.rotationPitch);
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

	public IMinecartAccess getAccess(int slot) {
		return accesses[slot];
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

		WorldServer server = (WorldServer) getEntityWorld();

		int stackDirty = itemHandler.getDirty();
		itemHandler.clearDirty();
		for (int slot = 0; slot < SLOTS; slot++) {
			MinecartAccess access = accesses[slot];

			boolean stackChanged = (stackDirty & (1 << slot)) != 0;
			boolean accessChanged = access.dirty;

			if (stackChanged) {
				accesses[slot].reset();

				IMinecartUpgradeHandler upgrade = itemHandler.getUpgrade(slot);
				IPeripheral peripheral = peripherals[slot] = upgrade == null ? null : upgrade.create(accesses[slot]);
				computer.setPeripheral(PERIPHERAL_MAPPINGS[slot], peripheral);
			}

			{
				IMinecartUpgradeHandler upgrade = itemHandler.getUpgrade(slot);
				if (upgrade != null) {
					upgrade.update(access, peripherals[slot]);
					accessChanged |= access.dirty;
				}
			}

			access.dirty = false;

			if (stackChanged || accessChanged) {
				// Gather the appropriate data for this packet
				MessageMinecartSlot message = new MessageMinecartSlot(this, slot);
				message.setTag(access.compound);
				if (stackChanged) message.setStack(itemHandler.getStackInSlot(slot));

				// And send it to all players.
				for (EntityPlayer player : server.getEntityTracker().getTrackingPlayers(this)) {
					Plethora.network.sendTo(message, (EntityPlayerMP) player);
				}
			}
		}
	}

	@Override
	public boolean interactFirst(EntityPlayer player) {
		if (MinecraftForge.EVENT_BUS.post(new MinecartInteractEvent(this, player))) return true;

		if (!worldObj.isRemote) {
			Matrix4f trans = getTranslationMatrix(1);
			Vec3 from = new Vec3(player.posX, player.posY + player.getEyeHeight(), player.posZ);
			Vec3 look = player.getLook(1.0f);
			double reach = 5;
			if (player instanceof EntityPlayerMP) {
				reach = ((EntityPlayerMP) player).theItemInWorldManager.getBlockReachDistance();
			}
			Vec3 to = new Vec3(from.xCoord + look.xCoord * reach, from.yCoord + look.yCoord * reach, from.zCoord + look.zCoord * reach);

			int slot = getIntersectSlot(from, to, trans);
			if (slot >= 0) {
				ItemStack heldStack = player.getHeldItem();
				ItemStack currentStack = itemHandler.getStackInSlot(slot);
				if (heldStack == null && currentStack != null) {
					currentStack = itemHandler.extractItem(slot, 1, false);

					if (!player.capabilities.isCreativeMode) {
						Helpers.spawnItemStack(worldObj, posX, posY, posZ, currentStack);
					}
				} else if (heldStack != null && currentStack == null) {
					ItemStack copy = heldStack.copy();
					copy.stackSize = 1;

					if (itemHandler.insertItem(slot, copy, false) == null && !player.capabilities.isCreativeMode && --heldStack.stackSize <= 0) {
						player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
					}
				}

				return true;
			}

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
		tag.setTag("items", itemHandler.serializeNBT());
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound tag) {
		super.readEntityFromNBT(tag);

		id = tag.getInteger("computerId");
		setFamily(FAMILIES[tag.getByte("family")]);
		startOn |= tag.getBoolean("on");
		romId = tag.hasKey("rom_id", 99) ? tag.getInteger("rom_id") : -1;
		if (tag.hasKey("items", Constants.NBT.TAG_COMPOUND)) {
			itemHandler.deserializeNBT(tag.getCompoundTag("items"));
		}
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

			for (int slot = 0; slot < SLOTS; slot++) {
				IMinecartUpgradeHandler upgrade = itemHandler.getUpgrade(slot);
				IPeripheral peripheral = peripherals[slot] = upgrade == null ? null : upgrade.create(accesses[slot]);
				computer.setPeripheral(PERIPHERAL_MAPPINGS[slot], peripheral);
			}

			if (getFamily() == ComputerFamily.Command) {
				computer.addAPI(new CommandAPI(this));
			}

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

			for (int i = 0; i < SLOTS; i++) {
				ItemStack child = itemHandler.getStackInSlot(i);
				if (child != null) entityDropItem(child, 0);
			}
		}
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox() {
		return super.getCollisionBoundingBox();
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

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getCapability(net.minecraftforge.common.capabilities.Capability<T> capability, net.minecraft.util.EnumFacing facing) {
		return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ? (T) itemHandler : super.getCapability(capability, facing);
	}

	@Override
	public boolean hasCapability(net.minecraftforge.common.capabilities.Capability<?> capability, net.minecraft.util.EnumFacing facing) {
		return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
	}

	private Matrix4f getTranslationMatrix(float partialTicks) {
		// Tiny bit of random offset
		long id = (long) getEntityId() * 493286711L;
		id = id * id * 4392167121L + id * 98761L;
		float ox = (((float) (id >> 16 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
		float oy = (((float) (id >> 20 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
		float oz = (((float) (id >> 24 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;

		double x = lastTickPosX + (posX - lastTickPosX) * partialTicks;
		double y = lastTickPosY + (posY - lastTickPosY) * partialTicks;
		double z = lastTickPosZ + (posZ - lastTickPosZ) * partialTicks;
		float pitch = prevRotationPitch + (rotationPitch - prevRotationPitch) * partialTicks;
		float yaw = prevRotationYaw + (rotationYaw - prevRotationYaw) * partialTicks;

		Vec3 offsetPos = func_70489_a(x, y, z);

		if (offsetPos != null) {
			final double offset = 0.3;
			Vec3 posOff = MinecartHelpers.getPosOffset(this, x, y, z, offset);
			Vec3 negOff = MinecartHelpers.getPosOffset(this, x, y, z, -offset);

			if (posOff == null) posOff = offsetPos;
			if (negOff == null) negOff = offsetPos;

			x = offsetPos.xCoord;
			y = (posOff.yCoord + negOff.yCoord) / 2.0D;
			z = offsetPos.zCoord;
			Vec3 invoff = negOff.addVector(-posOff.xCoord, -posOff.yCoord, -posOff.zCoord);

			if (invoff.lengthVector() != 0.0D) {
				invoff = invoff.normalize();
				yaw = (float) (Math.atan2(invoff.zCoord, invoff.xCoord) * 180.0D / Math.PI);
				pitch = (float) (Math.atan(invoff.yCoord) * 73.0D);
			}
		}

		// Set up the translation matrix.
		// This could probably be "inlined" but it'll do.
		Matrix4f temp = new Matrix4f();
		Matrix4f trans = new Matrix4f();
		trans.setIdentity();

		temp.setIdentity();
		temp.setTranslation(new Vector3f((float) x + ox, (float) y + 0.375f + oy, (float) z + oz));
		trans.mul(temp);

		temp.setIdentity();
		temp.rotY((float) Math.toRadians(180 - yaw));
		trans.mul(temp);

		temp.setIdentity();
		temp.rotZ((float) Math.toRadians(-pitch));
		trans.mul(temp);

		float amplitude = (float) getRollingAmplitude() - partialTicks;
		float roll = getDamage() - partialTicks;

		if (roll < 0.0F) roll = 0.0F;

		if (amplitude > 0.0F) {
			temp.setIdentity();
			temp.rotX((float) Math.toRadians(MathHelper.sin(amplitude) * amplitude * roll / 10.0F * (float) getRollingDirection()));
			trans.mul(temp);
		}

		trans.setScale(0.75F);

		int offset = getDisplayTileOffset();
		temp.setIdentity();
		temp.setTranslation(new Vector3f(-0.5F, (float) (offset - 8) / 16.0F, 0.5F));
		trans.mul(temp);

		return trans;
	}

	private static int getIntersectSlot(Vec3 fromVec, Vec3 toVec, Matrix4f transform) {
		Matrix4f inv = new Matrix4f();
		inv.invert(transform);

		// Convert the vectors into "minecart" space
		Vector4f to = new Vector4f((float) toVec.xCoord, (float) toVec.yCoord, (float) toVec.zCoord, 1);
		inv.transform(to);

		Vector4f from = new Vector4f((float) fromVec.xCoord, (float) fromVec.yCoord, (float) fromVec.zCoord, 1);
		inv.transform(from);

		Vector4f step = new Vector4f();
		step.sub(to, from);
		step.scale(1 / 100.0f);

		// Now ray-trace to find where they intersect with the bounding box.
		for (int offset = 0; offset <= 100; offset++) {
			for (int i = 0; i < BOUNDS.length; i++) {
				AxisAlignedBB bb = BOUNDS[i];
				if (bb.isVecInside(new Vec3(from.getX(), from.getY(), from.getZ()))) {
					// If we got the actual block itself then pretend nothing happened.
					return i - 1;
				}
			}

			from.add(step);
		}

		return -1;
	}

	public static class MinecartModule extends Module implements IClientModule, IMessageHandler<MessageMinecartSlot, IMessage> {
		@Override
		public void preInit() {
			EntityRegistry.registerModEntity(EntityMinecartComputer.class, ID + ":minecartComputer", 2, Plethora.instance, 80, 3, true);
			MinecraftForge.EVENT_BUS.register(this);
			Plethora.network.registerMessage(this, MessageMinecartSlot.class, Packets.MINECART_MESSAGE, Side.CLIENT);
		}

		@Override
		@SideOnly(Side.CLIENT)
		public void clientInit() {
		}

		@Override
		@SideOnly(Side.CLIENT)
		public void clientPreInit() {
			RenderingRegistry.registerEntityRenderingHandler(EntityMinecartComputer.class, new IRenderFactory<EntityMinecartComputer>() {
				@Override
				public Render<EntityMinecartComputer> createRenderFor(RenderManager renderManager) {
					return new RenderMinecartComputer(renderManager);
				}
			});
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

		@SubscribeEvent
		public void startTracking(PlayerEvent.StartTracking event) {
			Entity entity = event.target;
			if (entity instanceof EntityMinecartComputer) {
				EntityMinecartComputer minecart = (EntityMinecartComputer) entity;
				IItemHandler handler = minecart.itemHandler;
				for (int slot = 0; slot < SLOTS; slot++) {
					ItemStack stack = handler.getStackInSlot(slot);
					NBTTagCompound tag = minecart.accesses[slot].compound;
					if (stack != null || tag != null) {
						MessageMinecartSlot message = new MessageMinecartSlot(minecart, slot);
						message.setStack(stack);
						message.setTag(tag);
						Plethora.network.sendTo(message, (EntityPlayerMP) event.entityPlayer);
					}
				}
			}
		}

		@SubscribeEvent
		@SideOnly(Side.CLIENT)
		public void drawHighlight(DrawBlockHighlightEvent event) {
			if (event.target.typeOfHit != MovingObjectPosition.MovingObjectType.ENTITY) return;
			if (!(event.target.entityHit instanceof EntityMinecartComputer)) return;

			EntityMinecartComputer minecart = (EntityMinecartComputer) event.target.entityHit;

			float partialTicks = event.partialTicks;
			GlStateManager.pushMatrix();

			Matrix4f trans = minecart.getTranslationMatrix(partialTicks);

			Matrix4f inv = new Matrix4f();
			inv.invert(trans);

			Entity player = Minecraft.getMinecraft().getRenderViewEntity();

			Vec3 from = player.getPositionEyes(partialTicks);
			Vec3 look = player.getLook(partialTicks);
			double reach = 5;
			if (player instanceof EntityPlayerMP) {
				reach = ((EntityPlayerMP) player).theItemInWorldManager.getBlockReachDistance();
			}
			Vec3 to = new Vec3(from.xCoord + look.xCoord * reach, from.yCoord + look.yCoord * reach, from.zCoord + look.zCoord * reach);

			int slot = getIntersectSlot(from, to, trans);

			// Shift everything back to be relative to the player
			GlStateManager.translate(
				-(player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks),
				-(player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks),
				-(player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks)
			);

			ForgeHooksClient.multiplyCurrentGlMatrix(trans);

			if (slot >= 0) {
				RenderHelper.renderBoundingBox(BOUNDS[slot + 1]);
			}

			GlStateManager.popMatrix();

			event.setCanceled(true);
		}

		@Override
		@SideOnly(Side.CLIENT)
		public IMessage onMessage(final MessageMinecartSlot message, final MessageContext ctx) {
			// We schedule this to run on the main thread so the entity is actually
			// loaded by this point.
			// After all, this is what the S0EPacketSpawnObject packet does.
			Minecraft mc = Minecraft.getMinecraft();
			if (!mc.isCallingFromMinecraftThread()) {
				mc.addScheduledTask(new Runnable() {
					@Override
					public void run() {
						onMessage(message, ctx);
					}
				});
			}

			World world = Minecraft.getMinecraft().theWorld;
			if (world == null) return null;

			Entity entity = world.getEntityByID(message.entityId);
			if (entity instanceof EntityMinecartComputer) {
				EntityMinecartComputer computer = ((EntityMinecartComputer) entity);

				if (message.hasStack()) computer.itemHandler.setStackInSlot(message.slot, message.stack);
				if (message.hasTag()) computer.accesses[message.slot].compound = message.tag;
			}

			return null;
		}
	}

	private static final class UpgradeItemHandler extends ItemStackHandler {
		private int dirty = 0;
		private final IMinecartUpgradeHandler[] handlers;

		public UpgradeItemHandler(int slots) {
			super(slots);
			this.handlers = new IMinecartUpgradeHandler[6];
		}

		@Override
		protected void onContentsChanged(int slot) {
			dirty |= 1 << slot;
			ItemStack stack = getStackInSlot(slot);
			handlers[slot] = stack == null ? null : stack.getCapability(MINECART_UPGRADE_HANDLER_CAPABILITY, null);
		}

		@Override
		protected void onLoad() {
			for (int i = 0; i < getSlots(); i++) {
				ItemStack stack = getStackInSlot(i);
				handlers[i] = stack == null ? null : stack.getCapability(MINECART_UPGRADE_HANDLER_CAPABILITY, null);
			}
		}

		public IMinecartUpgradeHandler getUpgrade(int slot) {
			validateSlotIndex(slot);
			return handlers[slot];
		}

		public int getDirty() {
			return dirty;
		}

		public void clearDirty() {
			dirty = 0;
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
			if (!stack.hasCapability(MINECART_UPGRADE_HANDLER_CAPABILITY, null)) {
				return stack;
			}

			return super.insertItem(slot, stack, simulate);
		}

		@Override
		public String toString() {
			return Arrays.toString(stacks);
		}
	}

	private static final class MinecartAccess implements IMinecartAccess {
		private final EntityMinecart minecart;
		protected NBTTagCompound compound;
		protected boolean dirty = false;

		private MinecartAccess(EntityMinecart minecart) {
			this.minecart = minecart;
		}

		@Nonnull
		@Override
		public EntityMinecart getMinecart() {
			return minecart;
		}

		@Nonnull
		@Override
		public NBTTagCompound getData() {
			NBTTagCompound tag = compound;
			if (tag == null) tag = compound = new NBTTagCompound();
			return tag;
		}

		@Override
		public void markDataDirty() {
			dirty = true;
		}

		public void reset() {
			compound = null;
			dirty = false;
		}
	}
}
