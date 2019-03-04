package org.squiddev.plethora.integration.enderio;

import crazypants.enderio.base.EnderIO;
import crazypants.enderio.base.item.soulvial.ItemSoulVial;
import crazypants.enderio.util.CapturedMob;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.api.meta.ItemStackContextMetaProvider;
import org.squiddev.plethora.api.method.ContextKeys;
import org.squiddev.plethora.api.method.IPartialContext;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Injects(EnderIO.MODID)
public class IntegrationEnderIO {
	public static final IMetaProvider<ItemStack> META_SOUL_VIAL = new ItemStackContextMetaProvider<ItemSoulVial>(ItemSoulVial.class) {
		@Nonnull
		@Override
		public Map<Object, Object> getMeta(@Nonnull IPartialContext<ItemStack> context, @Nonnull ItemSoulVial item) {
			IWorldLocation location = context.getContext(ContextKeys.ORIGIN, IWorldLocation.class);
			if (location == null) return Collections.emptyMap();

			CapturedMob mob = CapturedMob.create(context.getTarget());
			if (mob == null) return Collections.emptyMap();

			Entity entity = mob.getEntity(location.getWorld(), location.getPos(), null, false);
			if (entity == null) {
				Map<Object, Object> details = new HashMap<>(2);
				details.put("name", mob.getTranslationName());
				details.put("displayName", mob.getDisplayName());
				return Collections.singletonMap("capturedEntity", details);
			} else {
				return Collections.singletonMap("capturedEntity", context.makePartialChild(entity).getMeta());
			}
		}
	};
}
