package org.squiddev.plethora.modules;

import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import org.squiddev.plethora.ItemBase;
import org.squiddev.plethora.Plethora;
import org.squiddev.plethora.api.module.IModule;
import org.squiddev.plethora.api.module.IModuleItem;
import org.squiddev.plethora.api.reference.EntityReference;
import org.squiddev.plethora.api.reference.IReference;
import org.squiddev.plethora.utils.Helpers;

import javax.annotation.Nonnull;
import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix4f;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class ItemModule extends ItemBase implements IModuleItem {
	public static final String INTROSPECTION = "moduleIntrospection";
	public static final String LASER = "moduleLaser";
	public static final String SCANNER = "moduleScanner";
	public static final String SENSOR = "moduleSensor";

	public static final int INTROSPECTION_ID = 0;
	public static final int LASER_ID = 1;
	public static final int SCANNER_ID = 2;
	public static final int SENSOR_ID = 3;

	private static final int MODULES = 4;

	public static final int SCANNER_RADIUS = 8;
	public static final int SENSOR_RADIUS = 16;

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
			default:
				return "unknown";
		}
	}

	public static ResourceLocation toResource(String name) {
		return new ResourceLocation(Plethora.RESOURCE_DOMAIN, name);
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return super.getUnlocalizedName() + "." + getName(stack.getItemDamage());
	}

	@Override
	public void getSubItems(Item item, CreativeTabs tab, List<ItemStack> out) {
		for (int i = 0; i < MODULES; i++) {
			out.add(new ItemStack(this, 1, i));
		}
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		if (world.isRemote) return stack;
		switch (stack.getItemDamage()) {
			case INTROSPECTION_ID:
				if (player.isSneaking()) {
					UUID id = player.getGameProfile().getId();
					if (id != null) {
						NBTTagCompound compound = getTag(stack);
						compound.setLong("id_lower", id.getLeastSignificantBits());
						compound.setLong("id_upper", id.getMostSignificantBits());
					}
				} else {
					player.displayGUIChest(player.getInventoryEnderChest());
				}
			default:
				return stack;
		}
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List<String> out, boolean um) {
		super.addInformation(stack, player, out, um);

		Entity entity = getEntity(stack);
		if (entity != null) {
			out.add("Bound to " + entity.getName());
		}
	}

	//region Registering
	@Override
	@SideOnly(Side.CLIENT)
	public void clientInit() {
		for (int i = 0; i < MODULES; i++) {
			Helpers.setupModel(this, i, getName(i));
		}
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
			'I', new ItemStack(Items.iron_ingot),
			'G', new ItemStack(Blocks.glass),
			'R', new ItemStack(Items.redstone)
		);


		GameRegistry.addShapedRecipe(new ItemStack(this, 1, LASER_ID),
			"EDE",
			"IGI",
			"III",
			'G', new ItemStack(Blocks.glass),
			'I', new ItemStack(Items.iron_ingot),
			'E', new ItemStack(Items.ender_pearl),
			'D', new ItemStack(Blocks.dirt)
		);

		GameRegistry.addShapedRecipe(new ItemStack(this, 1, LASER_ID),
			"ERE",
			"IGI",
			"III",
			'G', new ItemStack(Blocks.glass),
			'I', new ItemStack(Items.iron_ingot),
			'E', new ItemStack(Items.ender_pearl),
			'R', new ItemStack(Items.rotten_flesh)
		);
	}

	//endregion

	//region IModuleItem
	@Nonnull
	@Override
	public Collection<IReference<?>> getAdditionalContext(@Nonnull ItemStack stack) {
		Entity entity = getEntity(stack);
		if (entity != null) {
			return Collections.<IReference<?>>singleton(new EntityReference<Entity>(entity));
		} else {
			return Collections.emptyList();
		}
	}

	private Entity getEntity(ItemStack stack) {
		NBTTagCompound tag = stack.getTagCompound();
		if (tag != null && tag.hasKey("id_lower", 99)) {
			return MinecraftServer.getServer().getEntityFromUuid(new UUID(tag.getLong("id_upper"), tag.getLong("id_lower")));
		} else {
			return null;
		}
	}

	@Nonnull
	@Override
	public IModule getModule(@Nonnull ItemStack stack) {
		final ResourceLocation location = toResource(getName(stack.getItemDamage()));

		return new IModule() {
			@Nonnull
			@Override
			public ResourceLocation getModuleId() {
				return location;
			}
		};
	}

	@Nonnull
	@Override
	@SideOnly(Side.CLIENT)
	public Pair<IBakedModel, Matrix4f> getModel(@Nonnull ItemStack stack, float delta) {
		Matrix4f matrix = new Matrix4f(1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1);
		matrix.setRotation(new AxisAngle4f(0f, 1f, 0f, delta));

		return Pair.of(
			Helpers.getMesher().getItemModel(stack),
			matrix
		);
	}
	//endregion
}
