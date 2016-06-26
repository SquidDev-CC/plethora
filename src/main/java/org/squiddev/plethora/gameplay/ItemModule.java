package org.squiddev.plethora.gameplay;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.squiddev.plethora.ItemBase;
import org.squiddev.plethora.Plethora;
import org.squiddev.plethora.api.module.IModule;
import org.squiddev.plethora.api.module.IModuleItem;
import org.squiddev.plethora.api.reference.IReference;
import org.squiddev.plethora.utils.Helpers;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ItemModule extends ItemBase implements IModuleItem {
	public static final String INTROSPECTION = "moduleIntrospection";
	public static final String LASER = "moduleLaser";
	public static final String SCANNER = "moduleScanner";
	public static final String SENSOR = "moduleSensor";

	private static final int MODULES = 4;

	public ItemModule() {
		super("module");
		setHasSubtypes(true);
	}

	public String getName(int id) {
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

	public ResourceLocation toResource(String name) {
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

	@Nonnull
	@Override
	public Collection<? extends IReference<?>> getAdditionalContext(@Nonnull ItemStack stack) {
		return Collections.emptyList();
	}
}
