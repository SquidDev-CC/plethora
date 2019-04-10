package org.squiddev.plethora.integration.vanilla.meta;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemBanner;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.BannerPattern;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.ItemStackMetaProvider;
import org.squiddev.plethora.api.method.LuaList;
import org.squiddev.plethora.utils.TypedField;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Injects
public final class MetaItemBanner extends ItemStackMetaProvider<ItemBanner> {
	private static final TypedField<BannerPattern, String> FIELD_NAME = TypedField.of(BannerPattern.class, "fileName", "field_191014_N");

	public MetaItemBanner() {
		super(ItemBanner.class);
	}

	@Nonnull
	@Override
	public Map<String, ?> getMeta(@Nonnull ItemStack stack, @Nonnull ItemBanner banner) {
		LuaList<Map<String, ?>> out = new LuaList<>();

		NBTTagCompound tag = stack.getSubCompound("BlockEntityTag");
		if (tag != null && tag.hasKey("Patterns")) {
			NBTTagList nbttaglist = tag.getTagList("Patterns", 10);

			for (int i = 0; i < nbttaglist.tagCount() && i < 6; ++i) {
				NBTTagCompound patternTag = nbttaglist.getCompoundTagAt(i);

				EnumDyeColor color = EnumDyeColor.byDyeDamage(patternTag.getInteger("Color"));
				BannerPattern pattern = getPatternByID(patternTag.getString("Pattern"));

				if (pattern != null) {
					Map<String, String> entry = new HashMap<>();
					entry.put("id", pattern.getHashname());
					entry.put("name", FIELD_NAME.get(pattern));

					entry.put("colour", color.toString());
					entry.put("color", color.toString());

					out.add(entry);
				}
			}
		}

		return Collections.singletonMap("banner", out.asMap());
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
