package org.squiddev.plethora.integration.enderio;

import com.enderio.core.common.TileEntityBase;
import crazypants.enderio.base.EnderIO;
import crazypants.enderio.base.init.ModObject;
import crazypants.enderio.base.item.soulvial.ItemSoulVial;
import crazypants.enderio.util.CapturedMob;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.converter.ConstantConverter;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.api.reference.BlockReference;
import org.squiddev.plethora.integration.ItemEntityStorageMetaProvider;
import org.squiddev.plethora.utils.CapabilityWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Injects(EnderIO.MODID)
public final class IntegrationEnderIO {
	private IntegrationEnderIO() {
	}

	public static final IMetaProvider<ItemStack> META_SOUL_VIAL = new ItemEntityStorageMetaProvider<ItemSoulVial>(
		"capturedEntity", ItemSoulVial.class,
		"Provides the entity captured inside this Soul Vial."
	) {
		@Nullable
		@Override
		protected Entity spawn(@Nonnull ItemStack stack, @Nonnull ItemSoulVial item, @Nonnull IWorldLocation location) {
			CapturedMob mob = CapturedMob.create(stack);
			return mob == null ? null : mob.getEntity(location.getWorld(), location.getPos(), null, false);
		}

		@Nonnull
		@Override
		protected Map<String, ?> getBasicDetails(@Nonnull ItemStack stack, @Nonnull ItemSoulVial item) {
			CapturedMob mob = CapturedMob.create(stack);
			if (mob == null) return Collections.emptyMap();

			Map<String, Object> details = new HashMap<>(2);
			details.put("name", mob.getTranslationName());
			details.put("displayName", mob.getDisplayName());
			return details;
		}

		@Nullable
		@Override
		public ItemStack getExample() {
			CapturedMob mob = CapturedMob.create(EntityList.getKey(EntityCow.class));
			Item item = ModObject.itemSoulVial.getItem();
			return mob != null && item != null ? mob.toStack(item, 1, 1) : null;
		}
	};

	/**
	 * Provide a capability provider which explicitly uses a specific side.
	 *
	 * EnderIO returns {@code null} for the internal side (https://github.com/SleepyTrousers/EnderIO/issues/4840),
	 * which breaks any of our existing capability converters. We bodge around this by exposing a secondary capability
	 * provider which uses the side we're up against.
	 */
	public static final ConstantConverter<BlockReference, ICapabilityProvider> ENDERIO_CAPABILITIES = x -> {
		TileEntity tile = x.getTileEntity();
		return tile instanceof TileEntityBase && x.getSide() != null
			? new CapabilityWrapper(tile, x.getSide())
			: null;
	};
}
