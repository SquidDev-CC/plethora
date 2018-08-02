package org.squiddev.plethora.gameplay.modules.glasses.objects.object2d;

import dan200.computercraft.api.lua.LuaException;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.squiddev.plethora.api.method.BasicMethod;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.method.MethodResult;
import org.squiddev.plethora.gameplay.modules.glasses.BaseObject;
import org.squiddev.plethora.gameplay.modules.glasses.CanvasClient;
import org.squiddev.plethora.gameplay.modules.glasses.objects.ObjectRegistry;
import org.squiddev.plethora.gameplay.modules.glasses.objects.Scalable;
import org.squiddev.plethora.utils.ByteBufUtils;
import org.squiddev.plethora.utils.Vec2d;

import javax.annotation.Nonnull;

import static dan200.computercraft.core.apis.ArgumentHelper.getString;
import static dan200.computercraft.core.apis.ArgumentHelper.optInt;

public class Item2D extends BaseObject implements Scalable, Positionable2D {
	private float scale;
	private Vec2d position = Vec2d.ZERO;

	private int damage;
	private Item item;

	public Item2D(int id, int parent) {
		super(id, parent, ObjectRegistry.ITEM_2D);
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
	public Vec2d getPosition() {
		return position;
	}

	@Override
	public void setPosition(@Nonnull Vec2d position) {
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
	public void writeInitial(ByteBuf buf) {
		ByteBufUtils.writeVec2d(buf, position);
		buf.writeFloat(scale);
		ByteBufUtils.writeUTF8String(buf, item.getRegistryName().toString());
		buf.writeInt(damage);
	}

	@Override
	public void readInitial(ByteBuf buf) {
		position = ByteBufUtils.readVec2d(buf);
		scale = buf.readFloat();

		ResourceLocation name = new ResourceLocation(ByteBufUtils.readUTF8String(buf));
		item = Item.REGISTRY.getObject(name);

		damage = buf.readInt();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void draw(CanvasClient canvas) {
		GlStateManager.pushMatrix();

		GlStateManager.translate(position.x, position.y, 0);
		GlStateManager.scale(scale, scale, 1);

		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		GlStateManager.enableTexture2D();
		GlStateManager.enableRescaleNormal();
		GlStateManager.enableBlend();
		GlStateManager.enableAlpha();

		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		RenderHelper.enableGUIStandardItemLighting();

		ItemStack stack = new ItemStack(item, 1, damage);
		Minecraft.getMinecraft().getRenderItem()
			.renderItemAndEffectIntoGUI(Minecraft.getMinecraft().player, stack, 0, 0);

		GlStateManager.popMatrix();
	}

	@BasicMethod.Inject(value = Item2D.class, doc = "function(): string, number -- Get the item and damage value for this object.")
	public static MethodResult getItem(IUnbakedContext<Item2D> context, Object[] args) throws LuaException {
		Item2D object = context.safeBake().getTarget();

		return MethodResult.result(object.getItem().getRegistryName().toString(), object.getDamage());
	}

	@BasicMethod.Inject(value = Item2D.class, doc = "function(item:string[, damage:number]) -- Set the item and damage value for this object.")
	public static MethodResult setItem(IUnbakedContext<Item2D> context, Object[] args) throws LuaException {
		Item2D object = context.safeBake().getTarget();

		ResourceLocation name = new ResourceLocation(getString(args, 0));
		int damage = optInt(args, 1, 0);

		Item item = Item.REGISTRY.getObject(name);
		if (item == null) throw new LuaException("Unknown item '" + name + "'");

		object.setItem(item);
		object.setDamage(damage);

		return MethodResult.empty();
	}
}
