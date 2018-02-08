package org.squiddev.plethora.integration.vanilla.meta;

import com.google.common.collect.Maps;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.output.NullOutputStream;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.utils.DebugLogger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Adds basic properties for item stacks.
 */
@IMetaProvider.Inject(ItemStack.class)
public class MetaItemBasic extends BasicMetaProvider<ItemStack> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull ItemStack stack) {
		if (stack.isEmpty()) return Collections.emptyMap();

		HashMap<Object, Object> data = getBasicProperties(stack);

		String display = stack.getDisplayName();
		data.put("displayName", display == null || display.length() == 0 ? stack.getUnlocalizedName() : display);
		data.put("rawName", stack.getUnlocalizedName());

		data.put("maxCount", stack.getMaxStackSize());
		data.put("maxDamage", stack.getMaxDamage());

		if (stack.getItem().showDurabilityBar(stack)) {
			data.put("durability", stack.getItem().getDurabilityForDisplay(stack));
		}

		return data;
	}

	@Nonnull
	public static HashMap<Object, Object> getBasicProperties(@Nonnull ItemStack stack) {
		HashMap<Object, Object> data = Maps.newHashMap();

		data.put("name", stack.getItem().getRegistryName().toString());
		data.put("damage", stack.getItemDamage());
		data.put("count", stack.getCount());
		data.put("nbtHash", getNBTHash(stack));

		return data;
	}

	@Nullable
	public static String getNBTHash(@Nonnull ItemStack stack) {
		return stack.hasTagCompound() ? getNBTHash(stack.getTagCompound()) : null;
	}

	@Nullable
	public static String getNBTHash(@Nullable NBTTagCompound tag) {
		if (tag == null || tag.hasNoTags()) return null;

		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			OutputStream dump = new NullOutputStream();
			DigestOutputStream hasher = new DigestOutputStream(dump, digest);
			DataOutput output = new DataOutputStream(hasher);
			CompressedStreamTools.write(tag, output);
			byte[] hash = digest.digest();
			return new String(Hex.encodeHex(hash));
		} catch (NoSuchAlgorithmException | IOException e) {
			DebugLogger.error("Cannot hash NBT", e);
			return null;
		}
	}
}
