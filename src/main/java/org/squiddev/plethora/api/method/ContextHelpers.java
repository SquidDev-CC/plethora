package org.squiddev.plethora.api.method;

import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.reference.Reference;
import org.squiddev.plethora.core.ContextFactory;
import org.squiddev.plethora.core.executor.BasicExecutor;
import org.squiddev.plethora.integration.MetaWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class ContextHelpers {
	private ContextHelpers() {
	}

	/**
	 * Generate a Lua list with the metadata taken for each element in the list
	 *
	 * @param context The base context to use in getting metadata.
	 * @param list    The list to get items from.
	 * @return The converted list.
	 */
	@Nonnull
	public static Map<Integer, Map<String, ?>> getMetaList(@Nonnull IPartialContext<?> context, @Nullable Collection<?> list) {
		if (list == null) return Collections.emptyMap();

		LuaList<Map<String, ?>> out = new LuaList<>(list.size());
		for (Object element : list) {
			out.add(element == null ? null : context.makePartialChild(element).getMeta());
		}

		return out.asMap();
	}

	/**
	 * Generate a Lua list with the metadata taken for each element in the list
	 *
	 * @param context The base context to use in getting metadata.
	 * @param list    The list to get items from.
	 * @return The converted list.
	 */
	@Nonnull
	public static Map<Integer, Map<String, ?>> getMetaList(@Nonnull IPartialContext<?> context, @Nullable Object[] list) {
		if (list == null) return Collections.emptyMap();

		Map<Integer, Map<String, ?>> map = new HashMap<>(list.length);
		for (int i = 0; i < list.length; i++) {
			Object element = list[i];
			if (element != null) {
				map.put(i + 1, context.makePartialChild(element).getMeta());
			}
		}

		return map;
	}

	/**
	 * Generate a Lua list with the {@link TypedLuaObject} taken for each element in the list.
	 *
	 * This uses the identity reference ({@link Reference#id(Object)}) to capture objects.
	 *
	 * @param context The base context to use in getting objects.
	 * @param list    The list to get items from.
	 * @return The converted list.
	 */
	@Nonnull
	public static <T> Map<Integer, TypedLuaObject<T>> getObjectList(@Nonnull IContext<?> context, @Nullable Collection<T> list) {
		if (list == null) return Collections.emptyMap();

		int i = 0;
		Map<Integer, TypedLuaObject<T>> map = new HashMap<>(list.size());
		for (T element : list) {
			if (element == null) {
				++i;
			} else {
				map.put(++i, context.makeChildId(element).getObject());
				map.put(++i, context.makeChild(element, Reference.id(element)).getObject());
			}
		}

		return map;
	}

	/**
	 * Wrap an {@link ItemStack} so that its metadata is exposed by an in-game call to {@code getMetadata()}
	 *
	 * @param context The base context to use in getting metadata
	 * @param object  The stack to wrap
	 * @return The wrapped stack
	 */
	@Nullable
	public static TypedLuaObject<MetaWrapper<ItemStack>> wrapStack(@Nonnull IPartialContext<?> context, @Nullable ItemStack object) {
		if (object == null || object.isEmpty()) return null;

		MetaWrapper<ItemStack> wrapper = MetaWrapper.of(object.copy());
		return context instanceof IContext
			? ((IContext<?>) context).makeChildId(wrapper).getObject()
			: ContextFactory.of(wrapper).withExecutor(BasicExecutor.INSTANCE).getObject();
	}
}
