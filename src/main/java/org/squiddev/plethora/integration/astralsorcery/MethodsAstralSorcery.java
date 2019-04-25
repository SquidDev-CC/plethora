package org.squiddev.plethora.integration.astralsorcery;

import dan200.computercraft.api.lua.LuaException;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.auxiliary.CelestialGatewaySystem;
import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ResearchManager;
import hellfirepvp.astralsorcery.common.data.research.ResearchProgression;
import hellfirepvp.astralsorcery.common.data.world.data.GatewayCache;
import hellfirepvp.astralsorcery.common.tile.TileCelestialGateway;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.relauncher.Side;
import org.squiddev.plethora.api.meta.TypedMeta;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.LuaList;
import org.squiddev.plethora.api.method.wrapper.FromSubtarget;
import org.squiddev.plethora.api.method.wrapper.FromTarget;
import org.squiddev.plethora.api.method.wrapper.PlethoraMethod;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.gameplay.modules.PlethoraModules;
import org.squiddev.plethora.integration.EntityIdentifier;

import javax.annotation.Nonnull;
import java.util.*;

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
	public static Map<String, ?> getGateways(@FromTarget TileCelestialGateway gateway) {
		Map<Integer, List<GatewayCache.GatewayNode>> nodesByDimension = CelestialGatewaySystem.instance.getGatewayCache(Side.SERVER);
		Map<String, Object> fullOut = new HashMap<>(nodesByDimension.size());

		//TODO This will break badly if dimensions aren't identified by number, e.g. 1.13, NEID, JEID, etc.
		for (Map.Entry<Integer, List<GatewayCache.GatewayNode>> entry : nodesByDimension.entrySet()) {
			// I was going to filter this for the current node, but that will result in excessive collection manipulation
			// if I can't convert to a stream API chain; otherwise, I would risk modifying the actual server data...
			List<GatewayCache.GatewayNode> dimNodes = entry.getValue();
			LuaList<Map<String, Object>> dimOut = new LuaList<>(dimNodes.size());
			for (GatewayCache.GatewayNode node : dimNodes) {
				Map<String, Object> inner = new HashMap<>(4);
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
			fullOut.put(String.valueOf(entry.getKey()), dimOut.asMap());
		}

		return fullOut;
	}

	//Mainly useful if a player wants to design some sort of organizer to track their progress
	@PlethoraMethod(
		modId = AstralSorcery.MODID,
		module = PlethoraModules.INTROSPECTION_S,
		doc = "-- Get this player's progress in Astral Sorcery"
	)
	public static Map<String, ?> getAstralProgress(@Nonnull IContext<IModuleContainer> context, @FromSubtarget EntityIdentifier.Player playerId) throws LuaException {
		EntityPlayerMP player = playerId.getPlayer();

		Map<String, Object> out = new HashMap<>();

		PlayerProgress progress = ResearchManager.getProgress(player);

		//Refers to the constellations that you have seen on a paper
		out.put("seenConstellations", getConstellationMeta(context, progress.getSeenConstellations()));

		//Refers to the constellations that you have discovered via telescope, after seeing them on a paper
		out.put("knownConstellations", getConstellationMeta(context, progress.getKnownConstellations()));

		out.put("availablePerkPoints", progress.getAvailablePerkPoints(player));

		IConstellation attuned = progress.getAttunedConstellation();
		if (attuned != null) {
			out.put("attunedConstellation", context.makePartialChild(attuned).getMeta());
		}
		out.put("progressTier", progress.getTierReached().toString()); //REFINE Do we want the name, the ordinal, or a LuaList with both?

		// ... shouldn't the `progressId` field be the same as the ordinal? ... whatever.
		//noinspection SimplifyOptionalCallChains It may be simpler, but it (to me) hurts readability...
		String researchTier = progress.getResearchProgression().stream()
			.max(Comparator.comparingInt(ResearchProgression::getProgressId))
			.map(Enum::toString).orElse(null);
		if (researchTier != null) {
			out.put("researchTier", researchTier);
		}

		//REFINE Someone else can expose the Perks if they want; cost/benefit says "no" at this time

		return out;
	}

	@Nonnull
	private static Map<Integer, TypedMeta<IConstellation, ?>> getConstellationMeta(IContext<?> context, Collection<String> translationKeys) {
		LuaList<TypedMeta<IConstellation, ?>> out = new LuaList<>(translationKeys.size());
		for (String translationKey : translationKeys) {
			IConstellation constellation = ConstellationRegistry.getConstellationByName(translationKey);
			if (constellation != null) out.add(context.makePartialChild(constellation).getMeta());
		}
		return out.asMap();
	}
}
