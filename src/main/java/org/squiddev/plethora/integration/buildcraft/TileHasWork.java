package org.squiddev.plethora.integration.buildcraft;

import buildcraft.api.tiles.IHasWork;
import buildcraft.core.BCCore;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.method.wrapper.FromTarget;
import org.squiddev.plethora.api.method.wrapper.PlethoraMethod;

@Injects(BCCore.MODID)
public final class TileHasWork {

	@PlethoraMethod(modId = BCCore.MODID, doc = "-- Reports true if the Block has work to do.")
	public static boolean hasWork(@FromTarget IHasWork hasWork) {
		return hasWork.hasWork();
	}
}
