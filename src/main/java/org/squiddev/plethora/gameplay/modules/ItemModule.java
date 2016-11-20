package org.squiddev.plethora.gameplay.modules;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import org.squiddev.plethora.api.Constants;
import org.squiddev.plethora.api.module.IModuleHandler;
import org.squiddev.plethora.api.reference.EntityReference;
import org.squiddev.plethora.api.reference.IReference;
import org.squiddev.plethora.gameplay.ConfigGameplay;
import org.squiddev.plethora.gameplay.ItemBase;
import org.squiddev.plethora.gameplay.Plethora;
import org.squiddev.plethora.gameplay.client.entity.RenderLaser;
import org.squiddev.plethora.utils.Helpers;

import javax.annotation.Nonnull;
import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix4f;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.squiddev.plethora.gameplay.ConfigGameplay.Kinetic.launchMax;
import static org.squiddev.plethora.gameplay.ConfigGameplay.Laser.laserMaximum;
import static org.squiddev.plethora.gameplay.ConfigGameplay.Laser.laserMinimum;

public final class ItemModule extends ItemBase {
	public static final String INTROSPECTION = "introspection";
	public static final String LASER = "laser";
	public static final String SCANNER = "scanner";
	public static final String SENSOR = "sensor";
	public static final String KINETIC = "kinetic";

	public static final int INTROSPECTION_ID = 0;
	public static final int LASER_ID = 1;
	public static final int SCANNER_ID = 2;
	public static final int SENSOR_ID = 3;
	public static final int KINETIC_ID = 4;

	private static final int MODULES = 5;

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

	public static String getName(int id) {
		switch (id) {
			case INTROSPECTION_ID:
				return INTROSPECTION;
			case LASER_ID:
				return LASER;
			case SCANNER_ID:
				return SCANNER;
			case SENSOR_ID:
				return SENSOR;
			case KINETIC_ID:
				return KINETIC;
			default:
				return "unknown";
		}
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return super.getUnlocalizedName() + ".module_" + getName(stack.getItemDamage());
	}

	@Override
	public void getSubItems(Item item, CreativeTabs tab, List<ItemStack> out) {
		for (int i = 0; i < MODULES; i++) {
			out.add(new ItemStack(this, 1, i));
		}
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		switch (stack.getItemDamage()) {
			case INTROSPECTION_ID:
				if (!world.isRemote) {
					if (player.isSneaking()) {
						UUID id = player.getGameProfile().getId();
						if (id != null) {
							NBTTagCompound compound = getTag(stack);
							compound.setLong("id_lower", id.getLeastSignificantBits());
							compound.setLong("id_upper", id.getMostSignificantBits());
							compound.setString("bound_name", player.getName());
						}
					} else {
						player.displayGUIChest(player.getInventoryEnderChest());
					}
				}

				return stack;
			case LASER_ID:
			case KINETIC_ID:
				player.setItemInUse(stack, MAX_TICKS);
				return stack;
			default:
				return stack;
		}
	}

	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World world, EntityPlayer player, int remaining) {
		if (world.isRemote) return;

		// Get the number of ticks the laser has been used for
		// We use a float we'll have to cast it later anyway
		float ticks = MAX_TICKS - remaining;
		if (ticks > USE_TICKS) ticks = USE_TICKS;
		if (ticks < 0) ticks = 0;

		switch (stack.getItemDamage()) {
			case LASER_ID: {
				double potency = (ticks / USE_TICKS) * (laserMaximum - laserMinimum) + laserMaximum;
				double inaccuracy = (USE_TICKS - ticks) / USE_TICKS * LASER_MAX_SPREAD;

				world.spawnEntityInWorld(new EntityLaser(world, player, (float) inaccuracy, (float) potency));
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
	public void addInformation(ItemStack stack, EntityPlayer player, List<String> out, boolean um) {
		super.addInformation(stack, player, out, um);

		String entity = getEntityName(stack);
		if (entity != null) {
			out.add("Bound to " + entity);
		}
	}

	//region Registering
	@Override
	@SideOnly(Side.CLIENT)
	public void clientInit() {
		for (int i = 0; i < MODULES; i++) {
			Helpers.setupModel(this, i, "module_" + getName(i));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void clientPreInit() {
		RenderingRegistry.registerEntityRenderingHandler(EntityLaser.class, new IRenderFactory<EntityLaser>() {
			@Override
			public Render<EntityLaser> createRenderFor(RenderManager renderManager) {
				return new RenderLaser(renderManager);
			}
		});
	}

	@Override
	public void preInit() {
		super.preInit();
		EntityRegistry.registerModEntity(EntityLaser.class, "laser", 0, Plethora.instance, 64, 10, true);
	}

	@Override
	public void init() {
		super.init();

		GameRegistry.addShapedRecipe(new ItemStack(this, 1, INTROSPECTION_ID),
			"GCG",
			"CHC",
			"GCG",
			'G', new ItemStack(Items.gold_ingot),
			'H', new ItemStack(Items.skull),
			'C', new ItemStack(Blocks.ender_chest)
		);

		GameRegistry.addShapedRecipe(new ItemStack(this, 1, LASER_ID),
			"III",
			"GDR",
			"  I",
			'D', new ItemStack(Items.diamond),
			'I', new ItemStack(Items.iron_ingot),
			'G', new ItemStack(Blocks.glass),
			'R', new ItemStack(Items.redstone)
		);


		GameRegistry.addShapedRecipe(new ItemStack(this, 1, SCANNER_ID),
			"EDE",
			"IGI",
			"III",
			'G', new ItemStack(Blocks.glass),
			'I', new ItemStack(Items.iron_ingot),
			'E', new ItemStack(Items.ender_pearl),
			'D', new ItemStack(Blocks.dirt)
		);

		GameRegistry.addShapedRecipe(new ItemStack(this, 1, SENSOR_ID),
			"ERE",
			"IGI",
			"III",
			'G', new ItemStack(Blocks.glass),
			'I', new ItemStack(Items.iron_ingot),
			'E', new ItemStack(Items.ender_pearl),
			'R', new ItemStack(Items.rotten_flesh)
		);

		GameRegistry.addShapedRecipe(new ItemStack(this, 1, KINETIC_ID),
			"RGR",
			"PBP",
			"RGR",
			'G', new ItemStack(Items.gold_ingot),
			'R', new ItemStack(Items.redstone),
			'P', new ItemStack(Blocks.piston),
			'B', new ItemStack(Blocks.redstone_block)
		);
	}

	//endregion

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound tag) {
		return new ItemModuleHandler(stack);
	}

	private static final class ItemModuleHandler implements IModuleHandler, ICapabilityProvider {
		private final ItemStack stack;
		private ResourceLocation moduleId;

		private ItemModuleHandler(ItemStack stack) {
			this.stack = stack;
		}

		@Override
		public boolean hasCapability(Capability<?> capability, EnumFacing enumFacing) {
			return capability == Constants.MODULE_HANDLER_CAPABILITY && stack.getItemDamage() < MODULES;
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T> T getCapability(Capability<T> capability, EnumFacing enumFacing) {
			return capability == Constants.MODULE_HANDLER_CAPABILITY && stack.getItemDamage() < MODULES ? (T) this : null;
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

		@Nonnull
		@Override
		public Collection<IReference<?>> getAdditionalContext() {
			Entity entity = getEntity(stack);
			if (entity != null) {
				return Collections.<IReference<?>>singleton(new EntityReference<Entity>(entity));
			} else {
				return Collections.emptyList();
			}
		}

		@Nonnull
		@Override
		public Pair<IBakedModel, Matrix4f> getModel(float delta) {
			Matrix4f matrix = new Matrix4f(1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1);
			matrix.setRotation(new AxisAngle4f(0f, 1f, 0f, delta));

			return Pair.of(
				Helpers.getMesher().getItemModel(stack),
				matrix
			);
		}
	}

	private static Entity getEntity(ItemStack stack) {
		NBTTagCompound tag = stack.getTagCompound();
		if (tag != null && tag.hasKey("id_lower", 99)) {
			MinecraftServer server = MinecraftServer.getServer();
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

	public static void launch(EntityLivingBase entity, float yaw, float pitch, float power) {
		float motionX = -MathHelper.sin(yaw / 180.0f * (float) Math.PI) * MathHelper.cos(pitch / 180.0f * (float) Math.PI);
		float motionZ = MathHelper.cos(yaw / 180.0f * (float) Math.PI) * MathHelper.cos(pitch / 180.0f * (float) Math.PI);
		float motionY = -MathHelper.sin(pitch / 180.0f * (float) Math.PI);

		power /= MathHelper.sqrt_float(motionX * motionX + motionY * motionY + motionZ * motionZ);
		entity.addVelocity(motionX * power, motionY * power * ConfigGameplay.Kinetic.launchYScale, motionZ * power);
		entity.velocityChanged = true;
	}

}
