package org.squiddev.plethora.gameplay.modules.glasses.objects.object2d;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.squiddev.plethora.gameplay.modules.glasses.BaseObject;
import org.squiddev.plethora.gameplay.modules.glasses.objects.ObjectRegistry;
import org.squiddev.plethora.gameplay.modules.glasses.objects.Scalable;

import javax.annotation.Nonnull;

public class Item2D extends BaseObject implements Scalable, Positionable2D {
	private float scale;
	private Point2D position = new Point2D();

	private int damage;
	private Item item;

	public Item2D(int id) {
		super(id, ObjectRegistry.ITEM_2D);
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

	@Override
	public Point2D getPosition() {
		return position;
	}

	@Override
	public void setPosition(Point2D position) {
		if (!this.position.equals(position)) {
			this.position = position;
			setDirty();
		}
	}

	public int getDamage() {
		return damage;
	}

	public void setDamage(int damage) {
		if (this.damage != damage) {
			this.damage = damage;
			setDirty();
		}
	}

	public Item getItem() {
		return item;
	}

	public void setItem(@Nonnull Item item) {
		if (this.item != item) {
			this.item = item;
			setDirty();
		}
	}

	@Override
	public void writeInital(ByteBuf buf) {
		position.write(buf);
		buf.writeFloat(scale);
		ByteBufUtils.writeUTF8String(buf, item.getRegistryName().toString());
		buf.writeInt(damage);
	}

	@Override
	public void readInitial(ByteBuf buf) {
		position.read(buf);
		scale = buf.readFloat();

		ResourceLocation name = new ResourceLocation(ByteBufUtils.readUTF8String(buf));
		item = Item.REGISTRY.getObject(name);

		damage = buf.readInt();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void draw2D() {
		GlStateManager.pushMatrix();

		GlStateManager.translate(position.x, position.y, 0);
		GlStateManager.scale(scale, scale, 1);

		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		GlStateManager.enableTexture2D();
		GlStateManager.enableRescaleNormal();
		GlStateManager.enableBlend();
		GlStateManager.enableAlpha();
		GlStateManager.enableCull();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		RenderHelper.enableGUIStandardItemLighting();

		ItemStack stack = new ItemStack(item, 1, damage);
		Minecraft.getMinecraft().getRenderItem()
			.renderItemAndEffectIntoGUI(Minecraft.getMinecraft().player, stack, 0, 0);

		GlStateManager.popMatrix();
	}
}
