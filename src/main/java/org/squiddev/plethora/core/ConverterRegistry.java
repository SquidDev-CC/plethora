package org.squiddev.plethora.core;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.*;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import org.objectweb.asm.Type;
import org.squiddev.plethora.api.converter.IConverter;
import org.squiddev.plethora.api.converter.IConverterRegistry;
import org.squiddev.plethora.core.collections.ClassIteratorIterable;
import org.squiddev.plethora.utils.DebugLogger;
import org.squiddev.plethora.utils.Helpers;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class ConverterRegistry implements IConverterRegistry {
	public static final ConverterRegistry instance = new ConverterRegistry();

	private final Multimap<Class<?>, IConverter<?, ?>> converters = MultimapBuilder.hashKeys().hashSetValues().build();

	@Override
	public <TIn, TOut> void registerConverter(@Nonnull Class<TIn> source, @Nonnull IConverter<TIn, TOut> converter) {
		Preconditions.checkNotNull(source, "source cannot be null");
		Preconditions.checkNotNull(converter, "converter cannot be null");

		converters.put(source, converter);
	}

	@Nonnull
	@Override
	@SuppressWarnings("unchecked")
	public List<?> convertAll(@Nonnull Object in) {
		Preconditions.checkNotNull(in, "in cannot be null");

		List<Object> result = Lists.newArrayList();
		Set<Object> allConverted = Sets.newHashSet();

		Queue<Object> toConvert = Queues.newArrayDeque();
		toConvert.add(in);
		allConverted.add(in);

		while (toConvert.size() > 0) {
			Object target = toConvert.remove();
			result.add(target);

			Class<?> initial = target.getClass();
			for (Class<?> klass : new ClassIteratorIterable(initial)) {
				for (IConverter<?, ?> converter : converters.get(klass)) {
					Object converted = ((IConverter<Object, Object>) converter).convert(target);
					if (converted != null && allConverted.add(converted)) {
						toConvert.add(converted);
					}
				}
			}
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	public void loadAsm(ASMDataTable asmDataTable) {
		for (ASMDataTable.ASMData asmData : asmDataTable.getAll(IConverter.Inject.class.getName())) {
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
				IConverter instance = asmClass.asSubclass(IConverter.class).newInstance();

				Class<?> target = Class.forName(((Type) info.get("value")).getClassName());
				Helpers.assertTarget(asmClass, target, IConverter.class);
				registerConverter(target, instance);
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
