package org.squiddev.plethora.integration.cbmp;

import codechicken.multipart.PartMap;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TSlottedPart;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.SimpleMetaProvider;
import org.squiddev.plethora.api.method.LuaList;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

@Injects("forgemultipartcbe")
public final class IntegrationMultipart {
	public static final SimpleMetaProvider<TMultiPart> META_MULTIPART = IntegrationMultipart::getBasicMeta;

	public static final SimpleMetaProvider<TSlottedPart> META_SLOTTED_PART = object -> {
		int slots = object.getSlotMask();

		LuaList<String> out = new LuaList<>(Integer.bitCount(slots));
		for (PartMap slot : PartMap.values()) {
			if ((slots & slot.mask) != 0) out.add(slot.name().toLowerCase(Locale.ENGLISH));
		}
		return Collections.singletonMap("slots", out.asMap());
	};

	private IntegrationMultipart() {
	}

	@Nonnull
	public static Map<String, ?> getBasicMeta(@Nonnull TMultiPart part) {
		return Collections.singletonMap("name", part.getType().toString());
	}
}
