package org.squiddev.plethora.gameplay.modules;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.squiddev.plethora.api.Constants;
import org.squiddev.plethora.api.IPlayerOwnable;
import org.squiddev.plethora.core.executor.TaskRunner;
import org.squiddev.plethora.gameplay.TileBase;
import org.squiddev.plethora.gameplay.registry.Registration;
import org.squiddev.plethora.utils.Helpers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

import static org.squiddev.plethora.gameplay.modules.BlockManipulator.BOX_EXPAND;
import static org.squiddev.plethora.gameplay.modules.BlockManipulator.OFFSET;
import static org.squiddev.plethora.gameplay.modules.ManipulatorType.VALUES;

public final class TileManipulator extends TileBase implements ITickable, IPlayerOwnable {
	private ManipulatorType type;
	private NonNullList<ItemStack> stacks;
	private GameProfile profile;
	private int stackHash;

	private final Map<ResourceLocation, NBTTagCompound> moduleData = new HashMap<>();

	private final TaskRunner runner = new TaskRunner();

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
		stacks = NonNullList.withSize(type.size(), ItemStack.EMPTY);
	}

	public ManipulatorType getType() {
		return type;
	}

	public EnumFacing getFacing() {
		IBlockState state = getWorld().getBlockState(getPos());
		return state.getBlock() == Registration.blockManipulator
			? state.getValue(BlockManipulator.FACING)
			: EnumFacing.DOWN;
	}

	@Nonnull
	public ItemStack getStack(int slot) {
		return stacks.get(slot);
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
		World world = getWorld();
		IBlockState state = world.getBlockState(pos);
		world.notifyBlockUpdate(getPos(), state, state, 3);
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
		for (int i = 0; i < stacks.size(); i++) {
			ItemStack stack = stacks.get(i);
			if (!stack.isEmpty()) {
				tag.setTag("stack" + i, stack.serializeNBT());
			} else {
				tag.removeTag("stack" + i);
			}
		}

		if (moduleData.isEmpty()) {
			tag.removeTag("data");
		} else {
			NBTTagCompound data = tag.getCompoundTag("data");
			for (Map.Entry<ResourceLocation, NBTTagCompound> entry : moduleData.entrySet()) {
				data.setTag(entry.getKey().toString(), entry.getValue());
			}
		}
	}

	@Override
	protected void readDescription(NBTTagCompound tag) {
		if (tag.hasKey("type") && type == null) {
			int meta = tag.getInteger("type");
			setType(VALUES[meta & 1]);
		}

		if (type == null) return;

		for (int i = 0; i < stacks.size(); i++) {
			stacks.set(i, tag.hasKey("stack" + i) ? new ItemStack(tag.getCompoundTag("stack" + i)) : ItemStack.EMPTY);
		}

		stackHash = Helpers.hashStacks(stacks);

		NBTTagCompound data = tag.getCompoundTag("data");
		for (String key : data.getKeySet()) {
			moduleData.put(new ResourceLocation(key), data.getCompoundTag(key));
		}
	}

	@Override
	public boolean onActivated(EntityPlayer player, EnumHand hand, EnumFacing side, Vec3d hit) {
		if (player.getEntityWorld().isRemote) return true;

		if (type == null) {
			int meta = getBlockMetadata();
			setType(VALUES[meta & 1]);
		}

		ItemStack heldStack = player.getHeldItem(hand);
		AxisAlignedBB[] boxes = type.boxesFor(getFacing());
		for (int i = 0; i < type.size(); i++) {
			AxisAlignedBB box = boxes[i];
			if (box.grow(BOX_EXPAND, BOX_EXPAND, BOX_EXPAND).contains(hit)) {
				final ItemStack stack = stacks.get(i);
				if (heldStack.isEmpty() && !stack.isEmpty()) {
					if (!player.capabilities.isCreativeMode) {
						Vec3d offsetPos = new Vec3d(pos).add(new Vec3d(getFacing().getOpposite().getDirectionVec()).scale(OFFSET));
						Helpers.spawnItemStack(getWorld(), offsetPos.x, offsetPos.y, offsetPos.z, stack);
					}

					stacks.set(i, ItemStack.EMPTY);
					stackHash = Helpers.hashStacks(stacks);
					markForUpdate();

					break;
				} else if (stack.isEmpty() && !heldStack.isEmpty() && heldStack.hasCapability(Constants.MODULE_HANDLER_CAPABILITY, null)) {
					stacks.set(i, heldStack.copy());
					stacks.get(i).setCount(1);
					stackHash = Helpers.hashStacks(stacks);

					if (!player.capabilities.isCreativeMode) {
						heldStack.grow(-1);
						if (heldStack.getCount() <= 0) {
							player.inventory.setInventorySlotContents(player.inventory.currentItem, ItemStack.EMPTY);
						}
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

		Vec3d offsetPos = new Vec3d(pos).add(new Vec3d(getFacing().getOpposite().getDirectionVec()).scale(OFFSET));
		for (ItemStack stack : stacks) {
			if (stack != null) {
				Helpers.spawnItemStack(getWorld(), offsetPos.x, offsetPos.y, offsetPos.z, stack);
			}
		}

		stacks.clear();
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
		runner.update();
	}

	@Nonnull
	public TaskRunner getRunner() {
		return runner;
	}

	@Override
	public void removed() {
		runner.reset();
	}

	public void setOwningProfile(GameProfile profile) {
		this.profile = profile;
	}

	@Nullable
	@Override
	public GameProfile getOwningProfile() {
		return profile;
	}
}
