package org.squiddev.plethora.gameplay.minecart;

import com.mojang.authlib.GameProfile;
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
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.item.EntityMinecartEmpty;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.minecart.MinecartInteractEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.squiddev.plethora.api.IPlayerOwnable;
import org.squiddev.plethora.api.vehicle.IVehicleAccess;
import org.squiddev.plethora.api.vehicle.IVehicleUpgradeHandler;
import org.squiddev.plethora.gameplay.GuiHandler;
import org.squiddev.plethora.gameplay.Plethora;
import org.squiddev.plethora.utils.Helpers;
import org.squiddev.plethora.utils.PlayerHelpers;
import org.squiddev.plethora.utils.RenderHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import static org.squiddev.plethora.api.Constants.VEHICLE_UPGRADE_HANDLER_CAPABILITY;

@Mod.EventBusSubscriber(modid = Plethora.ID)
public class EntityMinecartComputer extends EntityMinecart implements IPlayerOwnable {
	private static final ComputerFamily[] FAMILIES = ComputerFamily.values();
	private static final ComputerState[] STATES = ComputerState.values();

	private static final int[] PERIPHERAL_MAPPINGS = new int[]{1, 5, 4, 2};

	private static final AxisAlignedBB[] BOUNDS = new AxisAlignedBB[]{
		// The main block: simply there to avoid reaching "through" the block.
		new AxisAlignedBB(0, 0, -1, 1, 1, 0),

		new AxisAlignedBB(0.125, 1, -0.875, 0.875, 1.125, -0.125),
		new AxisAlignedBB(0.125, 0.125, -1.125, 0.875, 0.875, -1),
		new AxisAlignedBB(0.125, 0.125, 0, 0.875, 0.875, 0.125),
		new AxisAlignedBB(1, 0.125, -0.875, 1.125, 0.875, -0.125),
	};

	private static final DataParameter<Integer> INSTANCE_SLOT = EntityDataManager.createKey(EntityMinecartComputer.class, DataSerializers.VARINT);
	private static final DataParameter<Integer> SESSION_SLOT = EntityDataManager.createKey(EntityMinecartComputer.class, DataSerializers.VARINT);
	private static final DataParameter<Byte> FAMILY_SLOT = EntityDataManager.createKey(EntityMinecartComputer.class, DataSerializers.BYTE);
	private static final DataParameter<Byte> STATE_SLOT = EntityDataManager.createKey(EntityMinecartComputer.class, DataSerializers.BYTE);

	private static final int SLOTS = 4;

	private int id;
	private boolean on;
	private boolean startOn;
	private GameProfile profile;

	/**
	 * The item handler, representing all upgrades in the minecart
	 */
	final UpgradeItemHandler itemHandler = new UpgradeItemHandler(SLOTS);

	/**
	 * All peripherals provided by the items in {@link #itemHandler}.
	 */
	private final IPeripheral[] peripherals = new IPeripheral[SLOTS];

	/**
	 * The minecart access object for each peripheral slot.
	 */
	final VehicleAccess[] accesses = new VehicleAccess[SLOTS];

	@SideOnly(Side.CLIENT)
	private Integer lastClientId;

	public EntityMinecartComputer(World worldIn) {
		super(worldIn);
		setSize(0.98F, 0.98F);

		// Initialise the upgrades
		for (int i = 0; i < SLOTS; i++) accesses[i] = new VehicleAccess(this);
	}

	public EntityMinecartComputer(EntityMinecartEmpty minecart, int id, String label, ComputerFamily family, GameProfile profile) {
		this(minecart.getEntityWorld());

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
		setCustomNameTag(label == null ? "" : label);

		this.profile = profile;
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		dataManager.register(INSTANCE_SLOT, -1);
		dataManager.register(SESSION_SLOT, -1);
		dataManager.register(FAMILY_SLOT, (byte) 0);
		dataManager.register(STATE_SLOT, (byte) 0);
	}

	private int getInstanceId() {
		return dataManager.get(INSTANCE_SLOT);
	}

	private int getSessionId() {
		return dataManager.get(SESSION_SLOT);
	}

	public ComputerFamily getFamily() {
		return FAMILIES[dataManager.get(FAMILY_SLOT)];
	}

	private void setFamily(ComputerFamily family) {
		dataManager.set(FAMILY_SLOT, (byte) family.ordinal());
	}

	public IVehicleAccess getAccess(int slot) {
		return accesses[slot];
	}

	private ComputerState getState() {
		return STATES[dataManager.get(STATE_SLOT)];
	}

	private void setState(ComputerState state) {
		dataManager.set(STATE_SLOT, (byte) state.ordinal());
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		if (getEntityWorld().isRemote) return;

		ServerComputer computer = getServerComputer();
		computer.setWorld(getEntityWorld());
		computer.setPosition(getPosition());

		if (startOn) {
			startOn = false;
			computer.turnOn();
		}

		computer.keepAlive();

		String label = computer.getLabel();
		setCustomNameTag(label == null ? "" : label);

		on = computer.isOn();

		ComputerState state = ComputerState.Off;
		if (computer.isCursorDisplayed()) {
			state = ComputerState.Blinking;
		} else if (computer.isOn()) {
			state = ComputerState.On;
		}
		setState(state);

		WorldServer server = (WorldServer) getEntityWorld();

		int stackDirty = itemHandler.getDirty();
		itemHandler.clearDirty();
		for (int slot = 0; slot < SLOTS; slot++) {
			VehicleAccess access = accesses[slot];

			boolean stackChanged = (stackDirty & (1 << slot)) != 0;
			boolean accessChanged = access.dirty;

			if (stackChanged) {
				accesses[slot].reset();

				IVehicleUpgradeHandler upgrade = itemHandler.getUpgrade(slot);
				IPeripheral peripheral = peripherals[slot] = upgrade == null ? null : upgrade.create(accesses[slot]);
				computer.setPeripheral(PERIPHERAL_MAPPINGS[slot], peripheral);
			}

			{
				IVehicleUpgradeHandler upgrade = itemHandler.getUpgrade(slot);
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
	public boolean processInitialInteract(@Nonnull EntityPlayer player, @Nonnull EnumHand hand) {
		if (MinecraftForge.EVENT_BUS.post(new MinecartInteractEvent(this, player, hand))) return true;

		if (!getEntityWorld().isRemote) {
			Matrix4f trans = getTranslationMatrix(1);
			Vec3d from = new Vec3d(player.posX, player.posY + player.getEyeHeight(), player.posZ);
			Vec3d look = player.getLook(1.0f);
			double reach = 5;
			if (player instanceof EntityPlayerMP) {
				reach = player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue();
			}
			Vec3d to = new Vec3d(from.x + look.x * reach, from.y + look.y * reach, from.z + look.z * reach);

			int slot = getIntersectSlot(from, to, trans);
			if (slot >= 0) {
				ItemStack heldStack = player.getHeldItem(hand);
				ItemStack currentStack = itemHandler.getStackInSlot(slot);
				if (heldStack.isEmpty() && !currentStack.isEmpty()) {
					currentStack = itemHandler.extractItem(slot, 1, false);

					if (!player.capabilities.isCreativeMode) {
						Helpers.spawnItemStack(getEntityWorld(), posX, posY, posZ, currentStack);
					}
				} else if (!heldStack.isEmpty() && currentStack.isEmpty()) {
					ItemStack copy = heldStack.copy();
					copy.setCount(1);

					if (itemHandler.insertItem(slot, copy, false).isEmpty() && !player.capabilities.isCreativeMode) {
						heldStack.grow(-1);
						if (heldStack.isEmpty()) player.setHeldItem(hand, ItemStack.EMPTY);
					}
				}

				return true;
			}

			if (isUsable(player)) {
				ServerComputer computer = getServerComputer();
				computer.turnOn();
				// computer.sendState(player); // We manually send the state as sometimes it doesn't sync correctly
				GuiHandler.openMinecart(player, player.getEntityWorld(), this);
			}
		}

		return true;
	}

	@Override
	public void writeEntityToNBT(@Nonnull NBTTagCompound tag) {
		super.writeEntityToNBT(tag);

		tag.setInteger("computerId", id);
		tag.setByte("family", (byte) getFamily().ordinal());
		tag.setBoolean("on", startOn || on);
		tag.setTag("items", itemHandler.serializeNBT());

		PlayerHelpers.writeProfile(tag, profile);
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound tag) {
		super.readEntityFromNBT(tag);

		id = tag.getInteger("computerId");
		setFamily(FAMILIES[tag.getByte("family")]);
		startOn |= tag.getBoolean("on");
		if (tag.hasKey("items", Constants.NBT.TAG_COMPOUND)) {
			itemHandler.deserializeNBT(tag.getCompoundTag("items"));
		}

		profile = PlayerHelpers.readProfile(tag);
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
			computer = new ServerComputer(getEntityWorld(), id, getCustomNameTag(), instanceId, getFamily(), 51, 19);
			computer.setWorld(getEntityWorld());
			computer.setPosition(getPosition());

			for (int slot = 0; slot < SLOTS; slot++) {
				IVehicleUpgradeHandler upgrade = itemHandler.getUpgrade(slot);
				IPeripheral peripheral = peripherals[slot] = upgrade == null ? null : upgrade.create(accesses[slot]);
				computer.setPeripheral(PERIPHERAL_MAPPINGS[slot], peripheral);
			}

			if (getFamily() == ComputerFamily.Command) {
				computer.addAPI(new CommandAPI(this));
			}

			manager.add(instanceId, computer);

			dataManager.set(SESSION_SLOT, sessionId);
			dataManager.set(INSTANCE_SLOT, instanceId);
		}

		return computer;
	}

	@Nullable
	@SideOnly(Side.CLIENT)
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

	@Nonnull
	@Override
	public Type getType() {
		return Type.RIDEABLE;
	}

	@Override
	public boolean canBeRidden() {
		return false;
	}

	@Nonnull
	@Override
	public IBlockState getDisplayTile() {
		switch (getFamily()) {
			case Advanced:
			case Normal:
			default:
				return ComputerCraft.Blocks.computer
					.getDefaultState()
					.withProperty(BlockComputer.Properties.ADVANCED, getFamily() == ComputerFamily.Advanced)
					.withProperty(BlockComputer.Properties.STATE, getState());
			case Command:
				return ComputerCraft.Blocks.commandComputer
					.getDefaultState()
					.withProperty(BlockCommandComputer.Properties.STATE, getState());
		}
	}

	@Override
	public void killMinecart(DamageSource source) {
		setDead();

		if (getEntityWorld().getGameRules().getBoolean("doEntityDrops")) {
			entityDropItem(new ItemStack(Items.MINECART, 1), 0);

			ItemStack stack = ComputerItemFactory.create(id, getCustomNameTag(), getFamily());
			entityDropItem(stack, 0);

			for (int i = 0; i < SLOTS; i++) {
				ItemStack child = itemHandler.getStackInSlot(i);
				if (!child.isEmpty()) entityDropItem(child, 0);
			}
		}
	}

	public boolean isUsable(EntityPlayer player) {
		if (isDead || player.getDistanceSq(this) > 64.0D) return false;

		if (getFamily() == ComputerFamily.Command) {
			if (getEntityWorld().isRemote) return true;

			MinecraftServer server = player instanceof EntityPlayerMP ? ((EntityPlayerMP) player).server : null;
			if (server == null || !server.isCommandBlockEnabled()) {
				player.sendMessage(new TextComponentTranslation("advMode.notEnabled"));
				return false;
			}

			if (ComputerCraft.canPlayerUseCommands(player) && player.capabilities.isCreativeMode) {
				return true;
			} else {
				player.sendMessage(new TextComponentTranslation("advMode.notAllowed"));
				return false;
			}
		} else {
			return true;
		}
	}

	@Nullable
	@Override
	@SuppressWarnings("unchecked")
	public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
		return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ? (T) itemHandler : super.getCapability(capability, facing);
	}

	@Override
	public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
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

		Vec3d offsetPos = getPos(x, y, z);

		if (offsetPos != null) {
			final double offset = 0.3;
			Vec3d posOff = MinecartHelpers.getPosOffset(this, x, y, z, offset);
			Vec3d negOff = MinecartHelpers.getPosOffset(this, x, y, z, -offset);

			if (posOff == null) posOff = offsetPos;
			if (negOff == null) negOff = offsetPos;

			x = offsetPos.x;
			y = (posOff.y + negOff.y) / 2.0D;
			z = offsetPos.z;
			Vec3d invoff = negOff.add(-posOff.x, -posOff.y, -posOff.z);

			if (invoff.lengthSquared() != 0.0D) {
				invoff = invoff.normalize();
				yaw = (float) (Math.atan2(invoff.z, invoff.x) * 180.0D / Math.PI);
				pitch = (float) (Math.atan(invoff.y) * 73.0D);
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

	private static int getIntersectSlot(Vec3d fromVec, Vec3d toVec, Matrix4f transform) {
		Matrix4f inv = new Matrix4f();
		inv.invert(transform);

		// Convert the vectors into "minecart" space
		Vector4f to = new Vector4f((float) toVec.x, (float) toVec.y, (float) toVec.z, 1);
		inv.transform(to);

		Vector4f from = new Vector4f((float) fromVec.x, (float) fromVec.y, (float) fromVec.z, 1);
		inv.transform(from);

		Vector4f step = new Vector4f();
		step.sub(to, from);
		step.scale(1 / 100.0f);

		// Now ray-trace to find where they intersect with the bounding box.
		for (int offset = 0; offset <= 100; offset++) {
			for (int i = 0; i < BOUNDS.length; i++) {
				AxisAlignedBB bb = BOUNDS[i];
				if (bb.contains(new Vec3d(from.getX(), from.getY(), from.getZ()))) {
					// If we got the actual block itself then pretend nothing happened.
					return i - 1;
				}
			}

			from.add(step);
		}

		return -1;
	}

	@Nullable
	@Override
	public GameProfile getOwningProfile() {
		return profile;
	}

	@SubscribeEvent
	public static void onEntityInteraction(PlayerInteractEvent.EntityInteract event) {
		EntityPlayer player = event.getEntityPlayer();

		ItemStack stack = event.getItemStack();
		if (stack.isEmpty()) return;

		Item item = stack.getItem();
		if (item != Item.getItemFromBlock(ComputerCraft.Blocks.commandComputer) && item != Item.getItemFromBlock(ComputerCraft.Blocks.computer)) {
			return;
		}

		Entity target = event.getTarget();
		if (!(target instanceof EntityMinecartEmpty)) return;

		EntityMinecartEmpty minecart = (EntityMinecartEmpty) target;
		if (minecart.hasDisplayTile()) return;

		IComputerItem computerItem = (IComputerItem) item;

		int id = computerItem.getComputerID(stack);
		String label = computerItem.getLabel(stack);
		ComputerFamily family = computerItem.getFamily(stack);

		player.swingArm(event.getHand());
		if (minecart.getEntityWorld().isRemote) return;

		event.setCanceled(true);
		minecart.setDead();
		minecart.getEntityWorld().spawnEntity(new EntityMinecartComputer(minecart, id, label, family, player.getGameProfile()));

		if (!player.capabilities.isCreativeMode) {
			stack.grow(-1);
			if (stack.isEmpty()) player.setHeldItem(event.getHand(), ItemStack.EMPTY);
		}
	}

	@SubscribeEvent
	public static void startTracking(PlayerEvent.StartTracking event) {
		Entity entity = event.getTarget();
		if (entity instanceof EntityMinecartComputer) {
			EntityMinecartComputer minecart = (EntityMinecartComputer) entity;
			for (int slot = 0; slot < SLOTS; slot++) {
				ItemStack stack = minecart.itemHandler.getStackInSlot(slot);
				NBTTagCompound tag = minecart.accesses[slot].compound;
				if (!stack.isEmpty() || tag != null) {
					MessageMinecartSlot message = new MessageMinecartSlot(minecart, slot);
					message.setStack(stack);
					message.setTag(tag);
					Plethora.network.sendTo(message, (EntityPlayerMP) event.getEntityPlayer());
				}
			}
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public static void drawHighlight(DrawBlockHighlightEvent event) {
		if (event.getTarget().typeOfHit != RayTraceResult.Type.ENTITY) return;
		if (!(event.getTarget().entityHit instanceof EntityMinecartComputer)) return;

		EntityMinecartComputer minecart = (EntityMinecartComputer) event.getTarget().entityHit;

		float partialTicks = event.getPartialTicks();
		GlStateManager.pushMatrix();

		Matrix4f trans = minecart.getTranslationMatrix(partialTicks);

		Matrix4f inv = new Matrix4f();
		inv.invert(trans);

		Entity player = Minecraft.getMinecraft().getRenderViewEntity();

		Vec3d from = player.getPositionEyes(partialTicks);
		Vec3d look = player.getLook(partialTicks);
		double reach = 5;
		if (player instanceof EntityPlayerMP) {
			reach = ((EntityPlayerMP) player).getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue();
		}
		Vec3d to = new Vec3d(from.x + look.x * reach, from.y + look.y * reach, from.z + look.z * reach);

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

	static final class UpgradeItemHandler extends ItemStackHandler {
		private int dirty = 0;
		private final IVehicleUpgradeHandler[] handlers;

		public UpgradeItemHandler(int slots) {
			super(slots);
			this.handlers = new IVehicleUpgradeHandler[6];
		}

		@Override
		protected void onContentsChanged(int slot) {
			dirty |= 1 << slot;
			ItemStack stack = getStackInSlot(slot);
			handlers[slot] = stack.isEmpty() ? null : stack.getCapability(VEHICLE_UPGRADE_HANDLER_CAPABILITY, null);
		}

		@Override
		protected void onLoad() {
			for (int i = 0; i < getSlots(); i++) {
				ItemStack stack = getStackInSlot(i);
				handlers[i] = stack.isEmpty() ? null : stack.getCapability(VEHICLE_UPGRADE_HANDLER_CAPABILITY, null);
			}
		}

		public IVehicleUpgradeHandler getUpgrade(int slot) {
			validateSlotIndex(slot);
			return handlers[slot];
		}

		public int getDirty() {
			return dirty;
		}

		public void clearDirty() {
			dirty = 0;
		}

		@Nonnull
		@Override
		public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
			if (!stack.hasCapability(VEHICLE_UPGRADE_HANDLER_CAPABILITY, null)) {
				return stack;
			}

			return super.insertItem(slot, stack, simulate);
		}

		@Override
		public String toString() {
			return stacks.toString();
		}
	}

	static final class VehicleAccess implements IVehicleAccess {
		private final EntityMinecart minecart;
		NBTTagCompound compound;
		boolean dirty = false;

		private VehicleAccess(EntityMinecart minecart) {
			this.minecart = minecart;
		}

		@Nonnull
		@Override
		public EntityMinecart getVehicle() {
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
