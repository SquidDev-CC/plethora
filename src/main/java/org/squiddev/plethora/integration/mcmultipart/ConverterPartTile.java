package org.squiddev.plethora.integration.mcmultipart;

import mcmultipart.MCMultiPart;
import mcmultipart.api.container.IPartInfo;
import mcmultipart.api.multipart.IMultipartTile;
import net.minecraft.tileentity.TileEntity;
import org.squiddev.plethora.api.converter.ConstantConverter;
import org.squiddev.plethora.api.converter.IConverter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@IConverter.Inject(value = IPartInfo.class, modId = MCMultiPart.MODID)
public class ConverterPartTile extends ConstantConverter<IPartInfo, TileEntity> {
	@Nullable
	@Override
	@SuppressWarnings("deprecation")
	public TileEntity convert(@Nonnull IPartInfo part) {
		IMultipartTile tile = part.getTile();
		return tile == null ? null : tile.getTileEntity();
	}
}
