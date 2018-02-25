package org.squiddev.plethora.core;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import dan200.computercraft.api.lua.ILuaObject;
import org.squiddev.plethora.api.IAttachable;
import org.squiddev.plethora.api.method.*;
import org.squiddev.plethora.api.module.BasicModuleContainer;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.api.reference.IReference;
import org.squiddev.plethora.core.capabilities.DefaultCostHandler;
import org.squiddev.plethora.core.executor.DefaultExecutor;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Concrete implementation of {@link IContextBuilder} and {@link IContextFactory}.
 */
public class ContextFactory<T> implements IContextFactory<T>, IContextBuilder {
	private final List<String> targetKeys;
	private final List<Object> targetValues;
	private final List<Object> targetReferences;

	private final List<String> keys = new ArrayList<String>();
	private final List<Object> values = new ArrayList<Object>();
	private final List<Object> references = new ArrayList<Object>();

	private final List<IAttachable> attachments = Lists.newArrayList();

	private IResultExecutor executor = DefaultExecutor.INSTANCE;
	private ICostHandler handler = new DefaultCostHandler();
	private IModuleContainer modules = BasicModuleContainer.EMPTY;
	private IReference<IModuleContainer> moduleReference = BasicModuleContainer.EMPTY_REF;

	private String[] combinedKeys;
	private Object[] combinedValues;
	private Object[] combinedReferences;

	private ContextFactory(T target, IReference<T> targetReference) {
		targetKeys = new ArrayList<String>(1);
		targetValues = new ArrayList<Object>(1);
		targetReferences = new ArrayList<Object>(1);

		targetKeys.add(ContextKeys.TARGET);
		targetValues.add(target);
		targetReferences.add(targetReference);

		ConverterRegistry.instance.extendConverted(targetKeys, targetValues, targetReferences, 0);
	}

	public static <T> ContextFactory<T> of(T target, IReference<T> targetReference) {
		return new ContextFactory<T>(target, targetReference);
	}

	public static <T extends IReference<T>> ContextFactory<T> of(T target) {
		return new ContextFactory<T>(target, target);
	}

	private void dirty() {
		combinedKeys = null;
		combinedValues = null;
		combinedReferences = null;
	}

	@Nonnull
	@Override
	public <U> ContextFactory<T> addContext(@Nonnull String key, @Nonnull U baked, @Nonnull IReference<U> reference) {
		Preconditions.checkNotNull(key, "key cannot be null");
		Preconditions.checkNotNull(reference, "reference cannot be null");
		Preconditions.checkNotNull(baked, "baked cannot be null");

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
		Preconditions.checkNotNull(key, "key cannot be null");
		Preconditions.checkNotNull(object, "object cannot be null");

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
		Preconditions.checkNotNull(handler, "cost handler cannot be null");
		this.handler = handler;

		dirty();
		return this;
	}

	@Nonnull
	@Override
	public ContextFactory<T> withExecutor(@Nonnull IResultExecutor executor) {
		Preconditions.checkNotNull(executor, "executor cannot be null");
		this.executor = executor;

		dirty();
		return this;
	}

	@Nonnull
	@Override
	public ContextFactory<T> withModules(@Nonnull IModuleContainer modules, @Nonnull IReference<IModuleContainer> reference) {
		Preconditions.checkNotNull(modules, "modules cannot be null");
		Preconditions.checkNotNull(reference, "reference cannot be null");
		this.modules = modules;
		this.moduleReference = reference;

		dirty();
		return this;
	}

	private void setup() {
		if (combinedKeys == null) {
			int size = keys.size() + targetKeys.size();

			combinedKeys = new String[size];
			combinedValues = new Object[size];
			combinedReferences = new Object[size];

			int j = 0;
			for (int i = 0; i < keys.size(); i++, j++) {
				combinedKeys[j] = keys.get(i);
				combinedValues[j] = values.get(i);
				combinedReferences[j] = references.get(i);
			}

			for (int i = 0; i < targetKeys.size(); i++, j++) {
				combinedKeys[j] = targetKeys.get(i);
				combinedValues[j] = targetValues.get(i);
				combinedReferences[j] = targetReferences.get(i);
			}
		}
	}

	@Nonnull
	@Override
	public Context<T> getBaked() {
		setup();
		return new Context<T>(getUnbaked(), combinedValues, modules);
	}

	@Nonnull
	@Override
	public UnbakedContext<T> getUnbaked() {
		setup();
		return new UnbakedContext<T>(keys.size(), combinedKeys, combinedReferences, handler, moduleReference, executor);
	}

	@Nonnull
	@Override
	public ILuaObject getObject() {
		return getBaked().getObject();
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
