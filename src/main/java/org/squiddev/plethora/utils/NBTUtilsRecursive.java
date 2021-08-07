package org.squiddev.plethora.utils;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.nbt.*;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.function.Predicate;

import static dan200.computercraft.api.lua.ArgumentHelper.getTable;

public final class NBTUtilsRecursive {

	@Nonnull
	public static NBTTagCompound encodeNBTTagCompound(Map<?,?> map) throws LuaException {
		NBTTagCompound nbt = new NBTTagCompound();

		for (Object key : map.keySet())
			nbt.setTag(key.toString(), encodeObject(map.get(key)));

		return nbt;
	}

	@Nonnull
	public static NBTTagList encodeNBTTagList(Map<?,?> map) throws LuaException {
		NBTTagList nbt = new NBTTagList();

		for (Object key : map.keySet())
			nbt.appendTag(encodeObject(map.get(key)));
		return nbt;
	}

	@Nonnull
	public static NBTBase encodeObject(Object object) throws LuaException {
		switch (object.getClass().getSimpleName()) {
			case "String":
				return new NBTTagString((String) object);
			case "Double":
				return new NBTTagDouble((Double) object);
			case "Boolean":
				return new NBTTagByte((byte) ((Boolean) object ? 1 : 0));
			case "HashMap":
				if (
						((Map<?,?>) object)
								.keySet().stream().allMatch(
								(Predicate<Object>) o -> o instanceof Double // Verify if it is an NBTTagList
						)
				) {
					return encodeNBTTagList((Map<?, ?>) object);
				} else {
					return encodeNBTTagCompound((Map<?, ?>) object);
				}
			default:
				throw new LuaException("Unknown type " + object.getClass().getSimpleName());
		}
	}

	@Nonnull
	public static @NotNull NBTTagCompound encodeObjects(Object[] objects, int index) throws LuaException {
		return encodeNBTTagCompound(getTable(objects, index));
	}
}
