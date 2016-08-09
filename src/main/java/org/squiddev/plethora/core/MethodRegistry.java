package org.squiddev.plethora.core;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.*;
import net.minecraft.util.Tuple;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import org.objectweb.asm.Type;
import org.squiddev.plethora.api.Constants;
import org.squiddev.plethora.api.PlethoraAPI;
import org.squiddev.plethora.api.method.*;
import org.squiddev.plethora.api.reference.IReference;
import org.squiddev.plethora.api.reference.Reference;
import org.squiddev.plethora.core.capabilities.DefaultCostHandler;
import org.squiddev.plethora.core.collections.SortedMultimap;
import org.squiddev.plethora.utils.DebugLogger;
import org.squiddev.plethora.utils.Helpers;

import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;
import java.util.*;

public final class MethodRegistry implements IMethodRegistry {
	public static final MethodRegistry instance = new MethodRegistry();
	private final IReference<?>[] emptyReference = new IReference[0];

	private final SortedMultimap<Class<?>, IMethod<?>> providers = SortedMultimap.create(new Comparator<IMethod<?>>() {
		@Override
		public int compare(IMethod<?> o1, IMethod<?> o2) {
			int p1 = o1.getPriority();
			int p2 = o2.getPriority();
			return (p1 < p2) ? -1 : ((p1 == p2) ? 0 : 1);
		}
	});

	@Override
	public <T> void registerMethod(@Nonnull Class<T> target, @Nonnull IMethod<T> method) {
		Preconditions.checkNotNull(target, "target cannot be null");
		Preconditions.checkNotNull(method, "method cannot be null");

		providers.put(target, method);
	}

	@Nonnull
	@Override
	@SuppressWarnings("unchecked")
	public <T> List<IMethod<T>> getMethods(@Nonnull IContext<T> context) {
		Preconditions.checkNotNull(context, "context cannot be null");

		List<IMethod<T>> methods = Lists.newArrayList();

		for (IMethod<?> genMethod : getMethods(context.getTarget().getClass())) {
			final IMethod<T> method = (IMethod<T>) genMethod;
			if (method.canApply(context)) methods.add(method);
		}

		return Collections.unmodifiableList(methods);
	}

	@Nonnull
	@Override
	public List<IMethod<?>> getMethods(@Nonnull Class<?> target) {
		Preconditions.checkNotNull(target, "target cannot be null");

		List<IMethod<?>> result = Lists.newArrayList();

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

	@Nonnull
	@Override
	public Multimap<Class<?>, IMethod<?>> getMethods() {
		Multimap<Class<?>, IMethod<?>> map = MultimapBuilder.hashKeys().arrayListValues().build();

		for (Map.Entry<Class<?>, Collection<IMethod<?>>> item : providers.items().entrySet()) {
			map.putAll(item.getKey(), item.getValue());
		}

		return map;
	}

	@Nonnull
	@Override
	public <T> IUnbakedContext<T> makeContext(@Nonnull IReference<T> target, @Nonnull ICostHandler handler, @Nonnull IReference<?>... context) {
		Preconditions.checkNotNull(target, "target cannot be null");
		Preconditions.checkNotNull(handler, "handler cannot be null");
		Preconditions.checkNotNull(context, "context cannot be null");
		return new UnbakedContext<T>(target, handler, context);
	}

	@Nonnull
	@Override
	public ICostHandler getCostHandler(@Nonnull ICapabilityProvider object) {
		Preconditions.checkNotNull(object, "object cannot be null");
		ICostHandler handler = object.getCapability(Constants.COST_HANDLER_CAPABILITY, null);
		return handler != null ? handler : DefaultCostHandler.get(object);
	}

	@Override
	public <T extends Annotation> void registerMethodBuilder(@Nonnull Class<T> klass, @Nonnull IMethodBuilder<T> builder) {
		Preconditions.checkNotNull(klass, "klass cannot be null");
		Preconditions.checkNotNull(builder, "builder cannot be null");

		MethodTypeBuilder.instance.addBuilder(klass, builder);
	}

	public Tuple<List<IMethod<?>>, List<IUnbakedContext<?>>> getMethodsPaired(IUnbakedContext<?> initialContext, IContext<?> initialBaked) {
		// TODO: Handle priority correctly.

		ArrayList<IMethod<?>> methods = Lists.newArrayList();
		ArrayList<IUnbakedContext<?>> contexts = Lists.newArrayList();

		Object initialTarget = initialBaked.getTarget();
		for (Object obj : PlethoraAPI.instance().converterRegistry().convertAll(initialTarget)) {
			IUnbakedContext<?> ctx = null;
			IContext<?> ctxBaked;

			if (obj == initialTarget) {
				ctxBaked = initialBaked;
			} else {
				ctxBaked = initialBaked.makeBakedChild(obj);
			}

			for (IMethod method : getMethods(ctxBaked)) {
				// Lazy load context
				if (ctx == null) {
					ctx = initialTarget == obj ? initialContext : initialContext.makeChild(Reference.id(obj));
				}

				methods.add(method);
				contexts.add(ctx);
			}
		}

		if (methods.size() > 0) {
			IMethodCollection collection = new MethodCollection(methods);
			IUnbakedContext<IMethodCollection> context = null;
			IContext<IMethodCollection> baked = new Context<IMethodCollection>(null, collection, initialBaked.getCostHandler(), emptyReference);
			for (IMethod method : getMethods(baked)) {
				if (context == null) {
					context = new UnbakedContext<IMethodCollection>(Reference.id(collection), initialBaked.getCostHandler(), emptyReference);
				}

				methods.add(method);
				contexts.add(context);
			}
		}

		return new Tuple<List<IMethod<?>>, List<IUnbakedContext<?>>>(methods, contexts);
	}

	@SuppressWarnings("unchecked")
	public void loadAsm(ASMDataTable asmDataTable) {
		for (ASMDataTable.ASMData asmData : asmDataTable.getAll(IMethod.Inject.class.getName())) {
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
				IMethod instance = asmClass.asSubclass(IMethod.class).newInstance();

				Class<?> target = Class.forName(((Type) info.get("value")).getClassName());
				registerMethod(target, instance);
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
