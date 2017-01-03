package org.squiddev.plethora.integration.vanilla.converter;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.squiddev.plethora.api.Constants;
import org.squiddev.plethora.api.converter.IConverter;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.api.module.IModuleHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;

@IConverter.Inject(TileEntity.class)
public class ConverterTileModule implements IConverter<TileEntity, IModuleContainer> {
	@Nullable
	@Override
	public IModuleContainer convert(@Nonnull TileEntity from) {
		IModuleHandler handler = from.getCapability(Constants.MODULE_HANDLER_CAPABILITY, null);
		if (handler == null) return null;

		final ResourceLocation existingModule = handler.getModule();

		return new IModuleContainer() {
			@Override
			public boolean hasModule(@Nonnull ResourceLocation module) {
				return module.equals(existingModule);
			}

			@Nonnull
			@Override
			public Set<ResourceLocation> getModules() {
				return Collections.singleton(existingModule);
			}
		};
	}
}
