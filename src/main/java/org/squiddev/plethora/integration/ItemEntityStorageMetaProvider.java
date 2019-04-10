package org.squiddev.plethora.integration;

import joptsimple.internal.Strings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.util.Constants;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.meta.ItemStackContextMetaProvider;
import org.squiddev.plethora.api.method.ContextKeys;
import org.squiddev.plethora.api.method.IPartialContext;
import org.squiddev.plethora.utils.Helpers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class ItemEntityStorageMetaProvider<T extends Item> extends ItemStackContextMetaProvider<T> {
	private final String id;

	protected ItemEntityStorageMetaProvider(String id, Class<T> type, int priority, String description) {
		super(type, priority, description);
		this.id = id;
	}

	protected ItemEntityStorageMetaProvider(String id, Class<T> type, String description) {
		super(type, description);
		this.id = id;
	}

	@Nonnull
	@Override
	public final Map<String, ?> getMeta(@Nonnull IPartialContext<ItemStack> context, @Nonnull T item) {
		IWorldLocation location = context.getContext(ContextKeys.ORIGIN, IWorldLocation.class);
		if (location != null) {
			Entity entity = spawn(context.getTarget(), item, location);
			if (entity != null) {
				Vec3d pos = location.getLoc();
				entity.setPositionAndRotation(pos.x, pos.y, pos.z, 0, 0);
				return Collections.singletonMap(id, context.makePartialChild(entity).getMeta());
			}
		}

		Map<String, ?> basic = getBasicDetails(context.getTarget(), item);
		return basic.isEmpty() ? Collections.emptyMap() : Collections.singletonMap(id, basic);
	}

	@Nullable
	protected abstract Entity spawn(@Nonnull ItemStack stack, @Nonnull T item, @Nonnull IWorldLocation location);

	@Nonnull
	protected abstract Map<String, ?> getBasicDetails(@Nonnull ItemStack stack, @Nonnull T item);

	@Nonnull
	public static Map<String, ?> getBasicDetails(@Nullable NBTTagCompound entityData) {
		if (entityData == null || !entityData.hasKey("id", Constants.NBT.TAG_STRING)) return Collections.emptyMap();
		return getBasicDetails(new ResourceLocation(entityData.getString("id")), entityData);
	}

	@Nonnull
	protected static Map<String, ?> getBasicDetails(@Nonnull ResourceLocation id, @Nullable NBTTagCompound entityData) {
		return getBasicDetails(
			id,
			entityData != null && entityData.hasKey("CustomName", Constants.NBT.TAG_STRING) ? entityData.getString("CustomName") : null
		);
	}

	@Nonnull
	protected static Map<String, ?> getBasicDetails(@Nonnull ResourceLocation id, @Nullable String displayName) {
		String translationKey = EntityList.getTranslationName(id);
		if (translationKey == null) return Collections.emptyMap();

		String translated = Helpers.translateToLocal("entity." + translationKey + ".name");

		Map<String, Object> details = new HashMap<>(2);
		details.put("name", translated);
		details.put("displayName", Strings.isNullOrEmpty(displayName) ? translated : displayName);
		return details;
	}
}
