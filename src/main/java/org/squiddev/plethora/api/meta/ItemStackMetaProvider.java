package org.squiddev.plethora.api.meta;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

/**
 * A meta provider for {@link ItemStack}s whose item has a specific type.
 *
 * @param <T> The type the stack's item must have.
 */
public abstract class ItemStackMetaProvider<T> extends BasicMetaProvider<ItemStack> {
	private final Class<T> type;
	private final String namespace;

	public ItemStackMetaProvider(String namespace, Class<T> type, int priority, String description) {
		super(priority, description);
		this.type = type;
		this.namespace = namespace;
	}

	public ItemStackMetaProvider(String namespace, Class<T> type, String description) {
		super(description);
		this.type = type;
		this.namespace = namespace;
	}

	public ItemStackMetaProvider(String namespace, Class<T> type, int priority) {
		super(priority);
		this.type = type;
		this.namespace = namespace;
	}

	public ItemStackMetaProvider(String namespace, Class<T> type) {
		this.type = type;
		this.namespace = namespace;
	}

	public ItemStackMetaProvider(Class<T> type, int priority, String description) {
		this(null, type, priority, description);
	}

	public ItemStackMetaProvider(Class<T> type, String description) {
		this(null, type, description);
	}

	public ItemStackMetaProvider(Class<T> type, int priority) {
		this(null, type, priority);
	}

	public ItemStackMetaProvider(Class<T> type) {
		this(null, type);
	}

	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull ItemStack object) {
		Item item = object.getItem();
		if (!type.isInstance(item)) return Collections.emptyMap();

		@SuppressWarnings("unchecked")
		Map<Object, Object> child = getMeta(object, (T) item);
		return namespace == null || child.isEmpty() ? child : Collections.singletonMap(namespace, child);
	}

	@Nonnull
	public abstract Map<Object, Object> getMeta(@Nonnull ItemStack stack, @Nonnull T item);
}
