package org.squiddev.plethora.modules;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.squiddev.plethora.ItemBase;
import org.squiddev.plethora.Plethora;
import org.squiddev.plethora.api.module.IModule;
import org.squiddev.plethora.api.module.IModuleItem;
import org.squiddev.plethora.api.reference.EntityReference;
import org.squiddev.plethora.api.reference.IReference;
import org.squiddev.plethora.utils.Helpers;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class ItemModule extends ItemBase implements IModuleItem {
	public static final String INTROSPECTION = "moduleIntrospection";
	public static final String LASER = "moduleLaser";
	public static final String SCANNER = "moduleScanner";
	public static final String SENSOR = "moduleSensor";

	private static final int MODULES = 4;

	public ItemModule() {
		super("module");
		setHasSubtypes(true);
	}

	public static String getName(int id) {
		switch (id) {
			case 0:
				return INTROSPECTION;
			case 1:
				return LASER;
			case 2:
				return SCANNER;
			case 3:
				return SENSOR;
			default:
				return INTROSPECTION;
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
	@SideOnly(Side.CLIENT)
	public void clientInit() {
		for (int i = 0; i < MODULES; i++) {
			Helpers.setupModel(this, i, getName(i));
		}
	}

	@Override
	public void getSubItems(Item item, CreativeTabs tab, List<ItemStack> out) {
		for (int i = 0; i < MODULES; i++) {
			out.add(new ItemStack(this, 1, i));
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

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		if (stack.getItemDamage() != 0 || !player.isSneaking() || world.isRemote) return stack;

		UUID id = player.getGameProfile().getId();
		if (id != null) {
			NBTTagCompound compound = getTag(stack);
			compound.setLong("id_lower", id.getLeastSignificantBits());
			compound.setLong("id_upper", id.getMostSignificantBits());
		}
		return stack;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List<String> out, boolean um) {
		super.addInformation(stack, player, out, um);

		Entity entity = getEntity(stack);
		if (entity != null) {
			out.add("Bound to " + entity.getName());
		}
	}

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
}
