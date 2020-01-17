package org.squiddev.plethora.core;

import com.google.common.base.Strings;
import com.google.common.reflect.TypeToken;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.converter.IConverter;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.api.method.IMethod;
import org.squiddev.plethora.api.method.wrapper.ArgumentType;
import org.squiddev.plethora.api.transfer.ITransferProvider;
import org.squiddev.plethora.core.wrapper.ArgumentTypeRegistry;
import org.squiddev.plethora.utils.Helpers;

import java.lang.reflect.*;
import java.util.Map;
import java.util.function.Supplier;

import static org.squiddev.plethora.core.PlethoraCore.LOG;

final class Registry {
	private static final Type TRANSFER_IN = ITransferProvider.class.getTypeParameters()[0];
	private static final Type CONVERTER_IN = IConverter.class.getTypeParameters()[0];
	private static final Type METHOD_IN = IMethod.class.getTypeParameters()[0];
	private static final Type META_IN = IMetaProvider.class.getTypeParameters()[0];
	private static final Type ARGUMENT_TYPE_IN = ArgumentType.class.getTypeParameters()[0];

	private Registry() {
	}

	static void register(ASMDataTable asmDataTable) {
		boolean ok = true;
		for (ASMDataTable.ASMData asmData : asmDataTable.getAll(Injects.class.getName())) {
			String name = asmData.getClassName();

			// First extract the mod id and ensure we're allowed to register it.
			Map<String, Object> info = asmData.getAnnotationInfo();
			String modId = (String) info.get("value");
			if (!Strings.isNullOrEmpty(modId) && !Helpers.modLoaded(modId)) {
				LOG.debug("Skipping " + name + " as " + modId + " is not loaded or is blacklisted");
				continue;
			}

			try {
				LOG.debug("Injecting " + name);

				if (Helpers.blacklisted(ConfigCore.Blacklist.blacklistProviders, name)) {
					LOG.debug("Ignoring " + name + " as it has been blacklisted");
					continue;
				}

				Result result = register(Class.forName(name));
				if (result == Result.PASS) LOG.warn("@Injects class {} has no usable fields or interfaces", name);
				if (result == Result.ERROR) ok = false;
			} catch (Exception | LinkageError e) {
				LOG.error("@Injects class {} failed to load", name, e);
				ok = false;
			}
		}

		if (!ok && ConfigCore.Testing.strict) {
			throw new IllegalStateException("Errors occurred when processing @Injects annotations. See the log above for more details.");
		}
	}

	static Result register(Class<?> klass) {
		// Skip blacklisted classes
		String name = klass.getName();
		if (Helpers.blacklisted(ConfigCore.Blacklist.blacklistProviders, name)) {
			LOG.debug("Ignoring " + name + " as it has been blacklisted");
			return Result.OK;
		}

		// Verify this class is "public final"
		int modifiers = klass.getModifiers();
		if (!Modifier.isPublic(modifiers)) {
			LOG.error("@Injects class {} should be public final, but is {}", name, Modifier.toString(modifiers));
			return Result.ERROR;
		}

		if (!Modifier.isFinal(modifiers)) {
			LOG.warn("@Injects class {} should be public final, but is only {}", name, Modifier.toString(modifiers));
		}

		Result result = registerInstance(name, klass, klass, klass, () -> {
			Object value;
			try {
				value = klass.newInstance();
			} catch (ReflectiveOperationException e) {
				LOG.error("@Injects class {} could not be instantiated", name, e);
				return null;
			}

			LOG.debug("Registering instance of @Injects class {}", name);
			return value;
		});

		for (Field field : klass.getDeclaredFields()) result = result.plus(register(field));

		return result;
	}

	private static Result register(Field field) {
		// Skip blacklisted fields.
		String name = field.getDeclaringClass().getName() + "." + field.getName();
		if (Helpers.blacklisted(ConfigCore.Blacklist.blacklistProviders, name)) {
			LOG.debug("Ignoring " + name + " as it has been blacklisted");
			return Result.OK;
		}

		return registerInstance(name, field, field.getType(), field.getGenericType(), () -> {
			// Verify this is a "public static final" field. We do this inside the getter as it means we don't warn on
			// fields which don't look like ours.
			int modifiers = field.getModifiers();
			if (!Modifier.isPublic(modifiers) || !Modifier.isStatic(modifiers)) {
				LOG.error("@Injects field {} should be public static final, but is {}", name, Modifier.toString(modifiers));
				return null;
			}

			if (!Modifier.isFinal(modifiers)) {
				LOG.warn("@Injects field {} should be public static final, but is {}", name, Modifier.toString(modifiers));
			}

			Object value;
			try {
				value = field.get(null);
			} catch (ReflectiveOperationException e) {
				LOG.error("@Injects field {}'s value could not be fetched", name, e);
				return null;
			}

			LOG.debug("Registering value of @Injects field {}", name);
			return value;
		});
	}

	@SuppressWarnings({ "unchecked", "UnstableApiUsage" })
	private static Result registerInstance(String name, AnnotatedElement element, Class<?> rawType, Type type, Supplier<?> instanceGetter) {
		Object instance = null;

		// Register ITransferProviders
		if (ITransferProvider.class.isAssignableFrom(rawType)) {
			Type typeParameter = TypeToken.of(type).resolveType(TRANSFER_IN).getType();
			Class<?> target = getRawType(name, typeParameter, typeParameter);
			if (target == null) return Result.ERROR;

			if (instance == null) instance = instanceGetter.get();
			if (instance == null) return Result.ERROR;

			ITransferProvider provider = (ITransferProvider) instance;
			if (provider.primary()) TransferRegistry.instance.registerPrimary(target, provider);
			if (provider.secondary()) TransferRegistry.instance.registerSecondary(target, provider);

			if (!provider.primary() && !provider.secondary()) {
				LOG.warn("@Injects {} is neither a primary nor secondary ITransferProvider", name);
			}
		}

		// Register IConverters
		if (IConverter.class.isAssignableFrom(rawType)) {
			Type typeParameter = TypeToken.of(type).resolveType(CONVERTER_IN).getType();
			Class<?> klass = getRawType(name, typeParameter, typeParameter);
			if (klass == null) return Result.ERROR;

			if (instance == null) instance = instanceGetter.get();
			if (instance == null) return Result.ERROR;

			ConverterRegistry.instance.registerConverter(klass, (IConverter) instance);
		}

		// Register IMethod
		if (IMethod.class.isAssignableFrom(rawType)) {
			Type typeParameter = TypeToken.of(type).resolveType(METHOD_IN).getType();
			Class<?> klass = getRawType(name, typeParameter, typeParameter);
			if (klass == null) return Result.ERROR;

			if (instance == null) instance = instanceGetter.get();
			if (instance == null) return Result.ERROR;

			MethodRegistry.instance.registerMethod(klass, (IMethod) instance);
		}

		// Register IMetaProvider
		if (IMetaProvider.class.isAssignableFrom(rawType)) {
			Type typeParameter = TypeToken.of(type).resolveType(META_IN).getType();
			Class<?> klass = getRawType(name, typeParameter, typeParameter);
			if (klass == null) return Result.ERROR;

			if (instance == null) instance = instanceGetter.get();
			if (instance == null) return Result.ERROR;

			MetaRegistry.instance.registerMetaProvider(klass, (IMetaProvider) instance, name);
		}

		// Register ArgumentType
		if (ArgumentType.class.isAssignableFrom(rawType)) {
			Type typeParameter = TypeToken.of(type).resolveType(ARGUMENT_TYPE_IN).getType();
			Class<?> klass = getRawType(name, typeParameter, typeParameter);
			if (klass == null) return Result.ERROR;

			if (!(element instanceof Field)) {
				LOG.error("@Injects {} must be a field in order to be injected as an ArgumentType", name);
				return Result.ERROR;
			}

			// Just get the instance to verify we're all OK.
			if (instance == null) instance = instanceGetter.get();
			if (instance == null) return Result.ERROR;

			if (!ArgumentTypeRegistry.register(klass, (Field) element)) return Result.ERROR;
		}

		return instance == null ? Result.PASS : Result.OK;
	}

	private static Class<?> getRawType(String name, Type root, Type underlying) {
		while (true) {
			if (underlying instanceof Class<?>) return (Class<?>) underlying;

			if (underlying instanceof ParameterizedType) {
				ParameterizedType type = (ParameterizedType) underlying;
				for (Type arg : type.getActualTypeArguments()) {
					if (arg instanceof WildcardType) continue;
					if (arg instanceof TypeVariable && ((TypeVariable) arg).getName().startsWith("capture#")) continue;

					LOG.error("@Injects {} has generic type {} with non-wildcard argument {}", name, root, arg);
					return null;
				}

				// Continue to extract from this child
				underlying = type.getRawType();
			} else {
				LOG.error("@Injects {} has unknown generic type {}", name, underlying);
				return null;
			}
		}
	}

	public enum Result {
		OK,
		ERROR,
		PASS;

		public Result plus(Result other) {
			if (this == ERROR || other == ERROR) return ERROR;
			if (this == OK || other == OK) return OK;
			return PASS;
		}
	}
}
