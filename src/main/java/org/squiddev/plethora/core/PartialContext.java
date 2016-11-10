package org.squiddev.plethora.core;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import net.minecraft.util.ResourceLocation;
import org.squiddev.plethora.api.PlethoraAPI;
import org.squiddev.plethora.api.method.ICostHandler;
import org.squiddev.plethora.api.method.IPartialContext;
import org.squiddev.plethora.api.transfer.ITransferRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.squiddev.plethora.core.UnbakedContext.arrayCopy;

public class PartialContext<T> implements IPartialContext<T> {
	protected final T target;
	protected final Object[] context;
	protected final ICostHandler handler;
	protected final Set<ResourceLocation> modules;

	public PartialContext(@Nonnull T target, @Nonnull ICostHandler handler, @Nonnull Object[] context, @Nonnull Set<ResourceLocation> modules) {
		this.target = target;
		this.handler = handler;
		this.context = context;
		this.modules = Collections.unmodifiableSet(modules);
	}

	@Nonnull
	@Override
	public T getTarget() {
		return target;
	}

	public Object[] getContext() {
		return context;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <V> V getContext(@Nonnull Class<V> klass) {
		Preconditions.checkNotNull(klass, "klass cannot be null");

		for (int i = context.length - 1; i >= 0; i--) {
			Object obj = context[i];
			if (klass.isInstance(obj)) return (V) obj;
		}

		return null;
	}

	@Override
	public <V> boolean hasContext(@Nonnull Class<V> klass) {
		Preconditions.checkNotNull(klass, "klass cannot be null");

		for (int i = context.length - 1; i >= 0; i--) {
			Object obj = context[i];
			if (klass.isInstance(obj)) return true;
		}

		return false;
	}

	@Nonnull
	@Override
	public <U> IPartialContext<U> makePartialChild(@Nonnull U newTarget, @Nonnull Object... newContext) {
		Preconditions.checkNotNull(newTarget, "target cannot be null");
		Preconditions.checkNotNull(newContext, "context cannot be null");

		Object[] wholeContext = new Object[newContext.length + context.length + 1];
		arrayCopy(newContext, wholeContext, 0);
		arrayCopy(context, wholeContext, newContext.length);
		wholeContext[wholeContext.length - 1] = target;

		return new PartialContext<U>(newTarget, handler, wholeContext, modules);
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
		Object found = registry.getTransferPart(target, primary, false);
		if (found == null) {
			for (int i = context.length - 1; i >= 0; i--) {
				found = registry.getTransferPart(context[i], primary, false);
				if (found != null) break;
			}

			if (found == null) return null;
		}

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

		out.addAll(registry.getTransferLocations(target));
		for (int i = context.length - 1; i >= 0; i--) {
			out.addAll(registry.getTransferLocations(context[i]));
		}

		return out;
	}

	@Override
	public boolean hasModule(@Nonnull ResourceLocation module) {
		return modules.contains(module);
	}

	@Nonnull
	@Override
	public Set<ResourceLocation> getModules() {
		return modules;
	}

	@Nonnull
	@Override
	public Map<Object, Object> getMeta() {
		return MetaRegistry.instance.getMeta(this);
	}
}
