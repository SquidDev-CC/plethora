package org.squiddev.plethora.integration.vanilla.transfer;

import com.google.common.collect.Sets;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;
import org.squiddev.plethora.api.transfer.ITransferProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

/**
 * Provides the inventory and ender chest of a player
 */
@ITransferProvider.Inject(EntityPlayer.class)
public class TransferEntityPlayerInventory implements ITransferProvider<EntityPlayer> {
	@Nullable
	@Override
	public Object getTransferLocation(@Nonnull EntityPlayer object, @Nonnull String key) {
		if (key.equals("inventory")) return new PlayerMainInvWrapper(object.inventory);
		if (key.equals("ender_chest")) return new InvWrapper(object.getInventoryEnderChest());
		return null;
	}

	@Nonnull
	@Override
	public Set<String> getTransferLocations(@Nonnull EntityPlayer object) {
		return Sets.newHashSet("inventory", "ender_chest");
	}
}
