package org.squiddev.plethora.integration.vanilla.meta;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.common.util.Constants;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.output.NullOutputStream;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.integration.PlethoraIntegration;
import org.squiddev.plethora.utils.Helpers;

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
@Injects
public final class MetaItemBasic extends BasicMetaProvider<ItemStack> {
	@Nonnull
	@Override
	public Map<String, ?> getMeta(@Nonnull ItemStack stack) {
		if (stack.isEmpty()) return Collections.emptyMap();

		HashMap<String, Object> data = new HashMap<>();
		fillBasicMeta(data, stack);

		String display = stack.getDisplayName();
		data.put("displayName", display == null || display.isEmpty() ? stack.getTranslationKey() : display);
		data.put("rawName", stack.getTranslationKey());

		data.put("maxCount", stack.getMaxStackSize());
		data.put("maxDamage", stack.getMaxDamage());

		if (stack.getItem().showDurabilityBar(stack)) {
			data.put("durability", stack.getItem().getDurabilityForDisplay(stack));
		}

		NBTTagCompound tag = stack.getTagCompound();
		if (tag != null && tag.hasKey("display", Constants.NBT.TAG_COMPOUND)) {
			NBTTagCompound displayTag = tag.getCompoundTag("display");
			if (displayTag.hasKey("Lore", Constants.NBT.TAG_LIST)) {
				NBTTagList loreTag = displayTag.getTagList("Lore", Constants.NBT.TAG_STRING);
				data.put("lore", Helpers.map(loreTag, x -> ((NBTTagString) x).getString()));
			}
		}

		return data;
	}

	@Nonnull
	public static HashMap<String, Object> getBasicMeta(@Nonnull ItemStack stack) {
		HashMap<String, Object> data = new HashMap<>();
		fillBasicMeta(data, stack);
		return data;
	}

	public static void fillBasicMeta(@Nonnull Map<? super String, Object> data, @Nonnull ItemStack stack) {
		data.put("name", stack.getItem().getRegistryName().toString());
		data.put("damage", stack.getItemDamage());
		data.put("count", stack.getCount());
		data.put("nbtHash", getNBTHash(stack));
	}

	@Nonnull
	@Override
	public ItemStack getExample() {
		return new ItemStack(Items.STICK, 5);
	}

	@Nullable
	public static String getNBTHash(@Nonnull ItemStack stack) {
		return stack.hasTagCompound() ? getNBTHash(stack.getTagCompound()) : null;
	}

	@Nullable
	public static String getNBTHash(@Nullable NBTTagCompound tag) {
		if (tag == null || tag.isEmpty()) return null;

		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			OutputStream dump = new NullOutputStream();
			DigestOutputStream hasher = new DigestOutputStream(dump, digest);
			DataOutput output = new DataOutputStream(hasher);
			CompressedStreamTools.write(tag, output);
			byte[] hash = digest.digest();
			return new String(Hex.encodeHex(hash));
		} catch (NoSuchAlgorithmException | IOException e) {
			PlethoraIntegration.LOG.error("Cannot hash NBT", e);
			return null;
		}
	}
}
