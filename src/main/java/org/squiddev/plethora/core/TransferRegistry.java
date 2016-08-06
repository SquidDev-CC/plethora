package org.squiddev.plethora.core;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.*;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import org.objectweb.asm.Type;
import org.squiddev.plethora.api.PlethoraAPI;
import org.squiddev.plethora.api.transfer.ITransferProvider;
import org.squiddev.plethora.api.transfer.ITransferRegistry;
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

		HashSet<Class<?>> visited = Sets.newHashSet();
		Queue<Class<?>> toVisit = Queues.newArrayDeque();

		visited.add(target);
		toVisit.add(target);

		while (toVisit.size() > 0) {
			Class<?> klass = toVisit.poll();

			Collection<ITransferProvider<?>> items = providers.get(klass);
			int size = items.size();
			if (size > 0) {
				result.ensureCapacity(result.size() + size);
				for (ITransferProvider provider : items) {
					result.add(provider);
				}
			}

			Class<?> parent = klass.getSuperclass();
			if (parent != null && visited.add(parent)) {
				toVisit.add(parent);
			}

			for (Class<?> iface : klass.getInterfaces()) {
				if (iface != null && visited.add(iface)) {
					toVisit.add(iface);
				}
			}
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	private Object getTransferPart(Object object, String key, Multimap<Class<?>, ITransferProvider<?>> providers) {
		for (Object converted : PlethoraAPI.instance().converterRegistry().convertAll(object)) {
			HashSet<Class<?>> visited = Sets.newHashSet();
			Queue<Class<?>> toVisit = Queues.newArrayDeque();

			Class<?> target = converted.getClass();
			visited.add(target);
			toVisit.add(target);

			while (toVisit.size() > 0) {
				Class<?> klass = toVisit.poll();
				for (ITransferProvider provider : providers.get(klass)) {
					Object result = provider.getTransferLocation(converted, key);
					if (result != null) return result;
				}

				Class<?> parent = klass.getSuperclass();
				if (parent != null && visited.add(parent)) {
					toVisit.add(parent);
				}

				for (Class<?> iface : klass.getInterfaces()) {
					if (iface != null && visited.add(iface)) {
						toVisit.add(iface);
					}
				}
			}
		}

		return null;
	}

	@Nonnull
	@Override
	@SuppressWarnings("unchecked")
	public Set<String> getTransferLocations(@Nonnull Object object) {
		HashSet<String> parts = Sets.newHashSet();

		for (Object converted : PlethoraAPI.instance().converterRegistry().convertAll(object)) {
			HashSet<Class<?>> visited = Sets.newHashSet();
			Queue<Class<?>> toVisit = Queues.newArrayDeque();

			Class<?> target = converted.getClass();
			visited.add(target);
			toVisit.add(target);

			while (toVisit.size() > 0) {
				Class<?> klass = toVisit.poll();
				for (ITransferProvider provider : primary.get(klass)) {
					parts.addAll(provider.getTransferLocations(converted));
				}

				Class<?> parent = klass.getSuperclass();
				if (parent != null && visited.add(parent)) {
					toVisit.add(parent);
				}

				for (Class<?> iface : klass.getInterfaces()) {
					if (iface != null && visited.add(iface)) {
						toVisit.add(iface);
					}
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
				Map<String, Object> info = asmData.getAnnotationInfo();
				String modName = (String) info.get("modId");
				if (!Strings.isNullOrEmpty(modName) && !Helpers.modLoaded(modName)) {
					DebugLogger.debug("Skipping " + name + " as " + modName + " is not loaded");
					continue;
				}

				DebugLogger.debug("Registering " + name);

				Class<?> asmClass = Class.forName(name);
				ITransferProvider instance = asmClass.asSubclass(ITransferProvider.class).newInstance();

				Class<?> target = Class.forName(((Type) info.get("value")).getClassName());

				Boolean primary = (Boolean) info.get("primary");
				if (primary == null || primary) {
					registerPrimary(target, instance);
				} else {
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
