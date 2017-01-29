package org.squiddev.plethora.api.method;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.squiddev.plethora.api.IAttachable;
import org.squiddev.plethora.api.reference.IReference;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

/**
 * Concrete implementation of {@link IContextBuilder}.
 */
public class BasicContextBuilder implements IContextBuilder {
	private final List<Object> objects = Lists.newArrayList();
	private final List<IReference<?>> references = Lists.newArrayList();
	private final List<IAttachable> attachments = Lists.newArrayList();

	@Override
	public <T> void addContext(@Nonnull T baked, @Nonnull IReference<T> reference) {
		Preconditions.checkNotNull(reference, "reference cannot be null");
		Preconditions.checkNotNull(baked, "baked cannot be null");

		objects.add(baked);
		references.add(reference);
	}

	@Override
	public <T extends IReference<T>> void addContext(@Nonnull T object) {
		Preconditions.checkNotNull(object, "object cannot be null");

		objects.add(object);
		references.add(object);
	}

	@Override
	public void addAttachable(@Nonnull IAttachable attachable) {
		attachments.add(attachable);
	}

	@Nonnull
	public List<Object> getObjects() {
		return Collections.unmodifiableList(objects);
	}

	@Nonnull
	public Object[] getObjectsArray() {
		Object[] out = new Object[objects.size()];
		objects.toArray(out);
		return out;
	}

	@Nonnull
	public List<IReference<?>> getReferences() {
		return Collections.unmodifiableList(references);
	}

	@Nonnull
	public IReference<?>[] getReferenceArray() {
		IReference<?>[] out = new IReference<?>[references.size()];
		references.toArray(out);
		return out;
	}

	public List<IAttachable> getAttachments() {
		return Collections.unmodifiableList(attachments);
	}
}
