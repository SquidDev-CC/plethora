package org.squiddev.plethora.integration.vanilla;

import net.minecraft.util.math.Vec3d;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.gameplay.PlethoraFakePlayer;

public final class FakePlayerProviderLocation {
	private FakePlayerProviderLocation() {
	}

	public static void load(PlethoraFakePlayer player, IWorldLocation location) {
		Vec3d vec = location.getLoc();
		player.setWorld(location.getWorld());
		player.setPositionAndRotation(vec.x, vec.y, vec.z, 0, 0);
	}
}
