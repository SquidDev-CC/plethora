package org.squiddev.plethora.core;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import org.apache.commons.lang3.tuple.Pair;
import org.objectweb.asm.Type;
import org.squiddev.plethora.api.Constants;
import org.squiddev.plethora.api.method.*;
import org.squiddev.plethora.core.capabilities.DefaultCostHandler;
import org.squiddev.plethora.core.collections.ClassIteratorIterable;
import org.squiddev.plethora.utils.DebugLogger;
import org.squiddev.plethora.utils.Helpers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.util.*;

public final class MethodRegistry implements IMethodRegistry {
	public static final MethodRegistry instance = new MethodRegistry();

	final Multimap<Class<?>, IMethod<?>> providers = MultimapBuilder.hashKeys().arrayListValues().build();

	@Override
	public <T> void registerMethod(@Nonnull Class<T> target, @Nonnull IMethod<T> method) {
		Preconditions.checkNotNull(target, "target cannot be null");
		Preconditions.checkNotNull(method, "method cannot be null");

		String comment = method.getName();
		String doc = method.getDocString();
		if (doc != null) comment += ": " + doc;
		ConfigCore.configuration.get("baseCosts", method.getClass().getName(), 0, comment, 0, Integer.MAX_VALUE);

		providers.put(target, method);

		if (target == Object.class && !(method instanceof IConverterExcludeMethod)) {
			DebugLogger.warn(
				"You're registering a method (" + method + ") targeting the base class (Object). Converters will " +
					"probably mask the original object: it is recommended that you implement IConverterExcludeMethod to avoid this."
			);
		}

		if (ConfigCore.Testing.likeDocs && method.getDocString() == null) {
			String message = "Method " + method + " (" + method.getName() + ") has no documentation. This isn't a bug but you really should add one.";
			if (ConfigCore.Testing.strict) {
				throw new IllegalArgumentException(message);
			} else {
				DebugLogger.error(message);
			}
		}
	}

	@Nonnull
	@Override
	@SuppressWarnings("unchecked")
	public <T> List<IMethod<T>> getMethods(@Nonnull IPartialContext<T> context) {
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

		for (Class<?> klass : new ClassIteratorIterable(target)) {
			result.addAll(providers.get(klass));
		}

		return Collections.unmodifiableList(result);
	}

	@Nonnull
	@Override
	public ICostHandler getCostHandler(@Nonnull ICapabilityProvider object, @Nullable EnumFacing side) {
		Preconditions.checkNotNull(object, "object cannot be null");
		ICostHandler handler = object.getCapability(Constants.COST_HANDLER_CAPABILITY, side);
		return handler != null ? handler : DefaultCostHandler.get(object);
	}

	@Override
	public <T extends Annotation> void registerMethodBuilder(@Nonnull Class<T> klass, @Nonnull IMethodBuilder<T> builder) {
		Preconditions.checkNotNull(klass, "klass cannot be null");
		Preconditions.checkNotNull(builder, "builder cannot be null");

		MethodTypeBuilder.instance.addBuilder(klass, builder);
	}

	@Override
	public int getBaseMethodCost(IMethod method) {
		Property property = ConfigCore.baseCosts.get(method.getClass().getName());
		if (property == null) {
			DebugLogger.warn("Cannot find cost for " + method.getClass().getName() + ", this may have been registered incorrectly");
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
				if (i != builder.target && method instanceof IConverterExcludeMethod) {
					continue;
				}

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

		if (methods.size() > 0) {
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

	@SuppressWarnings("unchecked")
	public void loadAsm(ASMDataTable asmDataTable) {
		for (ASMDataTable.ASMData asmData : asmDataTable.getAll(IMethod.Inject.class.getName())) {
			String name = asmData.getClassName();
			try {
				if (Helpers.blacklisted(ConfigCore.Blacklist.blacklistProviders, name)) {
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
				Helpers.assertTarget(asmClass, target, IMethod.class);
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
