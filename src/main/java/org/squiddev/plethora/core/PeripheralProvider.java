package org.squiddev.plethora.core;

import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import dan200.computercraft.shared.peripheral.common.IPeripheralTile;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.World;
import org.squiddev.plethora.api.Constants;
import org.squiddev.plethora.api.WorldLocation;
import org.squiddev.plethora.api.method.ICostHandler;
import org.squiddev.plethora.api.method.IMethod;
import org.squiddev.plethora.api.method.IPartialContext;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.reference.Reference;
import org.squiddev.plethora.core.executor.DefaultExecutor;
import org.squiddev.plethora.utils.DebugLogger;
import org.squiddev.plethora.utils.Helpers;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.squiddev.plethora.api.reference.Reference.tile;

/**
 * Wraps tile entities as a peripherals.
 * - Tries to find capability first
 * - If this fails then it attempts to find methods from it
 */
public class PeripheralProvider implements IPeripheralProvider {
	@Override
	public IPeripheral getPeripheral(World world, BlockPos blockPos, EnumFacing enumFacing) {
		TileEntity te = world.getTileEntity(blockPos);
		if (te != null) {
			// Check for capability first
			IPeripheral capability = te.getCapability(Constants.PERIPHERAL_CAPABILITY, enumFacing);
			if (capability != null) return capability;

			// Simple blacklisting
			if (te instanceof IPeripheralTile) return null;

			Class<?> klass = te.getClass();
			if (isBlacklisted(klass)) return null;

			MethodRegistry registry = MethodRegistry.instance;

			ICostHandler handler = registry.getCostHandler(te);
			IUnbakedContext<TileEntity> context = registry.makeContext(tile(te), handler, Reference.id(Collections.<ResourceLocation>emptySet()), new WorldLocation(world, blockPos));
			IPartialContext<TileEntity> baked = new PartialContext<TileEntity>(te, handler, new Object[]{new WorldLocation(world, blockPos)}, Collections.<ResourceLocation>emptySet());

			Tuple<List<IMethod<?>>, List<IUnbakedContext<?>>> paired = registry.getMethodsPaired(context, baked);
			if (paired.getFirst().size() > 0) {
				return new MethodWrapperPeripheral(te, paired.getFirst(), paired.getSecond(), DefaultExecutor.INSTANCE);
			}
		}

		return null;
	}

	private static Set<String> blacklist = new HashSet<String>();

	public static void addToBlacklist(String klass) {
		blacklist.add(klass);
	}

	public static boolean isBlacklisted(Class<?> klass) {
		String name = klass.getName();

		if (blacklist.contains(name)) return true;
		if (Helpers.classBlacklisted(ConfigCore.Blacklist.blacklistTileEntities, name)) {
			blacklist.add(name);
			return true;
		}

		try {
			klass.getField("PLETHORA_IGNORE");
			blacklist.add(name);
			return true;
		} catch (NoSuchFieldException ignored) {
		} catch (Throwable t) {
			DebugLogger.warn("Cannot get ignored field from " + name, t);
		}

		return false;
	}
}
