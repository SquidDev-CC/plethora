package org.squiddev.plethora.integration.extrautilities;

import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.items.ItemGoldenLasso;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.integration.ItemEntityStorageMetaProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

import static com.rwtema.extrautils2.items.ItemGoldenLasso.NBT_ANIMAL;

@Injects(ExtraUtils2.MODID)
public final class IntegrationExtraUtilities {
	public static final IMetaProvider<ItemStack> META_MONSTER_NET = new ItemEntityStorageMetaProvider<ItemGoldenLasso>(
		"capturedEntity", ItemGoldenLasso.class,
		"Provides the entity captured inside this lasso."
	) {
		@Nullable
		@Override
		protected Entity spawn(@Nonnull ItemStack stack, @Nonnull ItemGoldenLasso item, @Nonnull IWorldLocation location) {
			NBTTagCompound tag = stack.getTagCompound();
			if (tag == null || !tag.hasKey(NBT_ANIMAL, Constants.NBT.TAG_COMPOUND)) return null;

			NBTTagCompound entityData = tag.getCompoundTag(NBT_ANIMAL);
			if (!entityData.hasKey("id", Constants.NBT.TAG_STRING)) return null;

			return EntityList.createEntityFromNBT(entityData, location.getWorld());
		}

		@Nonnull
		@Override
		protected Map<String, ?> getBasicDetails(@Nonnull ItemStack stack, @Nonnull ItemGoldenLasso item) {
			return getBasicDetails(stack.getTagCompound());
		}

		@Nonnull
		@Override
		public ItemStack getExample() {
			return ItemGoldenLasso.newCraftingStack(EntitySquid.class);
		}
	};

	private IntegrationExtraUtilities() {
	}
}
