package org.squiddev.plethora.integration.plethora;

import com.mojang.authlib.GameProfile;
import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.gameplay.Plethora;
import org.squiddev.plethora.gameplay.modules.ItemModule;
import org.squiddev.plethora.gameplay.registry.Registry;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@IMetaProvider.Inject(value = ItemStack.class, modId = Plethora.ID)
public class MetaItemModule extends BasicMetaProvider<ItemStack> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull ItemStack stack) {
		if (stack.getItem() != Registry.itemModule) return Collections.emptyMap();

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
}
