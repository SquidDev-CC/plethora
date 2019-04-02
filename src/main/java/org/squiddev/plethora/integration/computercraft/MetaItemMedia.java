package org.squiddev.plethora.integration.computercraft;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.media.IMedia;
import dan200.computercraft.shared.MediaProviders;
import dan200.computercraft.shared.media.items.ItemDiskExpanded;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Provider for all media items (disks and records)
 */
@IMetaProvider.Inject(value = ItemStack.class, modId = ComputerCraft.MOD_ID, namespace = "media")
public class MetaItemMedia extends BasicMetaProvider<ItemStack> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull ItemStack object) {
		IMedia media = MediaProviders.get(object);
		if (media == null) return Collections.emptyMap();

		Map<Object, Object> out = new HashMap<>();
		out.put("label", media.getLabel(object));
		out.put("recordTitle", media.getAudioTitle(object));

		SoundEvent soundEvent = media.getAudio(object);
		if (soundEvent != null) {
			out.put("recordName", ObfuscationReflectionHelper.getPrivateValue(SoundEvent.class, soundEvent, "field_187506_b").toString());
		}

		return out;
	}

	@Nullable
	@Override
	public ItemStack getExample() {
		return ItemDiskExpanded.createFromIDAndColour(3, "My disk", 0xFF0000);
	}
}
