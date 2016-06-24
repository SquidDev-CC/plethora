package org.squiddev.plethora.impl;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.*;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.api.meta.IMetaRegistry;
import org.squiddev.plethora.api.meta.MetaProvider;
import org.squiddev.plethora.api.meta.NamespacedMetaProvider;
import org.squiddev.plethora.utils.DebugLogger;

import java.util.*;

public class MetaRegistry implements IMetaRegistry {
	public static final MetaRegistry instance = new MetaRegistry();

	private final Multimap<Class<?>, IMetaProvider<?>> providers = MultimapBuilder.hashKeys().hashSetValues().build();

	@Override
	public <T> void registerMetaProvider(Class<T> target, IMetaProvider<T> provider) {
		Preconditions.checkNotNull(target, "target cannot be null");
		Preconditions.checkNotNull(provider, "provider cannot be null");

		HashSet<Class<?>> visited = Sets.newHashSet();
		Queue<Class<?>> toVisit = Queues.newArrayDeque();

		visited.add(target);
		toVisit.add(target);

		while (toVisit.size() > 0) {
			Class<?> klass = toVisit.poll();
			providers.put(klass, provider);

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

	@Override
	public <T> void registerMetaProvider(Class<T> target, String namespace, IMetaProvider<T> provider) {
		registerMetaProvider(target, new NamespacedMetaProvider<T>(namespace, provider));
	}

	@Override
	@SuppressWarnings("unchecked")
	public Map<String, Object> getMeta(Object object) {
		HashMap<String, Object> out = Maps.newHashMap();
		for (IMetaProvider provider : getMetaProviders(object.getClass())) {
			out.putAll(provider.getMeta(object));
		}

		return out;
	}

	@Override
	public Collection<IMetaProvider<?>> getMetaProviders(Class<?> target) {
		return providers.get(target);
	}

	@SuppressWarnings("unchecked")
	public void loadAsm(ASMDataTable asmDataTable) {
		for (ASMDataTable.ASMData asmData : asmDataTable.getAll(MetaProvider.class.getCanonicalName())) {
			try {
				Class<?> asmClass = Class.forName(asmData.getClassName());
				Map<String, Object> info = asmData.getAnnotationInfo();

				IMetaProvider instance = asmClass.asSubclass(IMetaProvider.class).newInstance();

				Class<?> target = (Class<?>) info.get("value");
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
