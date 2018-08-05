package org.squiddev.plethora.core.docdump;

import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.api.meta.NamespacedMetaProvider;

import javax.annotation.Nonnull;

public class DocumentedMetaProvider extends DocumentedItem<IMetaProvider<?>> {
	private final Class<?> target;

	public DocumentedMetaProvider(@Nonnull Class<?> target, @Nonnull IMetaProvider<?> provider) {
		super(provider, getName(provider), getName(provider), provider.getDescription());
		this.target = target;
	}

	/**
	 * The class this meta provider targets
	 *
	 * @return This meta provider's target
	 */
	@Nonnull
	public Class<?> getTarget() {
		return target;
	}

	private static String getName(IMetaProvider provider) {
		return provider instanceof NamespacedMetaProvider
			? getName(((NamespacedMetaProvider) provider).getDelegate())
			: provider.getClass().getName();
	}
}
