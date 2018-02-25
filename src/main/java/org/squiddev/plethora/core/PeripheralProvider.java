package org.squiddev.plethora.core;

import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import dan200.computercraft.shared.peripheral.common.IPeripheralTile;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;
import org.squiddev.plethora.api.Constants;
import org.squiddev.plethora.api.WorldLocation;
import org.squiddev.plethora.api.method.CostHelpers;
import org.squiddev.plethora.api.method.IMethod;
import org.squiddev.plethora.api.reference.BlockReference;
import org.squiddev.plethora.core.executor.DefaultExecutor;
import org.squiddev.plethora.utils.DebugLogger;
import org.squiddev.plethora.utils.Helpers;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Wraps tile entities as a peripherals.
 * - Tries to find capability first
 * - If this fails then it attempts to find methods from it
 */
public class PeripheralProvider implements IPeripheralProvider {
	@Override
	public IPeripheral getPeripheral(@Nonnull World world, @Nonnull BlockPos blockPos, @Nonnull EnumFacing enumFacing) {
		TileEntity te = world.getTileEntity(blockPos);
		if (te != null) {
			try {
				// Check for capability first
				IPeripheral capability = te.getCapability(Constants.PERIPHERAL_CAPABILITY, enumFacing);
				if (capability != null) return capability;

				// Simple blacklisting
				if (te instanceof IPeripheralTile) return null;

				Class<?> klass = te.getClass();
				if (isBlacklisted(klass)) return null;

				MethodRegistry registry = MethodRegistry.instance;

				BlockReference reference = new BlockReference(new WorldLocation(world, blockPos), world.getBlockState(blockPos), te);
				ContextFactory<BlockReference> factory = ContextFactory.of(reference)
					.withCostHandler(CostHelpers.getCostHandler(te, enumFacing));

				Pair<List<IMethod<?>>, List<UnbakedContext<?>>> paired = registry.getMethodsPaired(factory.getBaked());
				if (paired.getLeft().size() > 0) {
					return new MethodWrapperPeripheral(Helpers.tryGetName(te).replace('.', '_'), te, paired, DefaultExecutor.INSTANCE);
				}
			} catch (RuntimeException e) {
				DebugLogger.error("Error getting peripheral", e);
			}
		}

		return null;
	}

	private static final Set<String> blacklist = new HashSet<String>();

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
