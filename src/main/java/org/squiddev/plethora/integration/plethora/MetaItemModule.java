package org.squiddev.plethora.integration.plethora;

import com.mojang.authlib.GameProfile;
import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.ItemStackMetaProvider;
import org.squiddev.plethora.gameplay.Plethora;
import org.squiddev.plethora.gameplay.modules.ItemModule;
import org.squiddev.plethora.gameplay.registry.Registry;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

@Injects(Plethora.ID)
public final class MetaItemModule extends ItemStackMetaProvider<ItemModule> {
	public MetaItemModule() {
		super(ItemModule.class);
	}

	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull ItemStack stack, @Nonnull ItemModule module) {
		Map<Object, Object> result = new HashMap<>();

		GameProfile profile = ItemModule.getProfile(stack);
		if (profile != null) {
			Map<String, Object> bound = new HashMap<>();
			result.put("bound", bound);

			if (profile.getId() != null) bound.put("id", profile.getId().toString());
			bound.put("name", profile.getName());
		}

		return result;
	}

	@Nonnull
	@Override
	public ItemStack getExample() {
		return new ItemStack(Registry.itemModule);
	}
}
