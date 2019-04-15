package org.squiddev.plethora.gameplay.modules;

import org.squiddev.plethora.api.reference.ConstantReference;

import javax.annotation.Nonnull;
import java.util.function.IntUnaryOperator;

/**
 * Provides the range for a {@link PlethoraModules#SENSOR} or {@link PlethoraModules#SCANNER}
 */
public interface RangeInfo extends ConstantReference<RangeInfo> {
	/**
	 * The maximum range this module operates at.
	 *
	 * @return This module's range.
	 */
	int getRange();

	/**
	 * The cost for some bulk operation (sense/scan).
	 *
	 * @return The cost of a bulk operation.
	 * @see org.squiddev.plethora.api.method.ICostHandler
	 */
	int getBulkCost();

	@Nonnull
	@Override
	default RangeInfo get() {
		return this;
	}

	@Nonnull
	@Override
	default RangeInfo safeGet() {
		return this;
	}

	static RangeInfo of(int level, IntUnaryOperator cost, IntUnaryOperator range) {
		return new RangeInfo() {
			@Override
			public int getRange() {
				return range.applyAsInt(level);
			}

			@Override
			public int getBulkCost() {
				return cost.applyAsInt(level);
			}
		};
	}
}
