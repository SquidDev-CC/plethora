package org.squiddev.plethora.integration.vanilla.meta;

import com.google.common.collect.Maps;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.meta.BaseMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.api.method.IPartialContext;
import org.squiddev.plethora.api.reference.BlockReference;
import org.squiddev.plethora.integration.vanilla.IntegrationVanilla;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

/**
 * Meta provider which adds block light for blocks when the daylight sensor is installed.
 */
@IMetaProvider.Inject(value = BlockReference.class, namespace = "light")
public class MetaDaylightSensor extends BaseMetaProvider<BlockReference> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull IPartialContext<BlockReference> context) {
		if (!context.getModules().hasModule(IntegrationVanilla.daylightSensorMod)) return Collections.emptyMap();

		IWorldLocation location = context.getTarget().getLocation();
		World world = location.getWorld();
		BlockPos pos = location.getPos();

		Map<Object, Object> out = Maps.newHashMap();
		if (!world.provider.hasNoSky()) {
			out.put("sky", world.getLightFor(EnumSkyBlock.SKY, pos) - world.getSkylightSubtracted());
		}

		out.put("block", world.getLightFor(EnumSkyBlock.BLOCK, pos));
		return out;
	}
}
