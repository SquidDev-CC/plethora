package org.squiddev.plethora.integration.mcmultipart;

import mcmultipart.MCMultiPart;
import mcmultipart.api.container.IPartInfo;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.integration.vanilla.meta.MetaBlock;

import javax.annotation.Nonnull;
import java.util.Map;

@Injects(MCMultiPart.MODID)
public class MetaMultipart extends BasicMetaProvider<IPartInfo> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull IPartInfo part) {
		return getBasicMeta(part);
	}

	public static Map<Object, Object> getBasicMeta(@Nonnull IPartInfo part) {
		return MetaBlock.getBasicMeta(part.getPart().getBlock());
	}
}
