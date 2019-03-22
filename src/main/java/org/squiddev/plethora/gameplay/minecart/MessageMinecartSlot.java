package org.squiddev.plethora.gameplay.minecart;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.squiddev.plethora.gameplay.registry.BasicMessage;

public class MessageMinecartSlot implements BasicMessage {
	private static final byte FLAG_TAG = 1;
	private static final byte FLAG_STACK = 2;

	private int entityId;
	public int slot;
	private int flags;
	public ItemStack stack;
	public NBTTagCompound tag;

	public MessageMinecartSlot() {
	}

	public MessageMinecartSlot(EntityMinecartComputer minecart, int slot) {
		entityId = minecart.getEntityId();
		this.slot = slot;
	}

	public void setStack(ItemStack stack) {
		this.stack = stack;
		flags |= FLAG_STACK;
	}

	public void setTag(NBTTagCompound tag) {
		this.tag = tag;
		flags |= FLAG_TAG;
	}

	public boolean hasTag() {
		return (flags & FLAG_TAG) != 0;
	}

	public boolean hasStack() {
		return (flags & FLAG_STACK) != 0;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		entityId = buf.readInt();
		slot = buf.readByte();
		flags = buf.readByte();

		if ((flags & FLAG_TAG) != 0) tag = ByteBufUtils.readTag(buf);
		if ((flags & FLAG_STACK) != 0) stack = ByteBufUtils.readItemStack(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(entityId);
		buf.writeByte(slot);
		buf.writeByte(flags);

		if ((flags & FLAG_TAG) != 0) ByteBufUtils.writeTag(buf, tag);
		if ((flags & FLAG_STACK) != 0) ByteBufUtils.writeItemStack(buf, stack);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onMessage(final MessageContext ctx) {
		// We schedule this to run on the main thread so the entity is actually loaded by this point.
		// After all, this is what the S0EPacketSpawnObject packet does.
		Minecraft mc = Minecraft.getMinecraft();
		if (!mc.isCallingFromMinecraftThread()) {
			mc.addScheduledTask(() -> this.onMessage(ctx));
			return;
		}

		World world = Minecraft.getMinecraft().world;
		if (world == null) return;

		Entity entity = world.getEntityByID(entityId);
		if (entity instanceof EntityMinecartComputer) {
			EntityMinecartComputer computer = ((EntityMinecartComputer) entity);

			if (hasStack()) computer.itemHandler.setStackInSlot(slot, stack);
			if (hasTag()) computer.accesses[slot].compound = tag;
		}
	}
}
