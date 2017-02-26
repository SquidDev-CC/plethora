package org.squiddev.plethora.gameplay;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

/**
 * Base tile for all TileEntities
 */
public abstract class TileBase extends TileEntity {
	/**
	 * Called to save data for the client
	 *
	 * @param tag The data to send
	 */
	protected void writeDescription(NBTTagCompound tag) {
	}

	/**
	 * Read data from the server
	 *
	 * @param tag The data to read
	 */
	protected void readDescription(NBTTagCompound tag) {
	}

	@Nonnull
	@Override
	public NBTTagCompound getUpdateTag() {
		NBTTagCompound tag = super.getUpdateTag();
		writeDescription(tag);
		return tag;
	}

	@Override
	public void handleUpdateTag(@Nonnull NBTTagCompound tag) {
		super.handleUpdateTag(tag);
		readDescription(tag);
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		NBTTagCompound tag = new NBTTagCompound();
		writeDescription(tag);
		return new SPacketUpdateTileEntity(getPos(), 0, tag);
	}

	@Override
	public final void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
		readDescription(packet.getNbtCompound());
	}

	/**
	 * Improvement over {@link #markDirty()}
	 */
	public void markForUpdate() {
		markDirty();
		World worldObj = getWorld();
		BlockPos pos = getPos();
		IBlockState state = worldObj.getBlockState(pos);
		worldObj.notifyBlockUpdate(getPos(), state, state, 3);
		worldObj.notifyNeighborsOfStateChange(pos, blockType, false);
	}

	/**
	 * Called when the block is activated
	 *
	 * @param player The player who triggered this
	 * @param hand   The hand it was clicked with
	 * @param side   The side the block is activated on
	 * @param hit    The position the hit occurred
	 * @return If the event succeeded
	 */
	public boolean onActivated(EntityPlayer player, EnumHand hand, EnumFacing side, Vec3d hit) {
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
	public void broken() {
	}

	/**
	 * Called when the TileEntity is unloaded or invalidated
	 */
	public void removed() {
	}

	/**
	 * Called when the TileEntity is validated
	 */
	public void created() {
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		World worldObj = getWorld();
		if (worldObj == null || !worldObj.isRemote) {
			removed();
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();
		World worldObj = getWorld();
		if (worldObj == null || !worldObj.isRemote) {
			removed();
		}
	}

	@Override
	public void validate() {
		super.validate();
		World worldObj = getWorld();
		if (worldObj == null || !worldObj.isRemote) {
			created();
		}
	}
}
