package org.squiddev.plethora.integration.enderio;

import crazypants.enderio.base.EnderIO;
import crazypants.enderio.base.init.ModObject;
import crazypants.enderio.base.item.soulvial.ItemSoulVial;
import crazypants.enderio.util.CapturedMob;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.api.meta.ItemStackContextMetaProvider;
import org.squiddev.plethora.api.method.ContextKeys;
import org.squiddev.plethora.api.method.IPartialContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Injects(EnderIO.MODID)
public final class IntegrationEnderIO {
	public static final IMetaProvider<ItemStack> META_SOUL_VIAL = new ItemStackContextMetaProvider<ItemSoulVial>(
		ItemSoulVial.class,
		"Provides the entity captured inside this Soul Vial."
	) {
		@Nonnull
		@Override
		public Map<String, ?> getMeta(@Nonnull IPartialContext<ItemStack> context, @Nonnull ItemSoulVial item) {
			CapturedMob mob = CapturedMob.create(context.getTarget());
			if (mob == null) return Collections.emptyMap();

			IWorldLocation location = context.getContext(ContextKeys.ORIGIN, IWorldLocation.class);
			if (location == null) return getBasic(mob);

			Entity entity = mob.getEntity(location.getWorld(), location.getPos(), null, false);
			if (entity == null) return getBasic(mob);

			return Collections.singletonMap("capturedEntity", context.makePartialChild(entity).getMeta());
		}

		private Map<String, Object> getBasic(CapturedMob mob) {
			Map<String, Object> details = new HashMap<>(2);
			details.put("name", mob.getTranslationName());
			details.put("displayName", mob.getDisplayName());
			return Collections.singletonMap("capturedEntity", details);
		}

		@Nullable
		@Override
		public ItemStack getExample() {
			CapturedMob mob = CapturedMob.create(EntityList.getKey(EntityCow.class));
			Item item = ModObject.itemSoulVial.getItem();
			return mob != null && item != null ? mob.toStack(item, 1, 1) : null;
		}
	};

	private IntegrationEnderIO() {
	}
}
