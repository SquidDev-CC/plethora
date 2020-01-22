package org.squiddev.plethora.core;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import org.squiddev.plethora.api.PlethoraAPI;
import org.squiddev.plethora.api.transfer.ITransferProvider;
import org.squiddev.plethora.api.transfer.ITransferRegistry;
import org.squiddev.plethora.core.collections.ClassIteratorIterable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public final class TransferRegistry implements ITransferRegistry {
	public static final TransferRegistry instance = new TransferRegistry();

	private final List<TargetedRegisteredValue<? extends ITransferProvider<?>>> allPrimary = new ArrayList<>();
	private final List<TargetedRegisteredValue<? extends ITransferProvider<?>>> allSecondary = new ArrayList<>();

	private final Multimap<Class<?>, ITransferProvider<?>> primary = MultimapBuilder.hashKeys().hashSetValues().build();
	private final Multimap<Class<?>, ITransferProvider<?>> secondary = MultimapBuilder.hashKeys().hashSetValues().build();

	private TransferRegistry() {
	}

	void registerPrimary(@Nonnull TargetedRegisteredValue<? extends ITransferProvider<?>> provider) {
		Objects.requireNonNull(provider, "provider cannot be null");
		allPrimary.add(provider);
	}

	void registerSecondary(@Nonnull TargetedRegisteredValue<? extends ITransferProvider<?>> provider) {
		Objects.requireNonNull(provider, "provider cannot be null");
		allSecondary.add(provider);
	}

	void build() {
		TargetedRegisteredValue.buildCache(allPrimary, primary);
		TargetedRegisteredValue.buildCache(allSecondary, secondary);
	}

	@Nullable
	@Override
	public Object getTransferLocation(@Nonnull Object object, @Nonnull String key) {
		Multimap<Class<?>, ITransferProvider<?>> map = primary;
		String[] parts = key.split("\\.");

		for (String part : parts) {
			Object next = getTransferPart(object, part, map);
			if (next == null) {
				return null;
			} else {
				object = next;
				map = secondary;
			}
		}

		return object;
	}

	@Nullable
	@Override
	public Object getTransferPart(@Nonnull Object object, @Nonnull String part, boolean secondary) {
		return getTransferPart(object, part, secondary ? this.secondary : primary);
	}

	@SuppressWarnings("unchecked")
	private static Object getTransferPart(Object object, String key, Multimap<Class<?>, ITransferProvider<?>> providers) {
		for (Object converted : PlethoraAPI.instance().converterRegistry().convertAll(object)) {
			Class<?> target = converted.getClass();
			for (Class<?> klass : new ClassIteratorIterable(target)) {
				for (ITransferProvider provider : providers.get(klass)) {
					Object result = provider.getTransferLocation(converted, key);
					if (result != null) return result;
				}
			}
		}

		return null;
	}

	@Nonnull
	@Override
	@SuppressWarnings("unchecked")
	public Set<String> getTransferLocations(@Nonnull Object object, boolean primary) {
		HashSet<String> parts = new HashSet<>();

		Multimap<Class<?>, ITransferProvider<?>> lookup = primary ? this.primary : secondary;
		for (Object converted : PlethoraAPI.instance().converterRegistry().convertAll(object)) {
			Class<?> target = converted.getClass();
			for (Class<?> klass : new ClassIteratorIterable(target)) {
				for (ITransferProvider provider : lookup.get(klass)) {
					parts.addAll(provider.getTransferLocations(converted));
				}
			}
		}

		return Collections.unmodifiableSet(parts);
	}
}
