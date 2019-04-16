package org.squiddev.plethora.integration.astralsorcery;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.auxiliary.CelestialGatewaySystem;
import hellfirepvp.astralsorcery.common.data.world.WorldCacheManager;
import hellfirepvp.astralsorcery.common.data.world.data.GatewayCache;
import hellfirepvp.astralsorcery.common.tile.TileCelestialGateway;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.relauncher.Side;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.method.ContextKeys;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.LuaList;
import org.squiddev.plethora.api.method.wrapper.PlethoraMethod;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MethodsAstralSorcery {
	private MethodsAstralSorcery() {
	}

	//REFINE This method is potentially OP, as it exposes all available `GatewayNode`s.
	// That said, players can already name a gateway, and they could add signs to the destination
	// so that it shows in the preview (if it is working; haven't tested)
	// If Hellfire complains, it's easy enough to delete...
	@PlethoraMethod(
		modId = AstralSorcery.MODID,
		doc = "-- Get a list of all Celestial Gateways, grouped by dimension"
	)
	public static Map<String, Object> getGateways(@Nonnull IContext<TileCelestialGateway> context) {
		//REFINE Should be able to convert this to an annotated method arg...
		IWorldLocation location = context.getContext(ContextKeys.ORIGIN, IWorldLocation.class);
		if (location == null) return Collections.singletonMap("error", "Failed to get list of gateways, world data not in context.");

		//TODO Confirm that this code does, in fact, only run on the server side... don't want to break things!
		Map<Integer, List<GatewayCache.GatewayNode>> nodesByDimension = CelestialGatewaySystem.instance.getGatewayCache(Side.SERVER);
		Map<String, Object> fullOut = new HashMap<>();

		//TODO This will break badly if dimensions aren't identified by number, e.g. 1.13, NEID, JEID, etc.
		for (Integer id : nodesByDimension.keySet()) {
			// I was going to filter this for the current node, but that will result in excessive collection manipulation
			// if I can't convert to a stream API chain; otherwise, I would risk modifying the actual server data...
			List<GatewayCache.GatewayNode> dimNodes = nodesByDimension.get(id);
			LuaList<Map<String, Object>> dimOut = new LuaList<>(dimNodes.size());
			for (GatewayCache.GatewayNode node : dimNodes) {
				Map<String, Object> inner = new HashMap<>();
				inner.put("posX", node.getX());
				inner.put("posY", node.getY());
				inner.put("posZ", node.getZ());
				inner.put("name", node.display);
				dimOut.add(inner);
			}

			//TODO Determine how to get a dimension's name; `DimensionType.getById(int).getName` is a start, but
			// it doesn't account for named dimensions, e.g. RFTools Dimensions, Mystcraft, etc. ...
			//REFINE I recall there being a difference between `String.valueOf` and `toString` for primitives, but I
			// don't remember the specifics...
			fullOut.put(String.valueOf(id), dimOut.asMap());
		}

		return fullOut;
	}
}
