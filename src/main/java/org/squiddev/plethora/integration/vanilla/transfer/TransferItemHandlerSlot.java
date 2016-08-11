package org.squiddev.plethora.integration.vanilla.transfer;

import net.minecraftforge.items.IItemHandler;
import org.squiddev.plethora.api.transfer.ITransferProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.AbstractSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

/**
 * Transfer location that allows accessing child slots
 */
@ITransferProvider.Inject(value = IItemHandler.class, primary = false)
public class TransferItemHandlerSlot implements ITransferProvider<IItemHandler> {
	@Nullable
	@Override
	public Object getTransferLocation(@Nonnull IItemHandler object, @Nonnull String key) {
		try {
			int value = Integer.parseInt(key) - 1;
			if (value >= 0 && value < object.getSlots()) {
				return object.getStackInSlot(value);
			}
		} catch (NumberFormatException ignored) {
		}

		return null;
	}

	@Nonnull
	@Override
	public Set<String> getTransferLocations(@Nonnull IItemHandler object) {
		final int size = object.getSlots();
		if (size == 0) return Collections.emptySet();
		return new NumberSet(size);
	}

	private static class NumberSet extends AbstractSet<String> {
		private final int size;

		public NumberSet(int size) {
			this.size = size;
		}

		@Override
		public int size() {
			return size;
		}

		@Override
		public boolean isEmpty() {
			return size > 0;
		}

		@Override
		public boolean contains(Object o) {
			if (!(o instanceof String)) return false;
			try {
				int value = Integer.valueOf((String) o);
				return value >= 1 && value <= size();
			} catch (NumberFormatException ignored) {
			}

			return false;
		}

		@Nonnull
		@Override
		public Iterator<String> iterator() {
			return new Iterator<String>() {
				private int position = 0;

				@Override
				public boolean hasNext() {
					return position < size;
				}

				@Override
				public String next() {
					position++;
					return Integer.toString(position);
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		}
	}
}
