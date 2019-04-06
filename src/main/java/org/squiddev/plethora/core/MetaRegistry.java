package org.squiddev.plethora.core;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import org.objectweb.asm.Type;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.api.meta.IMetaRegistry;
import org.squiddev.plethora.api.meta.NamespacedMetaProvider;
import org.squiddev.plethora.api.method.ContextKeys;
import org.squiddev.plethora.api.method.IPartialContext;
import org.squiddev.plethora.core.collections.ClassIteratorIterable;
import org.squiddev.plethora.core.collections.SortedMultimap;
import org.squiddev.plethora.utils.Helpers;

import javax.annotation.Nonnull;
import java.util.*;

public final class MetaRegistry implements IMetaRegistry {
	public static final MetaRegistry instance = new MetaRegistry();

	final SortedMultimap<Class<?>, IMetaProvider<?>> providers = SortedMultimap.create(Comparator.comparingInt(IMetaProvider::getPriority));
	private static final Map<IMetaProvider<?>, String> names = new HashMap<>();

	<T> void registerMetaProvider(@Nonnull Class<T> target, @Nonnull IMetaProvider<T> provider, @Nonnull String name) {
		Objects.requireNonNull(target, "target cannot be null");
		Objects.requireNonNull(provider, "provider cannot be null");
		Objects.requireNonNull(name, "name cannot be null");

		providers.put(target, provider);
		names.put(provider, name);
	}

	public String getName(@Nonnull IMetaProvider<?> provider) {
		String name = names.get(provider);
		if (name != null) return name;
		return provider.getClass().getName();
	}

	@Nonnull
	@SuppressWarnings("unchecked")
	public Map<String, ?> getMeta(@Nonnull PartialContext<?> context) {
		Objects.requireNonNull(context, "context cannot be null");

		String[] keys = context.keys;
		Object[] values = context.values;

		// TODO: Handle priority across each conversion correctly

		HashMap<String, Object> out = new HashMap<>();
		for (int i = values.length - 1; i >= 0; i--) {
			if (!ContextKeys.TARGET.equals(keys[i])) continue;

			Object child = values[i];
			IPartialContext<?> childContext = context.withIndex(i);

			for (IMetaProvider provider : getMetaProviders(child.getClass())) {
				Map<String, ?> res = provider.getMeta(childContext);
				if (res == null) {
					PlethoraCore.LOG.error("Meta provider {} returned null", getName(provider));
					continue;
				}
				out.putAll(res);
			}
		}

		return out;
	}

	@Nonnull
	@Override
	public List<IMetaProvider<?>> getMetaProviders(@Nonnull Class<?> target) {
		Objects.requireNonNull(target, "target cannot be null");

		List<IMetaProvider<?>> result = Lists.newArrayList();

		for (Class<?> klass : new ClassIteratorIterable(target)) {
			result.addAll(providers.get(klass));
		}

		return Collections.unmodifiableList(result);
	}

	@SuppressWarnings("unchecked")
	void loadAsm(ASMDataTable asmDataTable) {
		for (ASMDataTable.ASMData asmData : asmDataTable.getAll(IMetaProvider.Inject.class.getName())) {
			String name = asmData.getClassName();
			try {
				if (Helpers.blacklisted(ConfigCore.Blacklist.blacklistProviders, name)) {
					PlethoraCore.LOG.debug("Ignoring " + name + " as it has been blacklisted");
					continue;
				}

				Map<String, Object> info = asmData.getAnnotationInfo();
				String modId = (String) info.get("modId");
				if (!Strings.isNullOrEmpty(modId) && !Helpers.modLoaded(modId)) {
					PlethoraCore.LOG.debug("Skipping " + name + " as " + modId + " is not loaded or is blacklisted");
					continue;
				}

				PlethoraCore.LOG.debug("Registering " + name);

				Class<?> asmClass = Class.forName(name);
				IMetaProvider instance = asmClass.asSubclass(IMetaProvider.class).newInstance();

				Class<?> target = Class.forName(((Type) info.get("value")).getClassName());
				Helpers.assertTarget(asmClass, target, IMetaProvider.class);

				String namespace = (String) info.get("namespace");
				if (Strings.isNullOrEmpty(namespace)) {
					registerMetaProvider(target, instance, name);
				} else {
					registerMetaProvider(target, new NamespacedMetaProvider(namespace, instance), name);
				}
			} catch (Throwable e) {
				if (ConfigCore.Testing.strict) {
					throw new IllegalStateException("Failed to load: " + name, e);
				} else {
					PlethoraCore.LOG.error("Failed to load: " + name, e);
				}
			}
		}
	}
}
