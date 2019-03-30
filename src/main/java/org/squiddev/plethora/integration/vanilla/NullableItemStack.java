package org.squiddev.plethora.integration.vanilla;

import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.BasicMetaProvider;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

/**
 * An item stack which may have a count of 0 but still represent an item.
 */
public final class NullableItemStack {
	private final int count;
	private final ItemStack filledStack;

	private NullableItemStack(ItemStack stack, int count) {
		this.count = count;
		filledStack = stack;
	}

	public static NullableItemStack normal(@Nonnull ItemStack stack) {
		return new NullableItemStack(stack, stack.getCount());
	}

	public static NullableItemStack empty(@Nonnull ItemStack stack) {
		return new NullableItemStack(stack, 0);
	}

	public int getCount() {
		return count;
	}

	@Nonnull
	public ItemStack getFilledStack() {
		return filledStack;
	}

	@Injects
	public static final class MetaNullableItemStack extends BasicMetaProvider<NullableItemStack> {
		public MetaNullableItemStack() {
			super("Simply wraps an item stack with additional information. Refer to the documentation on those instead for more information.");
		}

		@Nonnull
		@Override
		public Map<Object, Object> getMeta(@Nonnull NullableItemStack object) {
			return Collections.singletonMap("count", object.count);
		}
	}
}
