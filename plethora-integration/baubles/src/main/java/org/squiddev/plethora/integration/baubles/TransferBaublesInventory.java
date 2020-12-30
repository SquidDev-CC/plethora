package org.squiddev.plethora.integration.baubles;

import baubles.api.BaublesApi;
import baubles.common.Baubles;
import net.minecraft.entity.player.EntityPlayer;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.transfer.ITransferProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;

/**
 * Provides an inventory called "baubles" for players
 */
@Injects(Baubles.MODID)
public final class TransferBaublesInventory implements ITransferProvider<EntityPlayer> {
	@Nullable
	@Override
	public Object getTransferLocation(@Nonnull EntityPlayer object, @Nonnull String key) {
		return key.equals("baubles") ? BaublesApi.getBaublesHandler(object) : null;
	}

	@Nonnull
	@Override
	public Set<String> getTransferLocations(@Nonnull EntityPlayer object) {
		return Collections.singleton("baubles");
	}
}
