package org.squiddev.plethora.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.*;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.IMethod;
import org.squiddev.plethora.api.method.IMethodRegistry;
import org.squiddev.plethora.api.method.Method;
import org.squiddev.plethora.utils.DebugLogger;

import java.util.*;

public final class MethodRegistry implements IMethodRegistry {
	public static final MethodRegistry instance = new MethodRegistry();

	private final Multimap<Class<?>, IMethod<?>> providers = MultimapBuilder.hashKeys().hashSetValues().build();

	@Override
	public <T> void registerMethod(Class<T> target, IMethod<T> method) {
		Preconditions.checkNotNull(target, "target cannot be null");
		Preconditions.checkNotNull(method, "provider cannot be null");

		HashSet<Class<?>> visited = Sets.newHashSet();
		Queue<Class<?>> toVisit = Queues.newArrayDeque();

		visited.add(target);
		toVisit.add(target);

		while (toVisit.size() > 0) {
			Class<?> klass = toVisit.poll();
			providers.put(klass, method);

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

	@SuppressWarnings("unchecked")
	@Override
	public <T> List<IMethod<T>> getMethods(IContext<T> context) {
		Preconditions.checkNotNull(context, "context cannot be null");

		List<IMethod<T>> methods = Lists.newArrayList();

		for (IMethod<?> genMethod : getMethods(context.getClass())) {
			final IMethod<T> method = (IMethod<T>) genMethod;
			if (method.canApply(context)) methods.add(method);
		}

		return methods;
	}

	@Override
	public Collection<IMethod<?>> getMethods(Class<?> target) {
		Preconditions.checkNotNull(target, "target cannot be null");
		return providers.get(target);
	}

	@Override
	public <T> IContext<T> getContext(T target, IContext<?> parent, Object... context) {
		Preconditions.checkNotNull(target, "target cannot be null");
		Preconditions.checkNotNull(parent, "parent cannot be null");
		Preconditions.checkNotNull(context, "context cannot be null");

		Object[] wholeContext = Arrays.copyOf(context, context.length + 1);
		wholeContext[context.length] = parent;

		return new Context<T>(target, wholeContext);
	}

	@SuppressWarnings("unchecked")
	public void loadAsm(ASMDataTable asmDataTable) {
		for (ASMDataTable.ASMData asmData : asmDataTable.getAll(Method.class.getCanonicalName())) {
			try {
				Class<?> asmClass = Class.forName(asmData.getClassName());
				Map<String, Object> info = asmData.getAnnotationInfo();

				IMethod instance = asmClass.asSubclass(IMethod.class).newInstance();

				Class<?> target = (Class<?>) info.get("value");
				registerMethod(target, instance);
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
