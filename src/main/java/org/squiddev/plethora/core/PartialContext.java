package org.squiddev.plethora.core;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import org.squiddev.plethora.api.PlethoraAPI;
import org.squiddev.plethora.api.method.ContextKeys;
import org.squiddev.plethora.api.method.ICostHandler;
import org.squiddev.plethora.api.method.IPartialContext;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.api.transfer.ITransferRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class PartialContext<T> implements IPartialContext<T> {
	protected final int target;
	protected final String[] keys;
	protected final Object[] values;
	protected final ICostHandler handler;
	protected final IModuleContainer modules;

	public PartialContext(int target, String[] keys, @Nonnull Object[] values, @Nonnull ICostHandler handler, @Nonnull IModuleContainer modules) {
		this.target = target;
		this.keys = keys;
		this.handler = handler;
		this.values = values;
		this.modules = modules;
	}

	@Nonnull
	@Override
	@SuppressWarnings("unchecked")
	public T getTarget() {
		return (T) values[target];
	}

	PartialContext<?> withIndex(int index) {
		return index == target ? this : new PartialContext(index, keys, values, handler, modules);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <V> V getContext(@Nonnull Class<V> klass) {
		Preconditions.checkNotNull(klass, "klass cannot be null");

		for (int i = values.length - 1; i >= 0; i--) {
			Object obj = values[i];
			if (klass.isInstance(obj)) return (V) obj;
		}

		return null;
	}

	@Override
	public <V> boolean hasContext(@Nonnull Class<V> klass) {
		Preconditions.checkNotNull(klass, "klass cannot be null");

		for (int i = values.length - 1; i >= 0; i--) {
			Object obj = values[i];
			if (klass.isInstance(obj)) return true;
		}

		return false;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <V> V getContext(@Nonnull String contextKey, @Nonnull Class<V> klass) {
		Preconditions.checkNotNull(contextKey, "contextKey cannot be null");
		Preconditions.checkNotNull(klass, "klass cannot be null");

		for (int i = values.length - 1; i >= 0; i--) {
			Object obj = values[i];
			if (contextKey.equals(keys[i]) && klass.isInstance(obj)) return (V) obj;
		}

		return null;
	}

	@Override
	public <V> boolean hasContext(@Nonnull String contextKey, @Nonnull Class<V> klass) {
		Preconditions.checkNotNull(klass, "klass cannot be null");

		for (int i = values.length - 1; i >= 0; i--) {
			Object obj = values[i];
			if (contextKey.equals(keys[i]) && klass.isInstance(obj)) return true;
		}

		return false;
	}

	@Nonnull
	@Override
	public <U> PartialContext<U> makePartialChild(@Nonnull U target) {
		Preconditions.checkNotNull(target, "target cannot be null");

		ArrayList<String> keys = new ArrayList<>(this.keys.length + 1);
		ArrayList<Object> values = new ArrayList<>(this.values.length + 1);

		Collections.addAll(keys, this.keys);
		Collections.addAll(values, this.values);

		for (int i = keys.size() - 1; i >= 0; i--) {
			if (!ContextKeys.TARGET.equals(keys.get(i))) continue;
			keys.set(i, ContextKeys.GENERIC);
		}

		// Add the new target and convert it.
		keys.add(ContextKeys.TARGET);
		values.add(target);
		ConverterRegistry.instance.extendConverted(keys, values, this.values.length);

		return new PartialContext<>(this.values.length, keys.toArray(new String[0]), values.toArray(), handler, modules);
	}

	@Nonnull
	@Override
	public ICostHandler getCostHandler() {
		return handler;
	}

	@Nullable
	@Override
	public Object getTransferLocation(@Nonnull String key) {
		Preconditions.checkNotNull(key, "key cannot be null");

		String[] parts = key.split("\\.");
		String primary = parts[0];

		ITransferRegistry registry = PlethoraAPI.instance().transferRegistry();

		// Lookup the primary
		Object found = null;
		for (int i = values.length - 1; i >= 0; i--) {
			found = registry.getTransferPart(values[i], primary, false);
			if (found != null) break;
		}

		if (found == null) return null;

		// Lookup the secondary from the primary.
		// This means that the root object is consistent: "<x>.<y>" will always target a sub-part of "<x>".
		for (int i = 1; i < parts.length; i++) {
			found = registry.getTransferPart(found, parts[i], true);
			if (found == null) return null;
		}

		return found;
	}

	@Nonnull
	@Override
	public Set<String> getTransferLocations() {
		Set<String> out = Sets.newHashSet();

		ITransferRegistry registry = PlethoraAPI.instance().transferRegistry();

		out.addAll(registry.getTransferLocations(target, true));
		for (int i = values.length - 1; i >= 0; i--) {
			out.addAll(registry.getTransferLocations(values[i], true));
		}

		return out;
	}

	@Nonnull
	@Override
	public IModuleContainer getModules() {
		return modules;
	}

	@Nonnull
	@Override
	public Map<Object, Object> getMeta() {
		return MetaRegistry.instance.getMeta(this);
	}
}
