package org.squiddev.plethora.gameplay;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.Vec3;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;

import java.util.UUID;

public class PlethoraFakePlayer extends FakePlayer {
	private static final GameProfile profile = new GameProfile(UUID.fromString("ebcda071-3ef5-4f4a-849d-c078264010c4"), Plethora.ID);

	public PlethoraFakePlayer(WorldServer world) {
		super(world, profile);
	}

	@Override
	public float getEyeHeight() {
		return 0.0F;
	}

	@Override
	public float getDefaultEyeHeight() {
		return 0.0F;
	}

	@Override
	public void mountEntity(Entity entity) {
	}

	@Override
	public void dismountEntity(Entity entity) {
	}

	@Override
	public void openEditSign(TileEntitySign sign) {
	}

	@Override
	public Vec3 getPositionVector() {
		return new Vec3(posX, posY, posZ);
	}
}
