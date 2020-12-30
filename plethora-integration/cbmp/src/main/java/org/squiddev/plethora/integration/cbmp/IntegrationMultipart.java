package org.squiddev.plethora.integration.cbmp;

import codechicken.multipart.PartMap;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TSlottedPart;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.SimpleMetaProvider;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

@Injects("forgemultipartcbe")
public final class IntegrationMultipart {
	public static final SimpleMetaProvider<TMultiPart> META_MULTIPART = IntegrationMultipart::getBasicMeta;

	public static final SimpleMetaProvider<TSlottedPart> META_SLOTTED_PART = object -> {
		int slots = object.getSlotMask();

		List<String> maps = Arrays.stream(PartMap.values())
			.filter(x -> (slots & x.mask) != 0)
			.map(x -> x.name().toLowerCase(Locale.ENGLISH))
			.collect(Collectors.toList());
		return Collections.singletonMap("slots", maps);
	};

	private IntegrationMultipart() {
	}

	@Nonnull
	public static Map<String, ?> getBasicMeta(@Nonnull TMultiPart part) {
		return Collections.singletonMap("name", part.getType().toString());
	}
}
