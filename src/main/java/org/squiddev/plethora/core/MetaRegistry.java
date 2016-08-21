package org.squiddev.plethora.core;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import org.objectweb.asm.Type;
import org.squiddev.plethora.api.PlethoraAPI;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.api.meta.IMetaRegistry;
import org.squiddev.plethora.api.meta.NamespacedMetaProvider;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.core.collections.SortedMultimap;
import org.squiddev.plethora.utils.DebugLogger;
import org.squiddev.plethora.utils.Helpers;

import javax.annotation.Nonnull;
import java.util.*;

public final class MetaRegistry implements IMetaRegistry {
	public static final MetaRegistry instance = new MetaRegistry();

	private final SortedMultimap<Class<?>, IMetaProvider<?>> providers = SortedMultimap.create(new Comparator<IMetaProvider<?>>() {
		@Override
		public int compare(IMetaProvider<?> o1, IMetaProvider<?> o2) {
			int p1 = o1.getPriority();
			int p2 = o2.getPriority();
			return (p1 < p2) ? -1 : ((p1 == p2) ? 0 : 1);
		}
	});

	@Override
	public <T> void registerMetaProvider(@Nonnull Class<T> target, @Nonnull IMetaProvider<T> provider) {
		Preconditions.checkNotNull(target, "target cannot be null");
		Preconditions.checkNotNull(provider, "provider cannot be null");

		providers.put(target, provider);

		// TODO: Can we walk .getGenericSubclass/.getGenericInterface to check that target type is correct?
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

		// TODO: Handle priority across each conversion correctly

		HashMap<Object, Object> out = Maps.newHashMap();

		List<?> objects = PlethoraAPI.instance().converterRegistry().convertAll(object);
		Collections.reverse(objects);
		for (Object child : objects) {
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
		for (ASMDataTable.ASMData asmData : asmDataTable.getAll(IMetaProvider.Inject.class.getName())) {
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
				IMetaProvider instance = asmClass.asSubclass(IMetaProvider.class).newInstance();

				Class<?> target = Class.forName(((Type) info.get("value")).getClassName());
				String namespace = (String) info.get("namespace");
				if (Strings.isNullOrEmpty(namespace)) {
					registerMetaProvider(target, instance);
				} else {
					registerMetaProvider(target, namespace, instance);
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
