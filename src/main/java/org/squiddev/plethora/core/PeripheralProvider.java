package org.squiddev.plethora.core;

import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import dan200.computercraft.api.peripheral.IPeripheralTile;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;
import org.squiddev.plethora.api.WorldLocation;
import org.squiddev.plethora.api.method.ContextKeys;
import org.squiddev.plethora.api.method.CostHelpers;
import org.squiddev.plethora.api.method.IMethod;
import org.squiddev.plethora.api.reference.BlockReference;
import org.squiddev.plethora.core.executor.TaskRunner;
import org.squiddev.plethora.utils.Helpers;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * Wraps any tile entity as a peripheral.
 */
public class PeripheralProvider implements IPeripheralProvider {
	@Override
	public IPeripheral getPeripheral(@Nonnull World world, @Nonnull BlockPos blockPos, @Nonnull EnumFacing side) {
		TileEntity te = world.getTileEntity(blockPos);
		if (te == null || te.isInvalid()) return null;

		// Skip peripheral tiles which are currently non-functioning.
		if (te instanceof IPeripheralTile && ((IPeripheralTile) te).getPeripheral(side) == null) {
			return null;
		}

		try {
			Class<?> klass = te.getClass();
			if (isBlacklisted(klass)) return null;

			MethodRegistry registry = MethodRegistry.instance;

			WorldLocation location = new WorldLocation(world, blockPos);
			BlockReference reference = new BlockReference(location, world.getBlockState(blockPos), te);
			ContextFactory<BlockReference> factory = ContextFactory.of(reference)
				.withCostHandler(CostHelpers.getCostHandler(te, side))
				.addContext(ContextKeys.ORIGIN, location);

			Pair<List<IMethod<?>>, List<UnbakedContext<?>>> paired = registry.getMethodsPaired(factory.getBaked());
			if (!paired.getLeft().isEmpty()) {
				return new MethodWrapperPeripheral(Helpers.tryGetName(te).replace('.', '_'), te, paired, TaskRunner.SHARED);
			}
		} catch (RuntimeException e) {
			PlethoraCore.LOG.error("Error getting peripheral", e);
		}

		return null;
	}

	private static final Map<Class<?>, Boolean> blacklistCache = new HashMap<>();
	private static final Set<String> blacklistedNames = new HashSet<>();

	static void addToBlacklist(String klass) {
		blacklistedNames.add(klass);
	}

	private static boolean isBlacklisted(Class<?> klass) {
		Boolean cached = blacklistCache.get(klass);
		if (cached != null) return cached;

		String name = klass.getName();
		boolean blacklisted = blacklistedNames.contains(name)
			|| Helpers.blacklisted(ConfigCore.Blacklist.blacklistTileEntities, name);

		blacklistCache.put(klass, blacklisted);
		return blacklisted;
	}
}
