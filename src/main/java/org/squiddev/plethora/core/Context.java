package org.squiddev.plethora.core;

import com.google.common.base.Preconditions;
import dan200.computercraft.api.lua.ILuaObject;
import org.squiddev.plethora.api.method.ContextKeys;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.api.reference.IReference;
import org.squiddev.plethora.api.reference.Reference;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;

public final class Context<T> extends PartialContext<T> implements IContext<T> {
	private final UnbakedContext<T> parent;

	Context(@Nonnull UnbakedContext<T> parent, @Nonnull Object[] context, @Nonnull IModuleContainer modules) {
		super(parent.target, parent.keys, context, parent.handler, modules);
		this.parent = parent;
	}

	@Override
	@SuppressWarnings("unchecked")
	Context<?> withIndex(int index) {
		return index == target ? this : new Context(parent.withIndex(index), values, modules);
	}

	@Nonnull
	@Override
	public <U> Context<U> makeChild(U target, @Nonnull IReference<U> targetReference) {
		Preconditions.checkNotNull(target, "target cannot be null");
		Preconditions.checkNotNull(targetReference, "targetReference cannot be null");

		ArrayList<String> keys = new ArrayList<String>(this.keys.length + 1);
		ArrayList<Object> references = new ArrayList<Object>(this.parent.references.length + 1);
		ArrayList<Object> values = new ArrayList<Object>(this.values.length + 1);

		Collections.addAll(keys, this.keys);
		Collections.addAll(references, this.parent.references);
		Collections.addAll(values, this.values);

		for (int i = keys.size() - 1; i >= 0; i--) {
			if (!ContextKeys.TARGET.equals(keys.get(i))) continue;
			keys.set(i, ContextKeys.GENERIC);
		}

		// Add the new target and convert it.
		keys.add(ContextKeys.TARGET);
		references.add(targetReference);
		values.add(target);
		ConverterRegistry.instance.extendConverted(keys, values, references, this.values.length);

		return new Context<U>(
			new UnbakedContext<U>(this.keys.length, keys.toArray(new String[keys.size()]), references.toArray(),
				handler, parent.modules, parent.executor),
			values.toArray(), modules
		);
	}

	@Nonnull
	@Override
	public <U extends IReference<U>> Context<U> makeChild(@Nonnull U target) {
		return makeChild(target, target);
	}

	@Nonnull
	@Override
	public <U> Context<U> makeChildId(@Nonnull U target) {
		return makeChild(target, Reference.id(target));
	}

	@Nonnull
	@Override
	public UnbakedContext<T> unbake() {
		return parent;
	}

	@Nonnull
	@Override
	public ILuaObject getObject() {
		return new MethodWrapperLuaObject(MethodRegistry.instance.getMethodsPaired(this));
	}
}
