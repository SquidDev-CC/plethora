package org.squiddev.plethora.core;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.config.Property;
import org.apache.commons.lang3.tuple.Pair;
import org.squiddev.plethora.api.Constants;
import org.squiddev.plethora.api.method.*;
import org.squiddev.plethora.core.capabilities.DefaultCostHandler;
import org.squiddev.plethora.core.collections.ClassIteratorIterable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public final class MethodRegistry implements IMethodRegistry {
	public static final MethodRegistry instance = new MethodRegistry();

	final Multimap<Class<?>, IMethod<?>> providers = MultimapBuilder.hashKeys().arrayListValues().build();

	public <T> void registerMethod(@Nonnull Class<T> target, @Nonnull IMethod<T> method) {
		Objects.requireNonNull(target, "target cannot be null");
		Objects.requireNonNull(method, "method cannot be null");

		String comment = method.getName() + ": " + method.getDocString();

		String id = method.getId();
		if (id.indexOf('#') >= 0) {
			String oldId = id.replace('#', '$');
			int targetIdx = oldId.lastIndexOf('(');
			if (targetIdx >= 0) oldId = oldId.substring(0, targetIdx);
			ConfigCore.configuration.renameProperty("baseCosts", oldId, id);
		}
		ConfigCore.configuration.get("baseCosts", method.getId(), 0, comment, 0, Integer.MAX_VALUE);

		providers.put(target, method);

		if (target == Object.class && !(method instanceof IConverterExcludeMethod)) {
			PlethoraCore.LOG.warn(
				"You're registering a method (" + method + ") targeting the base class (Object). Converters will " +
					"probably mask the original object: it is recommended that you implement IConverterExcludeMethod to avoid this."
			);
		}
	}

	@Nonnull
	@Override
	@SuppressWarnings("unchecked")
	public <T> List<IMethod<T>> getMethods(@Nonnull IPartialContext<T> context) {
		Objects.requireNonNull(context, "context cannot be null");

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
		Objects.requireNonNull(target, "target cannot be null");

		List<IMethod<?>> result = Lists.newArrayList();

		for (Class<?> klass : new ClassIteratorIterable(target)) {
			result.addAll(providers.get(klass));
		}

		return Collections.unmodifiableList(result);
	}

	@Nonnull
	@Override
	public ICostHandler getCostHandler(@Nonnull ICapabilityProvider object, @Nullable EnumFacing side) {
		Objects.requireNonNull(object, "object cannot be null");
		ICostHandler handler = object.getCapability(Constants.COST_HANDLER_CAPABILITY, side);
		return handler != null ? handler : DefaultCostHandler.get(object);
	}

	@Override
	public int getBaseMethodCost(IMethod<?> method) {
		Property property = ConfigCore.baseCosts.get(method.getId());
		if (property == null) {
			PlethoraCore.LOG.warn("Cannot find cost for " + method.getId() + ", this may have been registered incorrectly");
			return 0;
		}

		return property.getInt();
	}

	public Pair<List<IMethod<?>>, List<UnbakedContext<?>>> getMethodsPaired(Context<?> builder) {
		ArrayList<IMethod<?>> methods = Lists.newArrayList();
		ArrayList<UnbakedContext<?>> contexts = Lists.newArrayList();
		HashMap<String, Integer> methodLookup = new HashMap<>();

		String[] keys = builder.keys;
		Object[] values = builder.values;

		for (int i = values.length - 1; i >= 0; i--) {
			if (!ContextKeys.TARGET.equals(keys[i])) continue;

			UnbakedContext<?> unbaked = null;
			for (IMethod method : getMethods(builder.withIndex(i))) {
				// Skip IConverterExclude methods
				if (i != builder.target && method instanceof IConverterExcludeMethod) continue;

				if (unbaked == null) unbaked = builder.unbake().withIndex(i);

				Integer existing = methodLookup.get(method.getName());
				if (existing != null) {
					int index = existing;
					if (method.getPriority() > methods.get(index).getPriority()) {
						methods.set(index, method);
						contexts.set(index, unbaked);
					}
				} else {
					methods.add(method);
					contexts.add(unbaked);
					methodLookup.put(method.getName(), methods.size() - 1);
				}
			}
		}

		if (!methods.isEmpty()) {
			IMethodCollection collection = new MethodCollection(methods);

			Context<IMethodCollection> baked = builder.makeChildId(collection);
			for (IMethod method : getMethods(baked)) {
				Integer existing = methodLookup.get(method.getName());
				if (existing != null) {
					int index = existing;
					if (method.getPriority() > methods.get(index).getPriority()) {
						methods.set(index, method);
						contexts.set(index, baked.unbake());
					}
				} else {
					methods.add(method);
					contexts.add(baked.unbake());
					methodLookup.put(method.getName(), methods.size() - 1);
				}
			}
		}

		return Pair.of(methods, contexts);
	}
}
