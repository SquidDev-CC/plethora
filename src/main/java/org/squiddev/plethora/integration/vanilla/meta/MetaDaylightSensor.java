package org.squiddev.plethora.integration.vanilla.meta;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.BaseMetaProvider;
import org.squiddev.plethora.api.method.IPartialContext;
import org.squiddev.plethora.api.reference.BlockReference;
import org.squiddev.plethora.integration.vanilla.IntegrationVanilla;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Meta provider which adds block light for blocks when the daylight sensor is installed.
 */
@Injects
public class MetaDaylightSensor extends BaseMetaProvider<BlockReference> {
	public MetaDaylightSensor() {
		super("Provides metadata about block light levels, when the daylight sensor module is attached.");
	}

	@Nonnull
	@Override
	public Map<String, ?> getMeta(@Nonnull IPartialContext<BlockReference> context) {
		if (!context.getModules().hasModule(IntegrationVanilla.daylightSensorMod)) return Collections.emptyMap();

		IWorldLocation location = context.getTarget().getLocation();
		World world = location.getWorld();
		BlockPos pos = location.getPos();

		Map<String, Object> out = new HashMap<>(2);
		if (!world.provider.hasSkyLight()) {
			out.put("sky", world.getLightFor(EnumSkyBlock.SKY, pos) - world.getSkylightSubtracted());
		}

		out.put("block", world.getLightFor(EnumSkyBlock.BLOCK, pos));
		return Collections.singletonMap("light", out);
	}
}
