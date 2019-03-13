package org.squiddev.plethora.gameplay.modules.glasses.objects.object3d;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.method.ContextKeys;
import org.squiddev.plethora.api.method.wrapper.FromContext;
import org.squiddev.plethora.api.method.wrapper.FromTarget;
import org.squiddev.plethora.api.method.wrapper.Optional;
import org.squiddev.plethora.api.method.wrapper.PlethoraMethod;
import org.squiddev.plethora.gameplay.modules.glasses.BaseObject;
import org.squiddev.plethora.gameplay.modules.glasses.CanvasClient;
import org.squiddev.plethora.gameplay.modules.glasses.objects.ObjectGroup;
import org.squiddev.plethora.gameplay.modules.glasses.objects.ObjectRegistry;
import org.squiddev.plethora.utils.ByteBufUtils;

public class ObjectRoot3D extends BaseObject implements ObjectGroup.Group3D {
	private Vec3d origin;
	private int dimension;

	public ObjectRoot3D(int id, int parent) {
		super(id, parent, ObjectRegistry.ORIGIN_3D);
	}

	public void recentre(World world, Vec3d origin) {
		int dimension = world.provider.getDimension();

		if (!origin.equals(this.origin) || dimension != this.dimension) {
			this.origin = origin;
			this.dimension = dimension;
			setDirty();
		}
	}

	@Override
	public void readInitial(ByteBuf buf) {
		origin = ByteBufUtils.readVec3d(buf);
		dimension = buf.readInt();
	}

	@Override
	public void writeInitial(ByteBuf buf) {
		ByteBufUtils.writeVec3d(buf, origin);
		buf.writeInt(dimension);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void draw(CanvasClient canvas) {
		IntSet children = canvas.getChildren(id());
		if (children == null) return;

		Minecraft minecraft = Minecraft.getMinecraft();
		RenderManager renderManager = minecraft.getRenderManager();

		if (renderManager.renderViewEntity == null || renderManager.renderViewEntity.world.provider.getDimension() != dimension) {
			return;
		}

		// TODO: Determine a better way of handling this.
		double distance = renderManager.options.renderDistanceChunks * 16;
		if (origin.squareDistanceTo(renderManager.viewerPosX, renderManager.viewerPosY, renderManager.viewerPosZ) > distance * distance) {
			return;
		}

		GlStateManager.pushMatrix();
		GlStateManager.translate(-renderManager.viewerPosX + origin.x, -renderManager.viewerPosY + origin.y, -renderManager.viewerPosZ + origin.z);

		canvas.drawChildren(children.iterator());

		GlStateManager.popMatrix();
	}

	@PlethoraMethod(doc = "-- Recenter this canvas relative to the current position.", worldThread = false)
	public static void recenter(
		@FromTarget ObjectRoot3D target, @FromContext(ContextKeys.ORIGIN) IWorldLocation location,
		@Optional Vec3d offset
	) {
		if (offset == null) offset = Vec3d.ZERO;
		target.recentre(location.getWorld(), location.getLoc().add(offset));
	}
}
