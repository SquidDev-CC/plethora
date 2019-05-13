package org.squiddev.plethora.integration.industrialforegoing;

import com.buuz135.industrial.item.MobImprisonmentToolItem;
import com.buuz135.industrial.proxy.ItemRegistry;
import com.buuz135.industrial.utils.Reference;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.integration.ItemEntityStorageMetaProvider;
import org.squiddev.plethora.utils.WorldDummy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;

@Injects(Reference.MOD_ID)
public final class IntegrationIF {
	public static final IMetaProvider<ItemStack> META_MOB_IMPRISONMENT = new ItemEntityStorageMetaProvider<MobImprisonmentToolItem>(
		"capturedEntity", MobImprisonmentToolItem.class,
		"Provides the entity captured inside this mob imprisonment tool."
	) {
		@Nullable
		@Override
		protected Entity spawn(@Nonnull ItemStack stack, @Nonnull MobImprisonmentToolItem item, @Nonnull IWorldLocation location) {
			return item.containsEntity(stack) ? item.getEntityFromStack(stack, location.getWorld(), true) : null;
		}

		@Nonnull
		@Override
		protected Map<String, ?> getBasicDetails(@Nonnull ItemStack stack, @Nonnull MobImprisonmentToolItem item) {
			return item.containsEntity(stack)
				? getBasicDetails(new ResourceLocation(item.getID(stack)), stack.getTagCompound())
				: Collections.emptyMap();
		}

		@Nonnull
		@Override
		public ItemStack getExample() {
			ItemStack stack = new ItemStack(ItemRegistry.mobImprisonmentToolItem);
			Entity entity = new EntitySquid(WorldDummy.INSTANCE);
			NBTTagCompound tag = new NBTTagCompound();
			tag.setString("entity", EntityList.getKey(entity).toString());
			tag.setInteger("id", EntityList.getID(entity.getClass()));
			entity.writeToNBT(tag);
			stack.setTagCompound(tag);
			return stack;
		}
	};

	private IntegrationIF() {
	}
}
