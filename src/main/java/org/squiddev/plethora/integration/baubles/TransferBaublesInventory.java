package org.squiddev.plethora.integration.baubles;

import baubles.api.BaublesApi;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.squiddev.plethora.api.transfer.ITransferProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;

/**
 * Provides an inventory called "baubles" for players
 */
@ITransferProvider.Inject(value = EntityPlayer.class, modId = "Baubles")
public class TransferBaublesInventory implements ITransferProvider<EntityPlayer> {
	@Nullable
	@Override
	public Object getTransferLocation(@Nonnull EntityPlayer object, @Nonnull String key) {
		return key.equals("baubles") ? new InvWrapper(BaublesApi.getBaubles(object)) : null;
	}

	@Nonnull
	@Override
	public Set<String> getTransferLocations(@Nonnull EntityPlayer object) {
		return Collections.singleton("baubles");
	}
}
