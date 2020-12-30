package org.squiddev.plethora.integration.computercraft;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.media.IMedia;
import dan200.computercraft.shared.MediaProviders;
import dan200.computercraft.shared.media.items.ItemDiskExpanded;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.utils.TypedField;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Provider for all media items (disks and records)
 */
@Injects(ComputerCraft.MOD_ID)
public final class MetaItemMedia extends BasicMetaProvider<ItemStack> {
	private static final TypedField<SoundEvent, ResourceLocation> FIELD_SOUND_NAME = TypedField.of(SoundEvent.class, "soundName", "field_187506_b");

	@Nonnull
	@Override
	public Map<String, ?> getMeta(@Nonnull ItemStack object) {
		IMedia media = MediaProviders.get(object);
		if (media == null) return Collections.emptyMap();

		Map<String, Object> out = new HashMap<>(3);
		out.put("label", media.getLabel(object));
		out.put("recordTitle", media.getAudioTitle(object));

		SoundEvent soundEvent = media.getAudio(object);
		if (soundEvent != null) {
			ResourceLocation id = FIELD_SOUND_NAME.get(soundEvent);
			if (id != null) out.put("recordName", id.toString());
		}

		return Collections.singletonMap("media", out);
	}

	@Nonnull
	@Override
	public ItemStack getExample() {
		return ItemDiskExpanded.createFromIDAndColour(3, "My disk", 0xFF0000);
	}
}
