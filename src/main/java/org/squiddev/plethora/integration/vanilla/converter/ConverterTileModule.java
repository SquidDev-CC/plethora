package org.squiddev.plethora.integration.vanilla.converter;

import net.minecraft.tileentity.TileEntity;
import org.squiddev.plethora.api.Constants;
import org.squiddev.plethora.api.converter.DynamicConverter;
import org.squiddev.plethora.api.converter.IConverter;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.api.module.IModuleHandler;
import org.squiddev.plethora.api.module.SingletonModuleContainer;
import org.squiddev.plethora.core.ConfigCore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@IConverter.Inject(TileEntity.class)
public class ConverterTileModule extends DynamicConverter<TileEntity, IModuleContainer> {
	@Nullable
	@Override
	public IModuleContainer convert(@Nonnull TileEntity from) {
		if (!from.hasCapability(Constants.MODULE_HANDLER_CAPABILITY, null)) return null;

		IModuleHandler handler = from.getCapability(Constants.MODULE_HANDLER_CAPABILITY, null);
		if (handler == null) return null;

		String moduleName = handler.getModule().toString();
		if (ConfigCore.Blacklist.blacklistModules.contains(moduleName) || ConfigCore.Blacklist.blacklistModulesTile.contains(moduleName)) {
			return null;
		}

		return new SingletonModuleContainer(handler.getModule());
	}
}
