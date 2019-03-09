package org.squiddev.plethora.api.method.gen;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import org.squiddev.plethora.api.Injects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

import static dan200.computercraft.core.apis.ArgumentHelper.*;

@Injects
public final class ArgumentTypes {
	public static final ArgumentType<String> STRING = new ArgumentType<String>() {
		@Nonnull
		@Override
		public String get(@Nonnull Object[] args, int index) throws LuaException {
			return getString(args, index);
		}

		@Nullable
		@Override
		public String opt(@Nonnull Object[] args, int index) throws LuaException {
			return optString(args, index, null);
		}
	};

	public static final ArgumentType<ResourceLocation> RESOURCE = STRING.map(ResourceLocation::new);

	public static final ArgumentType<Item> ITEM = RESOURCE.map(name -> {
		Item item = Item.REGISTRY.getObject(name);
		if (item == null || !Item.REGISTRY.containsKey(name)) throw new LuaException("Unknown item '" + name + "'");
		return item;
	});

	public static final ArgumentType<Fluid> FLUID = STRING.map(name -> {
		Fluid fluid = FluidRegistry.getFluid(name);
		if (fluid == null) throw new LuaException("Unknown fluid '" + name + "'");
		return fluid;
	});

	public static final ArgumentType<Map<?, ?>> TABLE = new ArgumentType<Map<?, ?>>() {
		@Nonnull
		@Override
		public Map<?, ?> get(@Nonnull Object[] args, int index) throws LuaException {
			return getTable(args, index);
		}

		@Nullable
		@Override
		public Map<?, ?> opt(@Nonnull Object[] args, int index) throws LuaException {
			return optTable(args, index, null);
		}
	};
}
