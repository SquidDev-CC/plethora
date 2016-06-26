package org.squiddev.plethora;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

/**
 * Base tile for all TileEntities
 */
public abstract class TileBase extends TileEntity {
	/**
	 * Called to save data for the client
	 *
	 * @param tag The data to send
	 * @return If data needs to be sent
	 */
	protected boolean writeDescription(NBTTagCompound tag) {
		return false;
	}

	/**
	 * Read data from the server
	 *
	 * @param tag The data to read
	 */
	protected void readDescription(NBTTagCompound tag) {
	}

	@Override
	public final Packet<?> getDescriptionPacket() {
		NBTTagCompound tag = new NBTTagCompound();
		return writeDescription(tag) ? new S35PacketUpdateTileEntity(pos, 0, tag) : null;
	}

	@Override
	public final void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet) {
		readDescription(packet.getNbtCompound());
	}

	/**
	 * Improvement over {@link #markDirty()}
	 */
	public void markForUpdate() {
		markDirty();
		worldObj.markBlockForUpdate(pos);
	}

	/**
	 * Called when the block is activated
	 *
	 * @param player The player who triggered this
	 * @param side   The side the block is activated on
	 * @return If the event succeeded
	 */
	public boolean onActivated(EntityPlayer player, EnumFacing side) {
		return false;
	}

	/**
	 * Called when a neighbor tile/block changed
	 */
	public void onNeighborChanged() {
	}

	/**
	 * Called when this tile is broken
	 */
	public void onBroken() {
	}
}
