package org.squiddev.plethora.integration.thermalexpansion;

import cofh.thermalexpansion.ThermalExpansion;
import cofh.thermalexpansion.item.ItemMorb;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.util.Constants;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.api.meta.ItemStackContextMetaProvider;
import org.squiddev.plethora.api.method.ContextKeys;
import org.squiddev.plethora.api.method.IPartialContext;
import org.squiddev.plethora.utils.Helpers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Injects(ThermalExpansion.MOD_ID)
public final class IntegrationThermalExpansion {
	public static final IMetaProvider<ItemStack> META_MORB = new ItemStackContextMetaProvider<ItemMorb>(
		ItemMorb.class,
		"Provides the entity captured inside this Morb."
	) {
		@Nonnull
		@Override
		public Map<String, ?> getMeta(@Nonnull IPartialContext<ItemStack> context, @Nonnull ItemMorb item) {
			NBTTagCompound entityData = context.getTarget().getTagCompound();
			if (entityData == null || !entityData.hasKey("id", Constants.NBT.TAG_STRING)) return Collections.emptyMap();

			IWorldLocation location = context.getContext(ContextKeys.ORIGIN, IWorldLocation.class);
			if (location == null) return getBasicDetails(entityData);

			Entity entity = entityData.getBoolean(ItemMorb.GENERIC)
				? EntityList.createEntityByIDFromName(new ResourceLocation(entityData.getString("id")), location.getWorld())
				: EntityList.createEntityFromNBT(entityData, location.getWorld());
			if (entity == null) return getBasicDetails(entityData);

			Vec3d loc = location.getLoc();
			entity.setPositionAndRotation(loc.x, loc.y, loc.z, 0, 0);
			return Collections.singletonMap("capturedEntity", context.makePartialChild(entity).getMeta());
		}

		private Map<String, Object> getBasicDetails(NBTTagCompound entityData) {
			String translationKey = EntityList.getTranslationName(new ResourceLocation(entityData.getString("id")));
			if (translationKey == null) return Collections.emptyMap();

			String translated = Helpers.translateToLocal("entity." + translationKey + ".name");

			Map<String, Object> details = new HashMap<>(2);
			details.put("name", translated);
			details.put("displayName",
				entityData.hasKey("CustomName", Constants.NBT.TAG_STRING)
					? entityData.getString("CustomName")
					: translated
			);
			return Collections.singletonMap("capturedEntity", details);
		}

		@Nullable
		@Override
		public ItemStack getExample() {
			return ItemMorb.morbList.isEmpty() ? null : ItemMorb.morbList.get(0);
		}
	};

	private IntegrationThermalExpansion() {
	}
}
