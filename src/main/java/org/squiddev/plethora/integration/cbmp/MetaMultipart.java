package org.squiddev.plethora.integration.cbmp;

import codechicken.multipart.TMultiPart;
import com.google.common.collect.Maps;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;

import javax.annotation.Nonnull;
import java.util.Map;

@IMetaProvider.Inject(value = TMultiPart.class, modId = "forgemultipartcbe")
public class MetaMultipart extends BasicMetaProvider<TMultiPart> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull TMultiPart part) {
		return getBasicMeta(part);
	}

	public static Map<Object, Object> getBasicMeta(@Nonnull TMultiPart part) {
		Map<Object, Object> out = Maps.newHashMap();
		out.put("name", part.getType());
		return out;
	}
}
