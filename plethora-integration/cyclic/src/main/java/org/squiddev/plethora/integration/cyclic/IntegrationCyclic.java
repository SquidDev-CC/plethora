package org.squiddev.plethora.integration.cyclic;

import com.lothrazar.cyclicmagic.item.mobcapture.ItemProjectileMagicNet;
import com.lothrazar.cyclicmagic.util.Const;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.integration.ItemEntityStorageMetaProvider;
import org.squiddev.plethora.utils.WorldDummy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

import static com.lothrazar.cyclicmagic.item.mobcapture.ItemProjectileMagicNet.NBT_ENTITYID;

@Injects(Const.MODID)
public final class IntegrationCyclic {
	public static final IMetaProvider<ItemStack> META_MONSTER_NET = new ItemEntityStorageMetaProvider<ItemProjectileMagicNet>(
		"capturedEntity", ItemProjectileMagicNet.class,
		"Provides the entity captured inside this monster net."
	) {
		@Nullable
		@Override
		protected Entity spawn(@Nonnull ItemStack stack, @Nonnull ItemProjectileMagicNet item, @Nonnull IWorldLocation location) {
			NBTTagCompound entityData = stack.getTagCompound();
			if (entityData == null || !entityData.hasKey(NBT_ENTITYID, Constants.NBT.TAG_STRING)) return null;
			return EntityList.createEntityFromNBT(entityData, location.getWorld());
		}

		@Nonnull
		@Override
		protected Map<String, ?> getBasicDetails(@Nonnull ItemStack stack, @Nonnull ItemProjectileMagicNet item) {
			return getBasicDetails(stack.getTagCompound());
		}

		@Nullable
		@Override
		public ItemStack getExample() {
			Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(Const.MODID, "magic_net"));
			if (!(item instanceof ItemProjectileMagicNet)) return null;

			ItemStack stack = new ItemStack(item);
			NBTTagCompound tag = new NBTTagCompound();
			Entity entity = new EntitySquid(WorldDummy.INSTANCE);
			entity.writeToNBT(tag);
			tag.setString(NBT_ENTITYID, EntityList.getKey(entity.getClass()).toString());
			stack.setTagCompound(tag);
			return stack;
		}
	};

	private IntegrationCyclic() {
	}
}
