package org.squiddev.plethora.integration.cbmp;

import codechicken.multipart.PartMap;
import codechicken.multipart.TSlottedPart;
import com.google.common.collect.Maps;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

@IMetaProvider.Inject(value = TSlottedPart.class, modId = "forgemultipartcbe")
public class MetaSlottedMultipart extends BasicMetaProvider<TSlottedPart> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull TSlottedPart object) {
		int slots = object.getSlotMask();

		int i = 0;
		Map<Integer, String> out = Maps.newHashMapWithExpectedSize(Integer.bitCount(i));
		for (PartMap slot : PartMap.values()) {
			if ((slots & slot.mask) != 0) {
				out.put(++i, slot.name().toLowerCase(Locale.ENGLISH));
			}
		}
		return Collections.singletonMap("slots", out);
	}
}
