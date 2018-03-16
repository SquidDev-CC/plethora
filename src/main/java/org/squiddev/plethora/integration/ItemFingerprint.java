package org.squiddev.plethora.integration;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;
import org.squiddev.plethora.integration.vanilla.meta.MetaItemBasic;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

import static dan200.computercraft.core.apis.ArgumentHelper.badArgument;
import static org.squiddev.plethora.api.method.ArgumentHelper.badObject;

/**
 * An representation of an item which can be used to compare stacks.
 */
public class ItemFingerprint {
	@Nonnull
	public final Item item;

	public final int damage;

	@Nullable
	public final String hash;

	public ItemFingerprint(@Nonnull Item item, int damage, @Nullable String hash) {
		this.item = item;
		this.damage = damage;
		this.hash = hash;
	}

	public boolean matches(ItemStack stack) {
		if (stack.getItem() != item) return false;
		if (damage != OreDictionary.WILDCARD_VALUE && damage != stack.getItemDamage()) return false;
		if (hash != null && !hash.equals(MetaItemBasic.getNBTHash(stack))) return false;

		return true;
	}

	@Nonnull
	public static ItemFingerprint fromLua(@Nonnull Object[] args, int index) throws LuaException {
		String name;
		Integer damage;
		String hash;

		Object arg = index >= args.length ? null : args[index];
		if (arg instanceof String) {
			String contents = (String) arg;
			int atIdx = contents.lastIndexOf('@');

			if (atIdx < 0) {
				name = contents;
				damage = null;
			} else {
				name = contents.substring(0, atIdx);
				try {
					damage = Integer.parseInt(contents.substring(atIdx + 1));
				} catch (NumberFormatException e) {
					throw new LuaException("Cannot convert damage to item: " + e.getMessage());
				}
			}

			hash = null;
		} else if (arg instanceof Map) {
			Map<?, ?> data = (Map) arg;

			Object nameObj = data.get("name");
			if (!(nameObj instanceof String)) throw badObject(nameObj, "key 'name'", "string");

			Object damageObj = data.get("damage");
			if (damageObj != null && !(damageObj instanceof Number)) {
				throw badObject(damageObj, "key 'damage'", "int|nil");
			}

			Object hashObj = data.get("nbthash");
			if (hashObj != null && !(hashObj instanceof String)) {
				throw badObject(hashObj, "key 'nbthash'", "string|nil");
			}

			name = (String) nameObj;
			damage = damageObj == null ? null : ((Number) damageObj).intValue();
			hash = hashObj == null ? null : (String) hashObj;
		} else {
			throw badArgument(index, "string|table", arg);
		}

		ResourceLocation nameRes = new ResourceLocation(name);
		if (!ForgeRegistries.ITEMS.containsKey(nameRes)) throw new LuaException("No such item '" + name + "'");
		Item item = ForgeRegistries.ITEMS.getValue(nameRes);

		return new ItemFingerprint(item, damage == null ? OreDictionary.WILDCARD_VALUE : damage, hash);
	}
}
