package org.squiddev.plethora.integration.thermalexpansion;

import cofh.thermalexpansion.ThermalExpansion;
import cofh.thermalexpansion.item.ItemMorb;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.integration.ItemEntityStorageMetaProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;

@Injects(ThermalExpansion.MOD_ID)
public final class IntegrationThermalExpansion {
	public static final IMetaProvider<ItemStack> META_MORB = new ItemEntityStorageMetaProvider<ItemMorb>(
		"capturedEntity", ItemMorb.class,
		"Provides the entity captured inside this Morb."
	) {
		@Nullable
		@Override
		protected Entity spawn(@Nonnull ItemStack stack, @Nonnull ItemMorb item, @Nonnull IWorldLocation location) {
			NBTTagCompound entityData = stack.getTagCompound();
			if (entityData == null || !entityData.hasKey("id", Constants.NBT.TAG_STRING)) return null;

			return entityData.getBoolean(ItemMorb.GENERIC)
				? EntityList.createEntityByIDFromName(new ResourceLocation(entityData.getString("id")), location.getWorld())
				: EntityList.createEntityFromNBT(entityData, location.getWorld());
		}

		@Nonnull
		@Override
		protected Map<String, ?> getBasicDetails(@Nonnull ItemStack stack, @Nonnull ItemMorb item) {
			NBTTagCompound tag = stack.getTagCompound();
			if (tag == null || !tag.hasKey("id", Constants.NBT.TAG_STRING)) return Collections.emptyMap();
			return getBasicDetails(new ResourceLocation(tag.getString("id")), tag);
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
