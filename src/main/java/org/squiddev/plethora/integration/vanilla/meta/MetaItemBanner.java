package org.squiddev.plethora.integration.vanilla.meta;

import com.google.common.collect.Maps;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemBanner;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.BannerPattern;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.ItemStackMetaProvider;

import javax.annotation.Nonnull;
import java.util.Map;

@Injects
public final class MetaItemBanner extends ItemStackMetaProvider<ItemBanner> {
	public MetaItemBanner() {
		super("banner", ItemBanner.class);
	}

	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull ItemStack stack, @Nonnull ItemBanner banner) {
		int idx = 0;
		Map<Object, Object> out = Maps.newHashMap();

		NBTTagCompound tag = stack.getSubCompound("BlockEntityTag");
		if (tag != null && tag.hasKey("Patterns")) {
			NBTTagList nbttaglist = tag.getTagList("Patterns", 10);

			for (int i = 0; i < nbttaglist.tagCount() && i < 6; ++i) {
				NBTTagCompound patternTag = nbttaglist.getCompoundTagAt(i);

				EnumDyeColor color = EnumDyeColor.byDyeDamage(patternTag.getInteger("Color"));
				BannerPattern pattern = getPatternByID(patternTag.getString("Pattern"));

				if (pattern != null) {
					Map<String, String> entry = Maps.newHashMap();
					entry.put("id", pattern.getHashname());

					// patternName
					String name = ObfuscationReflectionHelper.getPrivateValue(BannerPattern.class, pattern, "field_191014_N");
					entry.put("name", name);

					entry.put("colour", color.toString());
					entry.put("color", color.toString());

					out.put(++idx, entry);
				}
			}
		}

		return out;
	}

	@Nonnull
	@Override
	public ItemStack getExample() {
		NBTTagList patterns = new NBTTagList();

		NBTTagCompound pattern1 = new NBTTagCompound();
		pattern1.setString("Pattern", BannerPattern.CREEPER.getHashname());
		pattern1.setInteger("Color", 5);

		patterns.appendTag(pattern1);

		return ItemBanner.makeBanner(EnumDyeColor.GREEN, patterns);
	}

	private static BannerPattern getPatternByID(String id) {
		for (BannerPattern pattern : BannerPattern.values()) {
			if (pattern.getHashname().equals(id)) return pattern;
		}

		return null;
	}
}
