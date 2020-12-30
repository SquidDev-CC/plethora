package org.squiddev.plethora.core;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
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

	final List<RegisteredMethod<?>> all = new ArrayList<>();
	final Multimap<Class<?>, RegisteredMethod<?>> providers = MultimapBuilder.hashKeys().arrayListValues().build();

	public void registerMethod(@Nonnull RegisteredMethod<?> entry) {
		Objects.requireNonNull(entry, "target cannot be null");

		if (entry.target() == Object.class && entry.method().has(IConverterExcludeMethod.class)) {
			PlethoraCore.LOG.warn(
				"You're registering a method (" + entry.name() + ") targeting the base class (Object). Converters will " +
					"probably mask the original object: it is recommended that you implement IConverterExcludeMethod to avoid this."
			);
		}

		all.add(entry);
	}

	public void build() {
		providers.clear();
		for (RegisteredMethod<?> entry : all) {
			if (entry.enabled()) {
				entry.build();
				providers.put(entry.target(), entry);
			}
		}
	}

	@Nonnull
	@SuppressWarnings("unchecked")
	public <T> List<RegisteredMethod<T>> getMethods(@Nonnull IPartialContext<T> context) {
		Objects.requireNonNull(context, "context cannot be null");

		List<RegisteredMethod<T>> methods = Lists.newArrayList();

		for (Class<?> klass : new ClassIteratorIterable(context.getTarget().getClass())) {
			for (RegisteredMethod entry : providers.get(klass)) {
				if (entry.method().canApply(context)) methods.add(entry);
			}
		}

		return Collections.unmodifiableList(methods);
	}

	@Nonnull
	@Override
	public ICostHandler getCostHandler(@Nonnull ICapabilityProvider object, @Nullable EnumFacing side) {
		Objects.requireNonNull(object, "object cannot be null");
		ICostHandler handler = object.getCapability(Constants.COST_HANDLER_CAPABILITY, side);
		return handler != null ? handler : DefaultCostHandler.get(object);
	}

	public Pair<List<RegisteredMethod<?>>, List<UnbakedContext<?>>> getMethodsPaired(Context<?> builder) {
		ArrayList<RegisteredMethod<?>> methods = Lists.newArrayList();
		ArrayList<UnbakedContext<?>> contexts = Lists.newArrayList();
		HashMap<String, Integer> methodLookup = new HashMap<>();

		String[] keys = builder.keys;
		Object[] values = builder.values;

		for (int i = values.length - 1; i >= 0; i--) {
			if (!ContextKeys.TARGET.equals(keys[i])) continue;

			UnbakedContext<?> unbaked = null;
			for (RegisteredMethod<?> entry : getMethods(builder.withIndex(i))) {
				IMethod<?> method = entry.method();
				// Skip IConverterExclude methods
				if (i != builder.target && method.has(IConverterExcludeMethod.class)) continue;

				if (unbaked == null) unbaked = builder.unbake().withIndex(i);

				Integer existing = methodLookup.get(method.getName());
				if (existing != null) {
					int index = existing;
					if (method.getPriority() > methods.get(index).method().getPriority()) {
						methods.set(index, entry);
						contexts.set(index, unbaked);
					}
				} else {
					methods.add(entry);
					contexts.add(unbaked);
					methodLookup.put(method.getName(), methods.size() - 1);
				}
			}
		}

		if (!methods.isEmpty()) {
			IMethodCollection collection = new MethodCollection(methods);

			Context<IMethodCollection> baked = builder.makeChildId(collection);
			for (RegisteredMethod<?> entry : getMethods(baked)) {
				IMethod<?> method = entry.method();
				Integer existing = methodLookup.get(method.getName());
				if (existing != null) {
					int index = existing;
					if (method.getPriority() > methods.get(index).method().getPriority()) {
						methods.set(index, entry);
						contexts.set(index, baked.unbake());
					}
				} else {
					methods.add(entry);
					contexts.add(baked.unbake());
					methodLookup.put(method.getName(), methods.size() - 1);
				}
			}
		}

		return Pair.of(methods, contexts);
	}
}
