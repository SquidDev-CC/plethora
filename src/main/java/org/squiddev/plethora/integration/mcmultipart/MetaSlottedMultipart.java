package org.squiddev.plethora.integration.mcmultipart;

import com.google.common.collect.Maps;
import mcmultipart.MCMultiPartMod;
import mcmultipart.multipart.ISlottedPart;
import mcmultipart.multipart.PartSlot;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Map;

@IMetaProvider.Inject(value = ISlottedPart.class, modId = MCMultiPartMod.MODID)
public class MetaSlottedMultipart extends BasicMetaProvider<ISlottedPart> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull ISlottedPart object) {
		EnumSet<PartSlot> slots = object.getSlotMask();

		int i = 0;
		Map<Integer, String> out = Maps.newHashMapWithExpectedSize(slots.size());
		for (PartSlot slot : slots) {
			out.put(++i, slot.name().toLowerCase(Locale.ENGLISH));
		}
		return Collections.<Object, Object>singletonMap("slots", out);
	}
}
