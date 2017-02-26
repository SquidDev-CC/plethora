package org.squiddev.plethora.gameplay.modules;

import com.google.common.collect.Maps;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.squiddev.plethora.api.Constants;
import org.squiddev.plethora.core.executor.ContextDelayedExecutor;
import org.squiddev.plethora.core.executor.IExecutorFactory;
import org.squiddev.plethora.gameplay.TileBase;
import org.squiddev.plethora.utils.Helpers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Map;

import static org.squiddev.plethora.gameplay.modules.BlockManipulator.OFFSET;
import static org.squiddev.plethora.gameplay.modules.BlockManipulator.PIX;
import static org.squiddev.plethora.gameplay.modules.ManipulatorType.VALUES;

public final class TileManipulator extends TileBase implements ITickable {
	private ManipulatorType type;
	private ItemStack[] stacks;
	private int stackHash;

	private Map<ResourceLocation, NBTTagCompound> moduleData = Maps.newHashMap();

	private final ContextDelayedExecutor executor = new ContextDelayedExecutor();

	// Lazily loaded render options
	private double offset = -1;
	private long tick;

	public TileManipulator() {
	}

	public TileManipulator(ManipulatorType type) {
		setType(type);
	}

	private void setType(ManipulatorType type) {
		if (this.type != null) return;

		this.type = type;
		stacks = new ItemStack[type.size()];
	}

	public ManipulatorType getType() {
		return type;
	}

	public ItemStack getStack(int slot) {
		return stacks[slot];
	}

	public int getStackHash() {
		return stackHash;
	}

	public NBTTagCompound getModuleData(ResourceLocation location) {
		NBTTagCompound tag = moduleData.get(location);
		if (tag == null) moduleData.put(location, tag = new NBTTagCompound());
		return tag;
	}

	public void markModuleDataDirty() {
		markDirty();
		BlockPos pos = getPos();
		IBlockState state = worldObj.getBlockState(pos);
		worldObj.notifyBlockUpdate(getPos(), state, state, 3);
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		readDescription(tag);
	}

	@Nonnull
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag) {
		tag = super.writeToNBT(tag);
		writeDescription(tag);
		return tag;
	}

	@Override
	protected void writeDescription(NBTTagCompound tag) {
		tag.setInteger("type", type.ordinal());
		for (int i = 0; i < stacks.length; i++) {
			ItemStack stack = stacks[i];
			if (stack != null) {
				tag.setTag("stack" + i, stack.serializeNBT());
			} else {
				tag.removeTag("stack" + i);
			}
		}

		if (moduleData.isEmpty()) {
			tag.removeTag("data");
		} else {
			NBTTagCompound data = tag.getCompoundTag("data");
			for (Map.Entry<ResourceLocation, NBTTagCompound> entry : this.moduleData.entrySet()) {
				data.setTag(entry.getKey().toString(), entry.getValue());
			}
		}
	}

	@Override
	protected void readDescription(NBTTagCompound tag) {
		if (tag.hasKey("type") && type == null) {
			int meta = tag.getInteger("type");
			setType(VALUES[meta < 0 || meta >= VALUES.length ? 0 : meta]);
		}

		if (type == null) return;

		for (int i = 0; i < stacks.length; i++) {
			if (tag.hasKey("stack" + i)) {
				stacks[i] = ItemStack.loadItemStackFromNBT(tag.getCompoundTag("stack" + i));
			} else {
				stacks[i] = null;
			}
		}

		stackHash = Helpers.hashStacks(stacks);

		NBTTagCompound data = tag.getCompoundTag("data");
		for (String key : data.getKeySet()) {
			moduleData.put(new ResourceLocation(key), data.getCompoundTag(key));
		}
	}

	@Override
	public boolean onActivated(EntityPlayer player, EnumHand hand, @Nullable ItemStack heldStack, EnumFacing side, Vec3d hit) {
		if (side != EnumFacing.UP) return false;
		if (player.worldObj.isRemote) return true;

		if (type == null) {
			int meta = getBlockMetadata();
			setType(VALUES[meta < 0 || meta >= VALUES.length ? 0 : meta]);
		}

		for (int i = 0; i < type.size(); i++) {
			AxisAlignedBB box = type.boxes[i];
			if (hit.yCoord > OFFSET - PIX &&
				hit.xCoord >= box.minX && hit.xCoord <= box.maxX &&
				hit.zCoord >= box.minZ && hit.zCoord <= box.maxZ) {

				final ItemStack stack = stacks[i];
				if (heldStack == null && stack != null) {
					if (!player.capabilities.isCreativeMode) {
						Helpers.spawnItemStack(worldObj, pos.getX(), pos.getY() + OFFSET, pos.getZ(), stack);
					}

					stacks[i] = null;
					stackHash = Helpers.hashStacks(stacks);
					markForUpdate();

					break;
				} else if (stack == null && heldStack != null && heldStack.hasCapability(Constants.MODULE_HANDLER_CAPABILITY, null)) {
					stacks[i] = heldStack.copy();
					stacks[i].stackSize = 1;
					stackHash = Helpers.hashStacks(stacks);

					if (!player.capabilities.isCreativeMode && --heldStack.stackSize <= 0) {
						player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
					}

					markForUpdate();

					break;
				}
			}
		}

		return true;
	}

	@Override
	public void broken() {
		if (stacks == null) return;

		for (ItemStack stack : stacks) {
			if (stack != null) {
				Helpers.spawnItemStack(worldObj, pos.getX(), pos.getY() + OFFSET, pos.getZ(), stack);
			}
		}

		Arrays.fill(stacks, null);
		stackHash = 0;
	}

	public double incrementRotation() {
		long tick = ++this.tick;
		double offset = this.offset;
		if (offset < 0) offset = this.offset = Helpers.RANDOM.nextDouble() * (2 * Math.PI);

		return (tick / 100.0) + offset;
	}

	@Override
	public void update() {
		executor.update();
	}

	public IExecutorFactory getFactory() {
		return executor;
	}

	@Override
	public void removed() {
		executor.reset();
	}
}
