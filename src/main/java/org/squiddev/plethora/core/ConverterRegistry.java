package org.squiddev.plethora.core;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Sets;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import org.objectweb.asm.Type;
import org.squiddev.plethora.api.converter.IConverter;
import org.squiddev.plethora.api.converter.IConverterRegistry;
import org.squiddev.plethora.api.reference.IReference;
import org.squiddev.plethora.core.collections.ClassIteratorIterable;
import org.squiddev.plethora.utils.DebugLogger;
import org.squiddev.plethora.utils.Helpers;

import javax.annotation.Nonnull;
import java.util.*;

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
	public Iterable<Object> convertAll(@Nonnull final Object input) {
		Preconditions.checkNotNull(input, "input cannot be null");

		return new Iterable<Object>() {
			@Nonnull
			@Override
			public Iterator<Object> iterator() {
				return new ConverterIterator(input);
			}
		};
	}

	@SuppressWarnings("unchecked")
	public void extendConverted(@Nonnull List<String> keys, @Nonnull List<Object> values, @Nonnull List<Object> references, int startPoint) {
		Preconditions.checkNotNull(keys, "keys cannot be null");
		Preconditions.checkNotNull(values, "values cannot be null");
		Preconditions.checkNotNull(references, "references in cannot be null");

		if (keys.size() != values.size()) throw new IllegalStateException("lists must be of the same size");
		if (keys.size() != references.size()) throw new IllegalStateException("lists must be of the same size");

		Set<Object> allConverted = Sets.newHashSet(values);

		for (int i = startPoint; i < values.size(); i++) {
			Object target = values.get(i);

			Class<?> initial = target.getClass();
			for (Class<?> klass : new ClassIteratorIterable(initial)) {
				for (IConverter<?, ?> converter : converters.get(klass)) {
					Object converted = ((IConverter<Object, Object>) converter).convert(target);
					if (converted != null && allConverted.add(converted)) {
						keys.add(keys.get(i));
						values.add(converted);

						boolean isConstant = converter.isConstant();
						if (isConstant) {
							Object reference = references.get(i);
							if (reference instanceof ConverterReference) {
								isConstant = false;
							} else if (reference instanceof IReference) {
								isConstant = ((IReference) reference).isConstant();
							}
						}


						references.add(isConstant ? converted : new ConverterReference(i, klass, converter));
					}
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void extendConverted(@Nonnull List<String> keys, @Nonnull List<Object> values, int startPoint) {
		Preconditions.checkNotNull(keys, "keys cannot be null");
		Preconditions.checkNotNull(values, "values cannot be null");

		if (keys.size() != values.size()) throw new IllegalStateException("lists must be of the same size");

		Set<Object> allConverted = Sets.newHashSet(values);

		for (int i = startPoint; i < values.size(); i++) {
			Object target = values.get(i);

			Class<?> initial = target.getClass();
			for (Class<?> klass : new ClassIteratorIterable(initial)) {
				for (IConverter<?, ?> converter : converters.get(klass)) {
					Object converted = ((IConverter<Object, Object>) converter).convert(target);
					if (converted != null && allConverted.add(converted)) {
						keys.add(keys.get(i));
						values.add(converted);
					}
				}
			}
		}
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

	private class ConverterIterator implements Iterator<Object> {
		private final Set<Object> allConverted = new HashSet<Object>();
		private final Queue<Object> queue = new ArrayDeque<Object>();

		ConverterIterator(Object input) {
			allConverted.add(input);
			queue.offer(input);
		}

		@Override
		public boolean hasNext() {
			return queue.size() > 0;
		}

		@Override
		@SuppressWarnings("unchecked")
		public Object next() {
			Object next = queue.remove();

			Class<?> initial = next.getClass();
			for (Class<?> klass : new ClassIteratorIterable(initial)) {
				for (IConverter<?, ?> converter : converters.get(klass)) {
					Object converted = ((IConverter<Object, Object>) converter).convert(next);
					if (converted != null && allConverted.add(converted)) {
						queue.offer(converted);
					}
				}
			}

			return next;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("remove");
		}
	}
}
