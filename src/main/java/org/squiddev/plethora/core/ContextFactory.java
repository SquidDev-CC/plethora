package org.squiddev.plethora.core;

import com.google.common.collect.Lists;
import org.squiddev.plethora.api.IAttachable;
import org.squiddev.plethora.api.method.*;
import org.squiddev.plethora.api.module.BasicModuleContainer;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.api.reference.IReference;
import org.squiddev.plethora.core.capabilities.EmptyCostHandler;
import org.squiddev.plethora.core.executor.NeverExecutor;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Concrete implementation of {@link IContextBuilder} and {@link IContextFactory}.
 */
public final class ContextFactory<T> implements IContextFactory<T>, IContextBuilder {
	private final Object targetValue;
	private final Object targetReference;

	private final List<String> keys = new ArrayList<>();
	private final List<Object> values = new ArrayList<>();
	private final List<Object> references = new ArrayList<>();

	private final List<IAttachable> attachments = Lists.newArrayList();

	private IResultExecutor executor = NeverExecutor.INSTANCE;
	private ICostHandler handler = EmptyCostHandler.INSTANCE;
	private IModuleContainer modules = BasicModuleContainer.EMPTY;
	private IReference<IModuleContainer> moduleReference = BasicModuleContainer.EMPTY_REF;

	private String[] combinedKeys;
	private Object[] combinedValues;
	private Object[] combinedReferences;

	private ContextFactory(T target, IReference<T> targetReference) {
		targetValue = target;
		this.targetReference = targetReference;
	}

	public static <T> ContextFactory<T> of(T target, IReference<T> targetReference) {
		return new ContextFactory<>(target, targetReference);
	}

	public static <T extends IReference<T>> ContextFactory<T> of(T target) {
		return new ContextFactory<>(target, target);
	}

	private void dirty() {
		combinedKeys = null;
		combinedValues = null;
		combinedReferences = null;
	}

	@Nonnull
	@Override
	public <U> ContextFactory<T> addContext(@Nonnull String key, @Nonnull U baked, @Nonnull IReference<U> reference) {
		Objects.requireNonNull(key, "key cannot be null");
		Objects.requireNonNull(reference, "reference cannot be null");
		Objects.requireNonNull(baked, "baked cannot be null");

		keys.add(key);
		values.add(baked);
		references.add(reference);
		ConverterRegistry.instance.extendConverted(keys, values, references, values.size() - 1);

		dirty();
		return this;
	}

	@Nonnull
	@Override
	public <U extends IReference<U>> ContextFactory<T> addContext(@Nonnull String key, @Nonnull U object) {
		Objects.requireNonNull(key, "key cannot be null");
		Objects.requireNonNull(object, "object cannot be null");

		keys.add(key);
		values.add(object);
		references.add(object);
		ConverterRegistry.instance.extendConverted(keys, values, references, values.size() - 1);

		dirty();
		return this;
	}

	@Nonnull
	@Override
	public ContextFactory<T> withCostHandler(@Nonnull ICostHandler handler) {
		Objects.requireNonNull(handler, "cost handler cannot be null");
		this.handler = handler;

		dirty();
		return this;
	}

	@Nonnull
	@Override
	public ContextFactory<T> withExecutor(@Nonnull IResultExecutor executor) {
		Objects.requireNonNull(executor, "executor cannot be null");
		this.executor = executor;

		dirty();
		return this;
	}

	@Nonnull
	@Override
	public ContextFactory<T> withModules(@Nonnull IModuleContainer modules, @Nonnull IReference<IModuleContainer> reference) {
		Objects.requireNonNull(modules, "modules cannot be null");
		Objects.requireNonNull(reference, "reference cannot be null");
		this.modules = modules;
		moduleReference = reference;

		dirty();
		return this;
	}

	private void setup() {
		if (combinedKeys == null) {
			List<String> combinedKeysList = Lists.newArrayListWithExpectedSize(keys.size() + 1);
			List<Object> combinedValuesList = Lists.newArrayListWithExpectedSize(keys.size() + 1);
			List<Object> combinedReferencesList = Lists.newArrayListWithExpectedSize(keys.size() + 1);

			combinedKeysList.addAll(keys);
			combinedValuesList.addAll(values);
			combinedReferencesList.addAll(references);

			combinedKeysList.add(ContextKeys.TARGET);
			combinedValuesList.add(targetValue);
			combinedReferencesList.add(targetReference);
			ConverterRegistry.instance.extendConverted(combinedKeysList, combinedValuesList, combinedReferencesList, combinedKeysList.size() - 1);

			combinedKeys = combinedKeysList.toArray(new String[0]);
			combinedValues = combinedValuesList.toArray();
			combinedReferences = combinedReferencesList.toArray();
		}
	}

	@Nonnull
	@Override
	public Context<T> getBaked() {
		setup();
		return new Context<>(getUnbaked(), combinedValues, modules);
	}

	@Nonnull
	@Override
	public UnbakedContext<T> getUnbaked() {
		setup();
		return new UnbakedContext<>(keys.size(), combinedKeys, combinedReferences, handler, moduleReference, executor);
	}

	@Nonnull
	@Override
	public ContextFactory<T> addAttachable(@Nonnull IAttachable attachable) {
		attachments.add(attachable);
		return this;
	}

	public List<IAttachable> getAttachments() {
		return Collections.unmodifiableList(attachments);
	}
}
