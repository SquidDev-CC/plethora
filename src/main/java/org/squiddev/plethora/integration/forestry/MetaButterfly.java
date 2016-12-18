package org.squiddev.plethora.integration.forestry;

import forestry.api.lepidopterology.IButterfly;
import forestry.core.config.Constants;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

@IMetaProvider.Inject(value = IButterfly.class, modId = Constants.MOD_ID, namespace = "butterfly")
public class MetaButterfly extends BasicMetaProvider<IButterfly> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull IButterfly butterfly) {
		if(butterfly.isAnalyzed()) {
			return Collections.<Object, Object>singletonMap("size", butterfly.getSize());
		} else {
			return Collections.emptyMap();
		}
	}
}
