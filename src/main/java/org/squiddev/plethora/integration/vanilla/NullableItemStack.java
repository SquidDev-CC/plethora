package org.squiddev.plethora.integration.vanilla;

import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.converter.ConstantConverter;
import org.squiddev.plethora.api.converter.IConverter;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.integration.vanilla.meta.MetaItemBasic;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * An item stack which may have a count of 0 but still represent an item.
 */
public class NullableItemStack {
	private final int count;
	private final ItemStack filledStack;

	private NullableItemStack(ItemStack stack, int count) {
		this.count = count;
		this.filledStack = stack;
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

	@Nonnull
	public HashMap<Object, Object> getBasicProperties() {
		HashMap<Object, Object> result = MetaItemBasic.getBasicMeta(filledStack);
		result.put("count", count);
		return result;
	}

	@IConverter.Inject(value = NullableItemStack.class)
	public static class ConverterNullableItemStack extends ConstantConverter<NullableItemStack, ItemStack> {
		@Nullable
		@Override
		public ItemStack convert(@Nonnull NullableItemStack from) {
			return from.filledStack;
		}
	}

	@IMetaProvider.Inject(value = NullableItemStack.class)
	public static class MetaNullableItemStack extends BasicMetaProvider<NullableItemStack> {
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
