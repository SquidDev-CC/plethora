package org.squiddev.plethora.integration.vanilla.meta;

import com.google.common.collect.Maps;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.api.meta.MetaProvider;

import java.util.HashMap;
import java.util.Map;

@MetaProvider(value = TileEntity.class, namespace = "location")
public class MetaTileBasic implements IMetaProvider<TileEntity> {
	@Override
	public Map<String, Object> getMeta(TileEntity object) {
		HashMap<String, Object> position = Maps.newHashMap();
		BlockPos pos = object.getPos();
		position.put("x", pos.getX());
		position.put("y", pos.getY());
		position.put("z", pos.getZ());

		return position;
	}
}
