package org.squiddev.plethora.api.meta;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.method.IPartialContext;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

/**
 * A meta provider for {@link ItemStack}s whose item has a specific type.
 *
 * @param <T> The type the stack's item must have.
 */
public abstract class ItemStackContextMetaProvider<T> extends BaseMetaProvider<ItemStack> {
	private final Class<T> type;
	private final String namespace;

	public ItemStackContextMetaProvider(String namespace, Class<T> type, int priority, String description) {
		super(priority, description);
		this.type = type;
		this.namespace = namespace;
	}

	public ItemStackContextMetaProvider(String namespace, Class<T> type, String description) {
		super(description);
		this.type = type;
		this.namespace = namespace;
	}

	public ItemStackContextMetaProvider(String namespace, Class<T> type, int priority) {
		super(priority);
		this.type = type;
		this.namespace = namespace;
	}

	public ItemStackContextMetaProvider(String namespace, Class<T> type) {
		this.type = type;
		this.namespace = namespace;
	}

	public ItemStackContextMetaProvider(Class<T> type, int priority, String description) {
		this(null, type, priority, description);
	}

	public ItemStackContextMetaProvider(Class<T> type, String description) {
		this(null, type, description);
	}

	public ItemStackContextMetaProvider(Class<T> type, int priority) {
		this(null, type, priority);
	}

	public ItemStackContextMetaProvider(Class<T> type) {
		this(null, type);
	}

	@Nonnull
	@Override
	public Map<String, ?> getMeta(@Nonnull IPartialContext<ItemStack> context) {
		Item item = context.getTarget().getItem();
		if (!type.isInstance(item)) return Collections.emptyMap();

		Map<String, ?> child = getMeta(context, type.cast(item));
		return namespace == null || child.isEmpty() ? child : Collections.singletonMap(namespace, child);
	}

	@Nonnull
	public abstract Map<String, ?> getMeta(@Nonnull IPartialContext<ItemStack> context, @Nonnull T item);
}
