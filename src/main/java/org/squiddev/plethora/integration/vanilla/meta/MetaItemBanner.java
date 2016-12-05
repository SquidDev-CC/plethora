package org.squiddev.plethora.integration.vanilla.meta;

import com.google.common.collect.Maps;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemBanner;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntityBanner;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

@IMetaProvider.Inject(value = ItemStack.class, namespace = "banner")
public class MetaItemBanner extends BasicMetaProvider<ItemStack> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull ItemStack stack) {
		if (!(stack.getItem() instanceof ItemBanner)) return Collections.emptyMap();

		int idx = 0;
		Map<Object, Object> out = Maps.newHashMap();

		NBTTagCompound tag = stack.getSubCompound("BlockEntityTag", false);
		if (tag != null && tag.hasKey("Patterns")) {
			NBTTagList nbttaglist = tag.getTagList("Patterns", 10);

			for (int i = 0; i < nbttaglist.tagCount() && i < 6; ++i) {
				NBTTagCompound patternTag = nbttaglist.getCompoundTagAt(i);

				EnumDyeColor color = EnumDyeColor.byDyeDamage(patternTag.getInteger("Color"));
				TileEntityBanner.EnumBannerPattern pattern = TileEntityBanner.EnumBannerPattern.getPatternByID(patternTag.getString("Pattern"));

				if (pattern != null) {
					Map<String, String> entry = Maps.newHashMap();
					entry.put("id", pattern.getPatternID());
					entry.put("name", pattern.getPatternName());
					entry.put("colour", color.toString());
					entry.put("color", color.toString());

					out.put(++idx, entry);
				}
			}
		}

		return out;
	}
}
