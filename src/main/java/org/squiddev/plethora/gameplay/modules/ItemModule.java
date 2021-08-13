package org.squiddev.plethora.gameplay.modules;

import com.mojang.authlib.GameProfile;
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
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.squiddev.plethora.api.Constants;
import org.squiddev.plethora.api.PlethoraAPI;
import org.squiddev.plethora.api.method.IContextBuilder;
import org.squiddev.plethora.api.module.AbstractModuleHandler;
import org.squiddev.plethora.api.module.IModuleAccess;
import org.squiddev.plethora.api.reference.Reference;
import org.squiddev.plethora.api.vehicle.IVehicleUpgradeHandler;
import org.squiddev.plethora.core.ConfigCore;
import org.squiddev.plethora.gameplay.ConfigGameplay;
import org.squiddev.plethora.gameplay.ItemBase;
import org.squiddev.plethora.gameplay.Plethora;
import org.squiddev.plethora.gameplay.client.RenderHelpers;
import org.squiddev.plethora.gameplay.modules.glasses.CanvasServer;
import org.squiddev.plethora.integration.EntityIdentifier;
import org.squiddev.plethora.utils.Helpers;
import org.squiddev.plethora.utils.TypedField;

import javax.annotation.Nonnull;
import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix4f;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.squiddev.plethora.gameplay.ConfigGameplay.Kinetic.launchMax;
import static org.squiddev.plethora.gameplay.ConfigGameplay.Laser.maximumPotency;
import static org.squiddev.plethora.gameplay.ConfigGameplay.Laser.minimumPotency;
import static org.squiddev.plethora.gameplay.modules.PlethoraModules.*;

public final class ItemModule extends ItemBase {
	private static final int MAX_TICKS = 72000;
	private static final int USE_TICKS = 30;

	private static final TypedField<NetHandlerPlayServer, Integer> FIELD_FLOATING_TICK_COUNT = TypedField.of(NetHandlerPlayServer.class, "floatingTickCount", "field_147365_f");

	/**
	 * We multiply the gaussian by this number.
	 * This is the change in velocity for each axis after normalisation.
	 *
	 * @see net.minecraft.entity.projectile.EntityThrowable#shoot(double, double, double, float, float)
	 */
	private static final float LASER_MAX_SPREAD = (float) (0.1 / 0.007499999832361937);

	public ItemModule() {
		super("module");
		setHasSubtypes(true);
	}

	@Nonnull
	@Override
	public String getTranslationKey(ItemStack stack) {
		return getTranslationKey() + ".module_" + getName(stack.getItemDamage());
	}

	private static boolean isBlacklisted(ItemStack stack) {
		return ConfigCore.Blacklist.blacklistModules.contains(Plethora.RESOURCE_DOMAIN + ":" + getName(stack.getItemDamage()));
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

		if (!world.isRemote && isBlacklisted(stack)) return ActionResult.newResult(EnumActionResult.FAIL, stack);

		switch (stack.getItemDamage()) {
			case INTROSPECTION_ID:
			case CHAT_ID:
				if (!world.isRemote) {
					if (player.isSneaking() && !player.getGameProfile().getName().startsWith("[") && player.getGameProfile().getId() != null) {
						NBTTagCompound tag = getTag(stack);

						if (player.getGameProfile().equals(getProfile(stack))) {
							// Remove the binding if we're already bound
							tag.removeTag("id_lower");
							tag.removeTag("id_upper");
							tag.removeTag("bound_name");
							// If our tag is now empty, clear it - turtle/pocket upgrades require NBT to be exactly the
							// same as the template item.
							if (tag.isEmpty()) stack.setTagCompound(null);

							player.sendStatusMessage(
								new TextComponentTranslation("item.plethora.module.module_" + getName(stack.getItemDamage()) + ".cleared", player.getName()),
								true
							);

						} else {
							// Otherwise bind to the current player
							UUID id = player.getGameProfile().getId();
							tag.setLong("id_lower", id.getLeastSignificantBits());
							tag.setLong("id_upper", id.getMostSignificantBits());
							tag.setString("bound_name", player.getName());

							player.sendStatusMessage(
								new TextComponentTranslation("item.plethora.module.module_" + getName(stack.getItemDamage()) + ".bound", player.getName()),
								true
							);
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
	public void onPlayerStoppedUsing(@Nonnull ItemStack stack, World world, @Nonnull EntityLivingBase player, int remaining) {
		if (world.isRemote) return;
		if (isBlacklisted(stack)) return;

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
		return stack.getItemDamage() == LASER_ID || stack.getItemDamage() == KINETIC_ID
			? EnumAction.BOW
			: super.getItemUseAction(stack);
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return stack.getItemDamage() == LASER_ID || stack.getItemDamage() == KINETIC_ID
			? MAX_TICKS
			: super.getMaxItemUseDuration(stack);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean hasEffect(@Nonnull ItemStack stack) {
		return super.hasEffect(stack) || getLevel(stack) > 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World world, List<String> out, ITooltipFlag flag) {
		super.addInformation(stack, world, out, flag);

		String entity = getEntityName(stack);
		if (entity != null) {
			out.add(Helpers.translateToLocalFormatted("item.plethora.module.module_" + getName(stack.getItemDamage()) + ".binding", entity));
		}

		int level = getLevel(stack);
		if (level > 0) {
			switch (stack.getItemDamage()) {
				case SCANNER_ID: {
					int range = getEffectiveRange(level, ConfigGameplay.Scanner.radius, ConfigGameplay.Scanner.maxRadius);
					out.add(Helpers.translateToLocalFormatted("item.plethora.module.level", level, range));
					break;
				}
				case SENSOR_ID: {
					int range = getEffectiveRange(level, ConfigGameplay.Sensor.radius, ConfigGameplay.Sensor.maxRadius);
					out.add(Helpers.translateToLocalFormatted("item.plethora.module.level", level, range));
					break;
				}
				default:
					out.add(Helpers.translateToLocalFormatted("item.plethora.module.level", level, "?"));
					break;
			}
		}
	}

	@Nonnull
	@Override
	public ICapabilityProvider initCapabilities(@Nonnull ItemStack stack, NBTTagCompound tag) {
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
			return id == null
				? (moduleId = new ResourceLocation(Plethora.RESOURCE_DOMAIN, getName(stack.getItemDamage())))
				: id;
		}

		@Override
		public void getAdditionalContext(@Nonnull IModuleAccess access, @Nonnull IContextBuilder builder) {
			String moduleKey = moduleId.toString();

			Entity entity = getEntity(stack);
			if (entity != null) builder.addContext(moduleKey, entity, Reference.entity(entity));

			GameProfile profile = getProfile(stack);
			if (profile != null) builder.addContext(moduleKey, new EntityIdentifier.Player(profile));

			switch (stack.getItemDamage()) {
				case SCANNER_ID: {
					// Inject range information for scanners
					int level = getLevel(stack);
					builder.addContext(moduleKey, RangeInfo.of(level,
						x -> (x+1) * ConfigGameplay.Scanner.scanLevelCost,
						x -> getEffectiveRange(x, ConfigGameplay.Scanner.radius, ConfigGameplay.Scanner.maxRadius)
					));
					break;
				}

				case SENSOR_ID: {
					// Inject range information for sensors
					int level = getLevel(stack);
					builder.addContext(moduleKey, RangeInfo.of(level,
						x -> (x+1) * ConfigGameplay.Sensor.senseLevelCost,
						x -> getEffectiveRange(x, ConfigGameplay.Sensor.radius, ConfigGameplay.Sensor.maxRadius)
					));
					break;
				}

				case CHAT_ID: {
					// Add a chat listener if we've got an entity (and are a chat module).
					Object owner = access.getOwner();
					UUID ownerId;
					if (owner instanceof Entity) {
						ownerId = ((Entity) owner).getUniqueID();
					} else if (profile != null && ConfigGameplay.Chat.allowBinding) {
						ownerId = profile.getId();
					} else {
						ownerId = null;
					}

					ChatListener.Listener listener = new ChatListener.Listener(access, ownerId);
					if (ownerId != null) builder.addContext(moduleKey, listener);
					builder.addAttachable(listener);
					break;
				}

				case GLASSES_ID: {
					// Add a chat listener if we've got an entity (and are a chat module).
					Object owner = access.getOwner();
					EntityPlayerMP ownerEntity = owner instanceof EntityPlayerMP ? (EntityPlayerMP) owner : null;

					if (ownerEntity != null && !(ownerEntity instanceof FakePlayer)) {
						CanvasServer glasses = new CanvasServer(access, ownerEntity);
						builder.addContext(moduleKey, glasses).addAttachable(glasses);
					}
					break;
				}

				case CHAT_CREATIVE_ID: {
					ChatListener.Listener listener = new ChatListener.CreativeListener(access);
					builder.addContext(moduleKey, listener).addAttachable(listener);
					break;
				}
			}
		}

		@Nonnull
		@Override
		@SideOnly(Side.CLIENT)
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

	public static GameProfile getProfile(ItemStack stack) {
		NBTTagCompound tag = stack.getTagCompound();
		return tag != null && tag.hasKey("id_lower", NBT.TAG_ANY_NUMERIC)
			? new GameProfile(new UUID(tag.getLong("id_upper"), tag.getLong("id_lower")), tag.getString("bound_name"))
			: null;
	}

	private static Entity getEntity(ItemStack stack) {
		NBTTagCompound tag = stack.getTagCompound();
		if (tag != null && tag.hasKey("id_lower", NBT.TAG_ANY_NUMERIC)) {
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
		return tag != null && tag.hasKey("bound_name", NBT.TAG_STRING) ? tag.getString("bound_name") : null;
	}

	public static int getLevel(ItemStack stack) {
		NBTTagCompound tag = stack.getTagCompound();
		return tag != null && tag.hasKey("level", NBT.TAG_ANY_NUMERIC) ? tag.getInteger("level") : 0;
	}

	public static int getEffectiveRange(int level, int baseRange, int maxRange) {
		if (maxRange <= baseRange || level <= 0) return baseRange;

		// Each level adds half of the remainder to the maximum level - so effectively the geometric sum.
		return baseRange + (int) Math.ceil((1 - Math.pow(0.5, level)) * (maxRange - baseRange));
	}

	public static int getEffectiveRange(ItemStack stack, int baseRange, int maxRange) {
		return getEffectiveRange(getLevel(stack), baseRange, maxRange);
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
			// Set .floatingTickCount to 0. Ideally we could do this with access transformers, but
			// it doesn't appear to work under all environments
			FIELD_FLOATING_TICK_COUNT.set(((EntityPlayerMP) entity).connection, 0);
		}
	}
}
