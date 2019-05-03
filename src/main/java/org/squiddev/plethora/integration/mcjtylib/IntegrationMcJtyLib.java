package org.squiddev.plethora.integration.mcjtylib;

import mcjty.lib.McJtyLib;
import mcjty.lib.base.GeneralConfig;
import mcjty.lib.tileentity.GenericTileEntity;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.utils.EntityPlayerDummy;
import org.squiddev.plethora.utils.WorldDummy;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Injects(McJtyLib.PROVIDES)
public final class IntegrationMcJtyLib {
	private IntegrationMcJtyLib() {
	}

	public static final IMetaProvider<GenericTileEntity> META_GENERIC_TILE = new BasicMetaProvider<GenericTileEntity>() {

		@Nonnull
		@Override
		public Map<String, ?> getMeta(@Nonnull GenericTileEntity context) {
			Map<String, Object> out = new HashMap<>(4);

			if (!GeneralConfig.manageOwnership) {
				out.put("ownerName", context.getOwnerName());

				UUID owner = context.getOwnerUUID();
				if (owner != null) out.put("ownerUUID", owner.toString());
			}

			out.put("infusion", context.getInfused());

			//REFINE Do we want to provide the max infusion on each `GenericTileEntity`,
			// provide it via a method, or provide `getInfusedFactor` as a percentage?
			out.put("infusionMax", GeneralConfig.maxInfuse);

			return out;
		}

		@Nonnull
		@Override
		public GenericTileEntity getExample() {
			GenericTileEntity tile = new GenericTileEntity();
			tile.setInfused(5);
			tile.setOwner(new EntityPlayerDummy(WorldDummy.INSTANCE));

			return tile;
		}
	};

}
