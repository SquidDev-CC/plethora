package org.squiddev.plethora.integration.mcmultipart;

import com.google.common.collect.Maps;
import mcmultipart.MCMultiPartMod;
import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.IMultipartContainer;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;

import javax.annotation.Nonnull;
import java.util.Map;

@IMetaProvider.Inject(value = IMultipart.class, modId = MCMultiPartMod.MODID)
public class MetaMultipart extends BasicMetaProvider<IMultipart> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull IMultipart part) {
		return getBasicMeta(part);
	}

	public static Map<Object, Object> getBasicMeta(@Nonnull IMultipart part) {
		Map<Object, Object> out = Maps.newHashMap();

		IMultipartContainer container = part.getContainer();
		if (container != null) out.put("id", container.getPartID(part).toString());

		out.put("name", part.getType().toString());

		return out;
	}
}
