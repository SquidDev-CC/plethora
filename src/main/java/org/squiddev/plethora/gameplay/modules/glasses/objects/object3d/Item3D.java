package org.squiddev.plethora.gameplay.modules.glasses.objects.object3d;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.squiddev.plethora.gameplay.modules.glasses.BaseObject;
import org.squiddev.plethora.gameplay.modules.glasses.CanvasClient;
import org.squiddev.plethora.gameplay.modules.glasses.objects.ItemObject;
import org.squiddev.plethora.gameplay.modules.glasses.objects.ObjectRegistry;
import org.squiddev.plethora.gameplay.modules.glasses.objects.Scalable;
import org.squiddev.plethora.utils.ByteBufUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class Item3D extends BaseObject implements Scalable, Positionable3D, DepthTestable, ItemObject, Rotatable3D {
	private float scale;
	private Vec3d position = Vec3d.ZERO;
	private Vec3d rotation = Vec3d.ZERO;
	private boolean depthTest = true;

	private int damage;
	private Item item;
	private ItemStack stack;

	public Item3D(int id, int parent) {
		super(id, parent, ObjectRegistry.ITEM_3D);
	}

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

	@Override
	public int getDamage() {
		return damage;
	}

	@Override
	public void setDamage(int damage) {
		if (this.damage != damage) {
			this.damage = damage;
			stack = null;
			setDirty();
		}
	}

	@Override
	@Nonnull
	public Item getItem() {
		return item;
	}

	@Override
	public void setItem(@Nonnull Item item) {
		if (this.item != item) {
			this.item = item;
			stack = null;
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

		ByteBufUtils.writeUTF8String(buf, item.getRegistryName().toString());
		buf.writeInt(damage);
	}

	@Override
	public void readInitial(ByteBuf buf) {
		position = ByteBufUtils.readVec3d(buf);
		rotation = ByteBufUtils.readOptVec3d(buf);
		scale = buf.readFloat();
		depthTest = buf.readBoolean();

		ResourceLocation name = new ResourceLocation(ByteBufUtils.readUTF8String(buf));
		item = Item.REGISTRY.getObject(name);

		damage = buf.readInt();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void draw(CanvasClient canvas) {
		Minecraft mc = Minecraft.getMinecraft();

		GlStateManager.pushMatrix();

		GlStateManager.translate(position.x, position.y, position.z);
		GlStateManager.scale(scale, scale, scale);
		if (rotation == null) {
			RenderManager renderManager = mc.getRenderManager();
			GlStateManager.rotate(180 - renderManager.playerViewY, 0, 1, 0);
			GlStateManager.rotate(renderManager.playerViewX, 1, 0, 0);
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

		if (stack == null) stack = new ItemStack(item, 1, damage);
		mc.getRenderItem().renderItem(stack, mc.player, ItemCameraTransforms.TransformType.NONE, false);

		GlStateManager.popMatrix();
	}
}
