package org.squiddev.plethora.integration.appliedenergistics;

import appeng.api.storage.data.IAEFluidStack;
import appeng.core.AppEng;
import net.minecraftforge.fluids.FluidStack;
import org.squiddev.plethora.api.converter.IConverter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@IConverter.Inject(value = IAEFluidStack.class, modId = AppEng.MOD_ID)
public class ConverterAEFluidStack implements IConverter<IAEFluidStack, FluidStack> {
	@Nullable
	@Override
	public FluidStack convert(@Nonnull IAEFluidStack from) {
		return from.getFluidStack();
	}
}
