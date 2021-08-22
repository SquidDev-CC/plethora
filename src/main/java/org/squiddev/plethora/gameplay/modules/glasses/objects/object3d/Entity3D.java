package org.squiddev.plethora.gameplay.modules.glasses.objects.object3d;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.squiddev.plethora.gameplay.modules.glasses.BaseObject;
import org.squiddev.plethora.gameplay.modules.glasses.CanvasClient;
import org.squiddev.plethora.gameplay.modules.glasses.objects.ObjectRegistry;
import org.squiddev.plethora.gameplay.modules.glasses.objects.Scalable;
import org.squiddev.plethora.utils.ByteBufUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class Entity3D extends BaseObject implements EntityObject, Scalable, Positionable3D, DepthTestable, Rotatable3D {
	private float scale;
	private Vec3d position = Vec3d.ZERO;
	private Vec3d rotation = Vec3d.ZERO;
	private boolean depthTest = true;


	private EntityEntry entityEntry;
	private Entity entity;


	public Entity3D(int id, int parent) {super(id, parent, ObjectRegistry.ENTITY ); }

	@Override
	public float getScale() {
		return scale;
	}

	@Override
	public void setScale(float scale) {
		if (this.scale != scale) {
			this.scale = scale;
			setDirty();
		}
	}

	@Nonnull
	@Override
	public Vec3d getPosition() {
		return position;
	}

	@Override
	public void setPosition(@Nonnull Vec3d position) {
		if (!this.position.equals(position)) {
			this.position = position;
			setDirty();
		}
	}

	@Override
	public boolean hasDepthTest() {
		return depthTest;
	}

	@Override
	public void setDepthTest(boolean depthTest) {
		if (this.depthTest != depthTest) {
			this.depthTest = depthTest;
			setDirty();
		}
	}

	@Nonnull
	public EntityEntry getEntityEntry() { return entityEntry; }

	public void setEntityEntry(@Nonnull EntityEntry entityEntry) {
		if (this.entityEntry == null || this.entityEntry != entityEntry) {
			this.entityEntry = entityEntry;
			entity = null;
			setDirty();
		}
	}

	@Nullable
	@Override
	public Vec3d getRotation() {
		return rotation;
	}

	@Override
	public void setRotation(@Nullable Vec3d rotation) {
		if (!Objects.equals(this.rotation, rotation)) {
			this.rotation = rotation;
			setDirty();
		}
	}

	@Override
	public void writeInitial(ByteBuf buf) {
		ByteBufUtils.writeVec3d(buf, position);
		ByteBufUtils.writeOptVec3d(buf, rotation);
		buf.writeFloat(scale);
		buf.writeBoolean(depthTest);

		ByteBufUtils.writeUTF8String(buf, entityEntry.getRegistryName().toString());
	}

	@Override
	public void readInitial(ByteBuf buf) {
		position = ByteBufUtils.readVec3d(buf);
		rotation = ByteBufUtils.readOptVec3d(buf);
		scale = buf.readFloat();
		depthTest = buf.readBoolean();

		String resourceName = ByteBufUtils.readUTF8String(buf);
		ResourceLocation name = new ResourceLocation(resourceName);
		entityEntry = ForgeRegistries.ENTITIES.getValue(name);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void draw(CanvasClient canvas) {

		Minecraft mc = Minecraft.getMinecraft();

		if (entity == null) {
			entity = entityEntry.newInstance(mc.world);
		}

		GlStateManager.pushMatrix();

		GlStateManager.translate(position.x, position.y, position.z);
		GlStateManager.scale(scale, scale, scale);
		if (rotation == null) {
			RenderManager renderManager = mc.getRenderManager();

			GlStateManager.rotate(180 - renderManager.playerViewY, 0, 1, 0);
			GlStateManager.rotate(-renderManager.playerViewX, 1, 0, 0);
		} else {
			GlStateManager.rotate((float) rotation.x, 1, 0, 0);
			GlStateManager.rotate((float) rotation.y, 0, 1, 0);
			GlStateManager.rotate((float) rotation.z, 0, 0, 1);
		}

		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		GlStateManager.enableTexture2D();
		GlStateManager.enableRescaleNormal();

		if (depthTest) {
			GlStateManager.enableDepth();
		} else {
			GlStateManager.disableDepth();
		}

		mc.getRenderManager().renderEntity(entity, position.x, position.y, position.z, 0, 0, true);

		GlStateManager.popMatrix();
	}
}
