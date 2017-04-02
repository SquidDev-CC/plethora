package org.squiddev.plethora.api.minecart;

import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import org.squiddev.plethora.api.Constants;
import org.squiddev.plethora.api.PlethoraAPI;
import org.squiddev.plethora.api.module.BasicModuleHandler;

import javax.annotation.Nonnull;

/**
 * A {@link BasicModuleHandler} which also provides a {@link IMinecartUpgradeHandler}.
 */
public class MinecartModuleHandler extends BasicModuleHandler {
	private IMinecartUpgradeHandler handler;

	public MinecartModuleHandler(ResourceLocation id, Item item) {
		super(id, item);
	}

	protected IMinecartUpgradeHandler createMinecart() {
		return PlethoraAPI.instance().moduleRegistry().toMinecartUpgrade(this);
	}


	@Override
	public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing enumFacing) {
		return super.hasCapability(capability, enumFacing) || capability == Constants.MINECART_UPGRADE_HANDLER_CAPABILITY;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing enumFacing) {
		if (capability == Constants.MINECART_UPGRADE_HANDLER_CAPABILITY) {
			IMinecartUpgradeHandler upgrade = handler;
			if (upgrade == null) {
				upgrade = this.handler = createMinecart();
			}
			return (T) upgrade;
		}

		return super.getCapability(capability, enumFacing);
	}
}
