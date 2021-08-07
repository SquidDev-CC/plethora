package org.squiddev.plethora.api.method.wrapper;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.utils.NBTUtilsRecursive;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static dan200.computercraft.api.lua.ArgumentHelper.*;

@Injects
public final class ArgumentTypes {
	public static final ArgumentType<String> STRING = new ArgumentType<String>() {
		@Override
		public String name() {
			return "string";
		}

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

	public static final ArgumentType<UUID> UUID_ARG = new ArgumentType<UUID>() {
		@Override
		public String name() {
			return "string";
		}

		@Nonnull
		@Override
		public UUID get(@Nonnull Object[] args, int index) throws LuaException {
			if (index >= args.length) throw badArgument(index, "string", "no value");
			Object value = args[index];
			if (value instanceof String) {
				String uuid = ((String) value).toLowerCase(Locale.ENGLISH);
				try {
					return UUID.fromString(uuid);
				} catch (IllegalArgumentException e) {
					throw new LuaException("Bad uuid '" + uuid + "' for argument #" + (index + 1));
				}
			} else {
				throw badArgumentOf(index, "string", value);
			}
		}
	};

	public static final ArgumentType<Map<?, ?>> TABLE = new ArgumentType<Map<?, ?>>() {
		@Override
		public String name() {
			return "table";
		}

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

	public static final ArgumentType<NBTTagCompound> NBT_TAG_COMPOUND_ARG = TABLE.map(NBTUtilsRecursive::encodeNBTTagCompound);

	public  static final  ArgumentType<EntityEntry> ENTITY_ARG = RESOURCE.map(name -> {
		EntityEntry entityEntry = ForgeRegistries.ENTITIES.getValue(name);
		if (entityEntry == null || !ForgeRegistries.ENTITIES.containsKey(name)) throw new LuaException("Unknown entity '" + name + "'");
		return entityEntry;
	});

	private ArgumentTypes() {
	}
}
