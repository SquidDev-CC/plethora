package org.squiddev.plethora.gameplay.redstone;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.common.TileGeneric;
import dan200.computercraft.shared.util.RedstoneUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.squiddev.plethora.gameplay.Plethora;
import org.squiddev.plethora.gameplay.registry.Registry;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static dan200.computercraft.core.apis.ArgumentHelper.*;
import static org.squiddev.plethora.api.method.ArgumentHelper.assertBetween;

public class TileRedstoneIntegrator extends TileGeneric implements IPeripheral {
	private final byte[] inputs = new byte[6];
	private final byte[] outputs = new byte[6];
	private final int[] bundledInputs = new int[6];
	private final int[] bundledOutputs = new int[6];

	private boolean outputDirty = false;
	private boolean inputDirty = false;

	private final Set<IComputerAccess> computers = Sets.newConcurrentHashSet();

	private void updateInput() {
		World world = getWorld();
		if (world == null || world.isRemote || isInvalid() || !world.isBlockLoaded(pos)) return;

		boolean changed = false;
		for (EnumFacing dir : EnumFacing.VALUES) {
			BlockPos offset = pos.offset(dir);
			EnumFacing offsetSide = dir.getOpposite();
			int dirIdx = dir.ordinal();

			byte newInput = (byte) world.getRedstonePower(offset, offsetSide);
			if (newInput != inputs[dirIdx]) {
				inputs[dirIdx] = newInput;
				changed = true;
			}

			short newBundled = (short) RedstoneUtil.getBundledRedstoneOutput(world, offset, offsetSide);
			if (bundledInputs[dirIdx] != newBundled) {
				bundledInputs[dirIdx] = newBundled;
				changed = true;
			}
		}

		if (changed) enqueueInputTick();
	}

	private void enqueueInputTick() {
		if (!inputDirty) {
			inputDirty = true;
			BlockRedstoneIntegrator.enqueueTick(this);
		}
	}

	private void enqueueOutputTick() {
		if (!outputDirty) {
			outputDirty = true;
			BlockRedstoneIntegrator.enqueueTick(this);
		}
	}

	void updateOnce() {
		World world = getWorld();
		if (world == null || world.isRemote || isInvalid() || !world.isBlockLoaded(pos)) return;

		if (outputDirty) {
			for (EnumFacing dir : EnumFacing.VALUES) {
				RedstoneUtil.propagateRedstoneOutput(world, pos, dir);
			}
			outputDirty = false;
		}

		if (inputDirty) {
			Iterator<IComputerAccess> computers = this.computers.iterator();
			while (computers.hasNext()) {
				IComputerAccess computer = computers.next();
				try {
					computer.queueEvent("redstone", new Object[]{computer.getAttachmentName()});
				} catch (RuntimeException e) {
					Plethora.LOG.error("Could not queue redstone event", e);
					computers.remove();
				}
			}
			inputDirty = false;
		}
	}

	@Override
	public void onLoad() {
		super.onLoad();

		// Update the output to ensure all redstone is turned off.
		enqueueOutputTick();
	}

	@Override
	public void onNeighbourChange() {
		updateInput();
	}

	@Override
	public ItemStack getPickedItem() {
		return new ItemStack(Registry.blockRedstoneIntegrator);
	}

	@Override
	public void getDroppedItems(@Nonnull NonNullList<ItemStack> drops, boolean creative) {
		super.getDroppedItems(drops, creative);
		if (!creative) drops.add(getPickedItem());
	}

	//region Redstone output providers
	@Override
	public boolean getRedstoneConnectivity(EnumFacing side) {
		return true;
	}

	@Override
	public int getRedstoneOutput(EnumFacing side) {
		return outputs[side.ordinal()];
	}

	@Override
	public boolean getBundledRedstoneConnectivity(@Nonnull EnumFacing side) {
		return true;
	}

	@Override
	public int getBundledRedstoneOutput(@Nonnull EnumFacing side) {
		return bundledOutputs[side.ordinal()];
	}
	//endregion

	//region IPeripheral implementation
	@Nonnull
	@Override
	public String getType() {
		return "redstone_integrator";
	}

	@Nonnull
	@Override
	public String[] getMethodNames() {
		return new String[]{
			"getSides",
			"setOutput", "getOutput", "getInput",
			"setBundledOutput", "getBundledOutput", "getBundledInput", "testBundledInput",
			"setAnalogOutput", "setAnalogueOutput", "getAnalogOutput", "getAnalogueOutput", "getAnalogInput", "getAnalogueInput",
		};
	}

	@Override
	public Object[] callMethod(@Nonnull IComputerAccess computer, @Nonnull ILuaContext context, int method, @Nonnull Object[] args) throws LuaException {
		switch (method) {
			case 0: { // getSides
				Map<Integer, String> result = Maps.newHashMap();

				for (int i = 0; i < EnumFacing.VALUES.length; i++) {
					result.put(i + 1, EnumFacing.VALUES[i].getName());
				}

				return new Object[]{result};
			}
			case 1: { // setOutput
				int side = getFacing(args, 0).ordinal();
				byte power = getBoolean(args, 1) ? (byte) 15 : 0;

				outputs[side] = power;

				enqueueOutputTick();
				return null;
			}
			case 2: { // getOutput
				int side = getFacing(args, 0).ordinal();
				return new Object[]{outputs[side] > 0};
			}
			case 3: { // getInput
				int side = getFacing(args, 0).ordinal();
				return new Object[]{inputs[side] > 0};
			}
			case 4: { // setBundledOutput
				int side = getFacing(args, 0).ordinal();
				int power = getInt(args, 1);

				bundledOutputs[side] = power;
				enqueueOutputTick();
				return null;
			}
			case 5: { // getBundledOutput
				int side = getFacing(args, 0).ordinal();
				return new Object[]{bundledOutputs[side]};
			}
			case 6: { // getBundledInput
				int side = getFacing(args, 0).ordinal();
				return new Object[]{bundledInputs[side]};
			}
			case 7: { // testBundledInput
				int side = getFacing(args, 0).ordinal();
				int power = getInt(args, 1);
				return new Object[]{(bundledInputs[side] & power) == power};
			}
			case 8: // setAnalogueOutput
			case 9: {
				int side = getFacing(args, 0).ordinal();
				int power = getInt(args, 1);

				assertBetween(power, 0, 15, "Power out of range (%s)");

				outputs[side] = (byte) power;
				enqueueOutputTick();
				return null;
			}
			case 10: // getAnalogueOutput
			case 11: {
				int side = getFacing(args, 0).ordinal();
				return new Object[]{outputs[side]};
			}
			case 12: // getAnalogueInput
			case 13: {
				int side = getFacing(args, 0).ordinal();
				return new Object[]{inputs[side]};
			}
			default:
				return null;
		}
	}

	@Override
	public void attach(@Nonnull IComputerAccess computer) {
		computers.add(computer);
	}

	@Override
	public void detach(@Nonnull IComputerAccess computer) {
		computers.remove(computer);
	}

	@Override
	public boolean equals(IPeripheral other) {
		return this == other;
	}

	private static EnumFacing getFacing(Object[] args, int index) throws LuaException {
		String value = getString(args, index);
		if (value.equalsIgnoreCase("bottom")) return EnumFacing.DOWN;
		if (value.equalsIgnoreCase("top")) return EnumFacing.UP;

		EnumFacing facing = EnumFacing.byName(value);
		if (facing == null) {
			throw new LuaException("Bad name '" + value.toLowerCase(Locale.ENGLISH) + "' for argument " + (index + 1));
		}

		return facing;
	}
	//endregion
}
