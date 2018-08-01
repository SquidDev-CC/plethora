package org.squiddev.plethora.gameplay.modules.glasses.objects.object2d;

import com.google.common.base.Objects;
import dan200.computercraft.api.lua.LuaException;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.squiddev.plethora.api.method.BasicMethod;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.method.MethodResult;
import org.squiddev.plethora.gameplay.modules.glasses.CanvasClient;
import org.squiddev.plethora.gameplay.modules.glasses.objects.ColourableObject;
import org.squiddev.plethora.gameplay.modules.glasses.objects.ObjectRegistry;
import org.squiddev.plethora.utils.ByteBufUtils;
import org.squiddev.plethora.utils.Vec2d;

import javax.annotation.Nonnull;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.squiddev.plethora.api.method.ArgumentHelper.getFloat;

public class Rectangle extends ColourableObject implements Positionable2D {
	private Vec2d position = Vec2d.ZERO;
	private float width;
	private float height;

	public Rectangle(int id, int parent) {
		super(id, parent, ObjectRegistry.RECTANGLE_2D);
	}

	@Nonnull
	@Override
	public Vec2d getPosition() {
		return position;
	}

	@Override
	public void setPosition(@Nonnull Vec2d position) {
		if (!Objects.equal(this.position, position)) {
			this.position = position;
			setDirty();
		}
	}

	public float getWidth() {
		return width;
	}

	public float getHeight() {
		return height;
	}

	public void setSize(float width, float height) {
		if (this.width != width || this.height != height) {
			this.width = width;
			this.height = height;
			setDirty();
		}
	}

	@Override
	public void writeInitial(ByteBuf buf) {
		super.writeInitial(buf);
		ByteBufUtils.writeVec2d(buf, position);
		buf.writeFloat(width);
		buf.writeFloat(height);
	}

	@Override
	public void readInitial(ByteBuf buf) {
		super.readInitial(buf);
		position = ByteBufUtils.readVec2d(buf);
		width = buf.readFloat();
		height = buf.readFloat();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void draw(CanvasClient canvas) {
		setupFlat();
		GlStateManager.color(1, 1, 1);

		float x = (float) position.x, y = (float) position.y;
		int red = getRed(), green = getGreen(), blue = getBlue(), alpha = getAlpha();

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);

		buffer.pos(x, y, 0).color(red, green, blue, alpha).endVertex();
		buffer.pos(x, y + height, 0).color(red, green, blue, alpha).endVertex();
		buffer.pos(x + width, y + height, 0).color(red, green, blue, alpha).endVertex();

		buffer.pos(x, y, 0).color(red, green, blue, alpha).endVertex();
		buffer.pos(x + width, y + height, 0).color(red, green, blue, alpha).endVertex();
		buffer.pos(x + width, y + 0, 0).color(red, green, blue, alpha).endVertex();

		tessellator.draw();
	}

	@BasicMethod.Inject(value = Rectangle.class, doc = "function():number, number -- Get the size of this rectangle.")
	public static MethodResult getSize(IUnbakedContext<Rectangle> context, Object[] arguments) throws LuaException {
		Rectangle rect = context.safeBake().getTarget();
		return MethodResult.result(rect.getWidth(), rect.getHeight());
	}

	@BasicMethod.Inject(value = Rectangle.class, doc = "function(width:number, height:number) -- Set the size of this rectangle.")
	public static MethodResult setSize(IUnbakedContext<Rectangle> context, Object[] arguments) throws LuaException {
		Rectangle rect = context.safeBake().getTarget();
		rect.setSize(getFloat(arguments, 0), getFloat(arguments, 1));
		return MethodResult.empty();
	}
}
