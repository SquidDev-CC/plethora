package org.squiddev.plethora.core;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Sets;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import org.objectweb.asm.Type;
import org.squiddev.plethora.api.PlethoraAPI;
import org.squiddev.plethora.api.transfer.ITransferProvider;
import org.squiddev.plethora.api.transfer.ITransferRegistry;
import org.squiddev.plethora.core.collections.ClassIteratorIterable;
import org.squiddev.plethora.utils.DebugLogger;
import org.squiddev.plethora.utils.Helpers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public final class TransferRegistry implements ITransferRegistry {
	public static final TransferRegistry instance = new TransferRegistry();

	private final Multimap<Class<?>, ITransferProvider<?>> primary = MultimapBuilder.hashKeys().hashSetValues().build();
	private final Multimap<Class<?>, ITransferProvider<?>> secondary = MultimapBuilder.hashKeys().hashSetValues().build();

	private TransferRegistry() {
	}

	@Override
	public <T> void registerPrimary(@Nonnull Class<T> klass, @Nonnull ITransferProvider<T> provider) {
		Preconditions.checkNotNull(klass, "klass cannot be null");
		Preconditions.checkNotNull(provider, "provider cannot be null");

		primary.put(klass, provider);
	}

	@Override
	public <T> void registerSecondary(@Nonnull Class<T> klass, @Nonnull ITransferProvider<T> provider) {
		Preconditions.checkNotNull(klass, "klass cannot be null");
		Preconditions.checkNotNull(provider, "provider cannot be null");

		secondary.put(klass, provider);
	}

	@Nonnull
	@Override
	public <T> Collection<ITransferProvider<? super T>> getPrimaryProviders(@Nonnull Class<T> klass) {
		return getTransferProviders(klass, primary);
	}

	@Nonnull
	@Override
	public <T> Collection<ITransferProvider<? super T>> getSecondaryProviders(Class<T> klass) {
		return getTransferProviders(klass, secondary);
	}


	@Nullable
	@Override
	@SuppressWarnings("unchecked")
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
		return getTransferPart(object, part, secondary ? this.secondary : this.primary);
	}

	@SuppressWarnings("unchecked")
	private <T> Collection<ITransferProvider<? super T>> getTransferProviders(Class<T> target, Multimap<Class<?>, ITransferProvider<?>> providers) {
		ArrayList<ITransferProvider<? super T>> result = Lists.newArrayList();

		for (Class<?> klass : new ClassIteratorIterable(target)) {
			Collection<ITransferProvider<?>> items = providers.get(klass);
			int size = items.size();
			if (size > 0) {
				result.ensureCapacity(result.size() + size);
				for (ITransferProvider provider : items) {
					result.add(provider);
				}
			}
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	private Object getTransferPart(Object object, String key, Multimap<Class<?>, ITransferProvider<?>> providers) {
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
		HashSet<String> parts = Sets.newHashSet();

		Multimap<Class<?>, ITransferProvider<?>> lookup = primary ? this.primary : this.secondary;
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

	@SuppressWarnings("unchecked")
	public void loadAsm(ASMDataTable asmDataTable) {
		for (ASMDataTable.ASMData asmData : asmDataTable.getAll(ITransferProvider.Inject.class.getName())) {
			String name = asmData.getClassName();
			try {
				if (Helpers.classBlacklisted(ConfigCore.Blacklist.blacklistProviders, name)) {
					DebugLogger.debug("Ignoring " + name + " as it has been blacklisted");
					continue;
				}

				Map<String, Object> info = asmData.getAnnotationInfo();
				String modId = (String) info.get("modId");
				if (!Strings.isNullOrEmpty(modId) && !Helpers.modLoaded(modId)) {
					DebugLogger.debug("Skipping " + name + " as " + modId + " is not loaded or is blacklisted");
					continue;
				}

				DebugLogger.debug("Registering " + name);

				Class<?> asmClass = Class.forName(name);
				ITransferProvider instance = asmClass.asSubclass(ITransferProvider.class).newInstance();

				Class<?> target = Class.forName(((Type) info.get("value")).getClassName());
				Helpers.assertTarget(asmClass, target, ITransferProvider.class);

				Boolean primary = (Boolean) info.get("primary");
				if (primary == null || primary) {
					registerPrimary(target, instance);
				}

				Boolean secondary = (Boolean) info.get("secondary");
				if (secondary == null || secondary) {
					registerSecondary(target, instance);
				}
			} catch (Throwable e) {
				if (ConfigCore.Testing.strict) {
					throw new IllegalStateException("Failed to load: " + name, e);
				} else {
					DebugLogger.error("Failed to load: " + name, e);
				}
			}
		}
	}
}
