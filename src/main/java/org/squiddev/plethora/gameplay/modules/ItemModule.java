package org.squiddev.plethora.gameplay.modules;

import com.mojang.authlib.GameProfile;
import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.lua.LuaException;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import org.squiddev.plethora.api.Constants;
import org.squiddev.plethora.api.IPlayerOwnable;
import org.squiddev.plethora.api.PlethoraAPI;
import org.squiddev.plethora.api.method.IContextBuilder;
import org.squiddev.plethora.api.module.AbstractModuleHandler;
import org.squiddev.plethora.api.module.IModuleAccess;
import org.squiddev.plethora.api.module.IModuleRegistry;
import org.squiddev.plethora.api.reference.ConstantReference;
import org.squiddev.plethora.api.reference.EntityReference;
import org.squiddev.plethora.api.vehicle.IVehicleUpgradeHandler;
import org.squiddev.plethora.gameplay.ConfigGameplay;
import org.squiddev.plethora.gameplay.ItemBase;
import org.squiddev.plethora.gameplay.Plethora;
import org.squiddev.plethora.gameplay.client.RenderHelpers;
import org.squiddev.plethora.gameplay.client.entity.RenderLaser;
import org.squiddev.plethora.gameplay.modules.glasses.*;
import org.squiddev.plethora.utils.Helpers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix4f;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.squiddev.plethora.gameplay.ConfigGameplay.Kinetic.launchMax;
import static org.squiddev.plethora.gameplay.ConfigGameplay.Laser.maximumPotency;
import static org.squiddev.plethora.gameplay.ConfigGameplay.Laser.minimumPotency;
import static org.squiddev.plethora.gameplay.modules.PlethoraModules.*;
import static org.squiddev.plethora.gameplay.registry.Packets.*;

public final class ItemModule extends ItemBase {
	private static final int MAX_TICKS = 72000;
	private static final int USE_TICKS = 30;

	/**
	 * We multiply the gaussian by this number.
	 * This is the change in velocity for each axis after normalisation.
	 *
	 * @see net.minecraft.entity.projectile.EntityThrowable#setThrowableHeading(double, double, double, float, float)
	 */
	private static final float LASER_MAX_SPREAD = (float) (0.1 / 0.007499999832361937);

	public ItemModule() {
		super("module");
		setHasSubtypes(true);
	}

	@Nonnull
	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return super.getUnlocalizedName() + ".module_" + getName(stack.getItemDamage());
	}

	@Override
	public void getSubItems(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> out) {
		if (!isInCreativeTab(tab)) return;
		for (int i = 0; i < MODULES; i++) {
			out.add(new ItemStack(this, 1, i));
		}
	}

	@Nonnull
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, @Nonnull EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);
		switch (stack.getItemDamage()) {
			case INTROSPECTION_ID:
			case CHAT_ID:
				if (!world.isRemote) {
					if (player.isSneaking() && !player.getGameProfile().getName().startsWith("[")) {
						UUID id = player.getGameProfile().getId();
						if (id != null) {
							NBTTagCompound compound = getTag(stack);
							compound.setLong("id_lower", id.getLeastSignificantBits());
							compound.setLong("id_upper", id.getMostSignificantBits());
							compound.setString("bound_name", player.getName());
						}
					} else {
						if (stack.getItemDamage() == INTROSPECTION_ID) {
							player.displayGUIChest(player.getInventoryEnderChest());
						}
					}
				}

				return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
			case LASER_ID:
			case KINETIC_ID:
				player.setActiveHand(hand);
				return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
			default:
				return ActionResult.newResult(EnumActionResult.PASS, stack);
		}
	}

	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World world, EntityLivingBase player, int remaining) {
		if (world.isRemote) return;

		// Get the number of ticks the laser has been used for
		// We use a float we'll have to cast it later anyway
		float ticks = MAX_TICKS - remaining;
		if (ticks > USE_TICKS) ticks = USE_TICKS;
		if (ticks < 0) ticks = 0;

		switch (stack.getItemDamage()) {
			case LASER_ID: {
				double potency = (ticks / USE_TICKS) * (maximumPotency - minimumPotency) + minimumPotency;
				double inaccuracy = (USE_TICKS - ticks) / USE_TICKS * LASER_MAX_SPREAD;

				world.spawnEntity(new EntityLaser(world, player, (float) inaccuracy, (float) potency));
				break;
			}
			case KINETIC_ID: {
				launch(player, player.rotationYaw, player.rotationPitch, (ticks / USE_TICKS) * launchMax);
				break;
			}
			default:
				super.onPlayerStoppedUsing(stack, world, player, remaining);
		}
	}

	@Nonnull
	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		if (stack.getItemDamage() == LASER_ID || stack.getItemDamage() == KINETIC_ID) {
			return EnumAction.BOW;
		} else {
			return super.getItemUseAction(stack);
		}
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		if (stack.getItemDamage() == LASER_ID || stack.getItemDamage() == KINETIC_ID) {
			return MAX_TICKS;
		} else {
			return super.getMaxItemUseDuration(stack);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World world, List<String> out, ITooltipFlag flag) {
		super.addInformation(stack, world, out, flag);

		String entity = getEntityName(stack);
		if (entity != null) {
			out.add("Bound to " + entity);
		}
	}

	//region Registering
	@Override
	@SideOnly(Side.CLIENT)
	public void clientPreInit() {
		RenderingRegistry.registerEntityRenderingHandler(EntityLaser.class, RenderLaser::new);
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	@Override
	public void registerModels(ModelRegistryEvent event) {
		for (int i = 0; i < MODULES; i++) {
			Helpers.setupModel(this, i, "module_" + getName(i));
		}
	}

	@Override
	public void preInit() {
		super.preInit();
		EntityRegistry.registerModEntity(new ResourceLocation(Plethora.ID, "laser"), EntityLaser.class, Plethora.ID + ":laser", 0, Plethora.instance, 64, 10, true);


		IModuleRegistry registry = PlethoraAPI.instance().moduleRegistry();
		for (int id : TURTLE_MODULES) {
			ItemStack stack = new ItemStack(this, 1, id);
			registry.registerTurtleUpgrade(stack);
		}

		{
			ItemStack stack = new ItemStack(this, 1, KINETIC_ID);
			ComputerCraftAPI.registerTurtleUpgrade(new TurtleUpgradeKinetic(
				stack,
				stack.getCapability(Constants.MODULE_HANDLER_CAPABILITY, null),
				stack.getUnlocalizedName() + ".adjective")
			);
		}

		for (int id : POCKET_MODULES) {
			ItemStack stack = new ItemStack(this, 1, id);
			registry.registerPocketUpgrade(stack);
		}

		MinecraftForge.EVENT_BUS.register(this);
		MinecraftForge.EVENT_BUS.register(new CanvasHandler());

		Plethora.network.registerMessage(new MessageCanvasAdd.Handler(), MessageCanvasAdd.class, CANVAS_ADD_MESSAGE, Side.CLIENT);
		Plethora.network.registerMessage(new MessageCanvasRemove.Handler(), MessageCanvasRemove.class, CANVAS_REMOVE_MESSAGE, Side.CLIENT);
		Plethora.network.registerMessage(new MessageCanvasUpdate.Handler(), MessageCanvasUpdate.class, CANVAS_UPDATE_MESSAGE, Side.CLIENT);
	}

	//endregion

	@Nonnull
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound tag) {
		return new ItemModuleHandler(stack);
	}

	private static final class ItemModuleHandler extends AbstractModuleHandler implements ICapabilityProvider {
		private final ItemStack stack;
		private ResourceLocation moduleId;
		private IVehicleUpgradeHandler vehicle;

		private ItemModuleHandler(ItemStack stack) {
			this.stack = stack;
		}

		@Override
		public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing enumFacing) {
			if (stack.getItemDamage() >= MODULES) return false;

			if (capability == Constants.MODULE_HANDLER_CAPABILITY) return true;
			if (capability == Constants.VEHICLE_UPGRADE_HANDLER_CAPABILITY) {
				return vehicle != null || Arrays.binarySearch(VEHICLE_MODULES, stack.getItemDamage()) != -1;
			}

			return false;
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing enumFacing) {
			if (stack.getItemDamage() >= MODULES) return null;

			if (capability == Constants.MODULE_HANDLER_CAPABILITY) return (T) this;

			if (capability == Constants.VEHICLE_UPGRADE_HANDLER_CAPABILITY) {
				if (vehicle != null) {
					return (T) vehicle;
				} else if (Arrays.binarySearch(VEHICLE_MODULES, stack.getItemDamage()) != -1) {
					return (T) (vehicle = PlethoraAPI.instance().moduleRegistry().toVehicleUpgrade(this));
				} else {
					return null;
				}
			}

			return null;
		}

		@Nonnull
		@Override
		public ResourceLocation getModule() {
			// Cache the ID
			ResourceLocation id = moduleId;
			if (id == null) {
				return this.moduleId = new ResourceLocation(Plethora.RESOURCE_DOMAIN, getName(stack.getItemDamage()));
			} else {
				return id;
			}
		}

		@Override
		public void getAdditionalContext(@Nonnull IModuleAccess access, @Nonnull IContextBuilder builder) {
			String moduleKey = moduleId.toString();
			Entity entity = getEntity(stack);
			if (entity != null) builder.addContext(moduleKey, entity, new EntityReference<>(entity));

			GameProfile profile = getProfile(stack);
			if (profile != null) builder.addContext(moduleKey, new ConstantOwnable(profile));

			if (stack.getItemDamage() == CHAT_ID) {
				// Add a chat listener if we've got an entity (and are a chat module).
				Object owner = access.getOwner();
				Entity ownerEntity = owner instanceof Entity ? (Entity) owner : entity;

				ChatListener.Listener listener = new ChatListener.Listener(access, ownerEntity);
				if (ownerEntity != null) builder.addContext(moduleKey, listener);
				builder.addAttachable(listener);
			} else if (stack.getItemDamage() == GLASSES_ID) {
				// Add a chat listener if we've got an entity (and are a chat module).
				Object owner = access.getOwner();
				EntityPlayerMP ownerEntity = owner instanceof EntityPlayerMP ? (EntityPlayerMP) owner : null;

				if (ownerEntity != null && !(ownerEntity instanceof FakePlayer)) {
					GlassesInstance glasses = new GlassesInstance(access, ownerEntity);
					builder.addContext(moduleKey, glasses);
					builder.addAttachable(glasses);
				}
			} else if (stack.getItemDamage() == CHAT_CREATIVE_ID) {
				ChatListener.Listener listener = new ChatListener.CreativeListener(access);
				builder.addContext(moduleKey, listener);
				builder.addAttachable(listener);
			}
		}

		@Nonnull
		@Override
		public Pair<IBakedModel, Matrix4f> getModel(float delta) {
			Matrix4f matrix = new Matrix4f();
			matrix.setIdentity();

			// We flip the laser so it points forwards on turtles.
			if (stack.getItemDamage() == LASER_ID) {
				delta = (float) ((delta + Math.PI) % (2 * Math.PI));
			}

			matrix.setRotation(new AxisAngle4f(0f, 1f, 0f, delta));

			return Pair.of(
				RenderHelpers.getMesher().getItemModel(stack),
				matrix
			);
		}
	}

	private static GameProfile getProfile(ItemStack stack) {
		NBTTagCompound tag = stack.getTagCompound();
		if (tag != null && tag.hasKey("id_lower", 99)) {
			return new GameProfile(new UUID(tag.getLong("id_upper"), tag.getLong("id_lower")), tag.getString("bound_name"));
		} else {
			return null;
		}
	}

	private static Entity getEntity(ItemStack stack) {
		NBTTagCompound tag = stack.getTagCompound();
		if (tag != null && tag.hasKey("id_lower", 99)) {
			FMLCommonHandler handler = FMLCommonHandler.instance();
			if (handler == null) return null;

			MinecraftServer server = handler.getMinecraftServerInstance();
			if (server == null) return null;
			return server.getEntityFromUuid(new UUID(tag.getLong("id_upper"), tag.getLong("id_lower")));
		} else {
			return null;
		}
	}

	private static String getEntityName(ItemStack stack) {
		NBTTagCompound tag = stack.getTagCompound();
		if (tag != null && tag.hasKey("bound_name", 8)) {
			return tag.getString("bound_name");
		} else {
			return null;
		}
	}

	private static final double TERMINAL_VELOCITY = -2;

	public static void launch(Entity entity, float yaw, float pitch, float power) {
		float motionX = -MathHelper.sin(yaw / 180.0f * (float) Math.PI) * MathHelper.cos(pitch / 180.0f * (float) Math.PI);
		float motionZ = MathHelper.cos(yaw / 180.0f * (float) Math.PI) * MathHelper.cos(pitch / 180.0f * (float) Math.PI);
		float motionY = -MathHelper.sin(pitch / 180.0f * (float) Math.PI);

		power /= MathHelper.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
		if (entity instanceof EntityLivingBase && ((EntityLivingBase) entity).isElytraFlying()) {
			power *= ConfigGameplay.Kinetic.launchElytraScale;
		}

		entity.addVelocity(motionX * power, motionY * power * ConfigGameplay.Kinetic.launchYScale, motionZ * power);
		entity.velocityChanged = true;

		if (ConfigGameplay.Kinetic.launchFallReset && motionY > 0) {
			if (entity.motionY > 0) {
				entity.fallDistance = 0;
			} else if (entity.motionY > TERMINAL_VELOCITY) {
				entity.fallDistance *= entity.motionY / TERMINAL_VELOCITY;
			}
		}

		if (ConfigGameplay.Kinetic.launchFloatReset && entity instanceof EntityPlayerMP) {
			try {
				// Set .floatingTickCount to 0. Ideally we could do this with access transformers, but
				// it doesn't appear to work under all environments
				ObfuscationReflectionHelper.setPrivateValue(NetHandlerPlayServer.class,
					((EntityPlayerMP) entity).connection, 0, "field_147365_f");
			} catch (RuntimeException ignored) {
				// This'll be logged by FML, so we'll ignore it for now.
			}
		}
	}

	private static class ConstantOwnable extends ConstantReference<ConstantOwnable> implements IPlayerOwnable {
		private final GameProfile profile;

		private ConstantOwnable(GameProfile profile) {
			this.profile = profile;
		}

		@Nullable
		@Override
		public GameProfile getOwningProfile() {
			return profile;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			ConstantOwnable that = (ConstantOwnable) o;

			return profile != null ? profile.equals(that.profile) : that.profile == null;
		}

		@Override
		public int hashCode() {
			return profile != null ? profile.hashCode() : 0;
		}

		@Nonnull
		@Override
		public ConstantOwnable get() throws LuaException {
			return this;
		}

		@Nonnull
		@Override
		public ConstantOwnable safeGet() throws LuaException {
			return this;
		}
	}
}
