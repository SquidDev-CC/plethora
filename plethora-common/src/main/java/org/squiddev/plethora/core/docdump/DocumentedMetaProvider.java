package org.squiddev.plethora.core.docdump;

import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.core.MetaRegistry;

import javax.annotation.Nonnull;

class DocumentedMetaProvider extends DocumentedItem<IMetaProvider<?>> {
	private final Class<?> target;

	DocumentedMetaProvider(@Nonnull Class<?> target, @Nonnull IMetaProvider<?> provider) {
		super(provider, MetaRegistry.instance.getName(provider), MetaRegistry.instance.getName(provider), provider.getDescription());
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
}
