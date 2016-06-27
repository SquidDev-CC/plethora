package org.squiddev.plethora.impl;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.*;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import org.objectweb.asm.Type;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.api.meta.IMetaRegistry;
import org.squiddev.plethora.api.meta.MetaProvider;
import org.squiddev.plethora.api.meta.NamespacedMetaProvider;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.utils.DebugLogger;

import javax.annotation.Nonnull;
import java.util.*;

public final class MetaRegistry implements IMetaRegistry {
	public static final MetaRegistry instance = new MetaRegistry();

	private final Multimap<Class<?>, IMetaProvider<?>> providers = MultimapBuilder.hashKeys().hashSetValues().build();

	@Override
	public <T> void registerMetaProvider(@Nonnull Class<T> target, @Nonnull IMetaProvider<T> provider) {
		Preconditions.checkNotNull(target, "target cannot be null");
		Preconditions.checkNotNull(provider, "provider cannot be null");

		providers.put(target, provider);
	}

	@Override
	public <T> void registerMetaProvider(@Nonnull Class<T> target, @Nonnull String namespace, @Nonnull IMetaProvider<T> provider) {
		registerMetaProvider(target, new NamespacedMetaProvider<T>(namespace, provider));
	}

	@Nonnull
	@Override
	@SuppressWarnings("unchecked")
	public Map<Object, Object> getMeta(@Nonnull Object object) {
		Preconditions.checkNotNull(object, "object cannot be null");
		if (object instanceof IContext || object instanceof IUnbakedContext) {
			throw new IllegalArgumentException("Trying to get instance of context. This is probably a bug");
		}

		HashMap<Object, Object> out = Maps.newHashMap();

		for (Object child : ConverterRegistry.instance.convertAll(object)) {
			for (IMetaProvider provider : getMetaProviders(child.getClass())) {
				out.putAll(provider.getMeta(child));
			}
		}

		return out;
	}

	@Nonnull
	@Override
	public List<IMetaProvider<?>> getMetaProviders(@Nonnull Class<?> target) {
		Preconditions.checkNotNull(target, "target cannot be null");

		List<IMetaProvider<?>> result = Lists.newArrayList();

		HashSet<Class<?>> visited = Sets.newHashSet();
		Queue<Class<?>> toVisit = Queues.newArrayDeque();

		visited.add(target);
		toVisit.add(target);

		while (toVisit.size() > 0) {
			Class<?> klass = toVisit.poll();
			result.addAll(providers.get(klass));

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

		return Collections.unmodifiableList(result);
	}

	@SuppressWarnings("unchecked")
	public void loadAsm(ASMDataTable asmDataTable) {
		for (ASMDataTable.ASMData asmData : asmDataTable.getAll(MetaProvider.class.getCanonicalName())) {
			try {
				DebugLogger.debug("Registering " + asmData.getClassName());

				Class<?> asmClass = Class.forName(asmData.getClassName());
				Map<String, Object> info = asmData.getAnnotationInfo();

				IMetaProvider instance = asmClass.asSubclass(IMetaProvider.class).newInstance();

				Class<?> target = Class.forName(((Type) info.get("value")).getClassName());
				String namespace = (String) info.get("namespace");
				if (Strings.isNullOrEmpty(namespace)) {
					registerMetaProvider(target, instance);
				} else {
					registerMetaProvider(target, namespace, instance);
				}
			} catch (ClassNotFoundException e) {
				DebugLogger.error("Failed to load: %s", asmData.getClassName(), e);
			} catch (IllegalAccessException e) {
				DebugLogger.error("Failed to load: %s", asmData.getClassName(), e);
			} catch (InstantiationException e) {
				DebugLogger.error("Failed to load: %s", asmData.getClassName(), e);
			}
		}
	}
}
