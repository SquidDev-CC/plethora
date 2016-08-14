package org.squiddev.plethora.integration.computercraft;

import com.google.common.collect.Maps;
import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.media.IMedia;
import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

/**
 * Provider for all media items (disks and records)
 */
@IMetaProvider.Inject(value = ItemStack.class, modId = "ComputerCraft", namespace = "media")
public class MetaItemMedia extends BasicMetaProvider<ItemStack> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull ItemStack object) {
		IMedia media = object.getItem() instanceof IMedia ? (IMedia) object.getItem() : ComputerCraft.getMedia(object);
		if (media == null) return Collections.emptyMap();

		Map<Object, Object> out = Maps.newHashMap();
		out.put("label", media.getLabel(object));
		out.put("recordTitle", media.getAudioTitle(object));
		out.put("recordName", media.getAudioRecordName(object));

		return out;
	}
}
