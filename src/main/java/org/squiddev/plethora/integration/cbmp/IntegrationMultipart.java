package org.squiddev.plethora.integration.cbmp;

import codechicken.multipart.PartMap;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TSlottedPart;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.SimpleMetaProvider;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Injects("forgemultipartcbe")
public final class IntegrationMultipart {
	public static final SimpleMetaProvider<TMultiPart> META_MULTIPART = IntegrationMultipart::getBasicMeta;

	public static final SimpleMetaProvider<TSlottedPart> META_SLOTTED_PART = object -> {
		int slots = object.getSlotMask();

		int i = 0;
		Map<Integer, String> out = new HashMap<>(Integer.bitCount(i));
		for (PartMap slot : PartMap.values()) {
			if ((slots & slot.mask) != 0) {
				out.put(++i, slot.name().toLowerCase(Locale.ENGLISH));
			}
		}
		return Collections.singletonMap("slots", out);
	};

	private IntegrationMultipart() {
	}

	@Nonnull
	public static Map<Object, Object> getBasicMeta(@Nonnull TMultiPart part) {
		Map<Object, Object> out = new HashMap<>();
		out.put("name", part.getType().toString());
		return out;
	}
}
