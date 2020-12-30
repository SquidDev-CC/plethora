package org.squiddev.plethora.integration.cbmp;

import codechicken.multipart.PartMap;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;
import org.squiddev.plethora.api.meta.TypedMeta;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.TypedLuaObject;
import org.squiddev.plethora.api.method.wrapper.FromTarget;
import org.squiddev.plethora.api.method.wrapper.Optional;
import org.squiddev.plethora.api.method.wrapper.PlethoraMethod;
import org.squiddev.plethora.utils.Helpers;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.squiddev.plethora.integration.cbmp.IntegrationMultipart.getBasicMeta;

public final class MethodsMultipart {
	private MethodsMultipart() {
	}

	@PlethoraMethod(modId = "forgemultipartcbe", doc = "-- Get a list of all parts in the multipart.")
	public static List<Map<String, ?>> listParts(@FromTarget TileMultipart multipart) {
		return Helpers.map(multipart.jPartList(), IntegrationMultipart::getBasicMeta);
	}

	@PlethoraMethod(modId = "forgemultipartcbe", doc = "-- Get a lookup of slot to parts.")
	public static Map<String, ?> listSlottedParts(@FromTarget TileMultipart container) {
		Map<String, Map<String, ?>> parts = new HashMap<>();

		for (PartMap slot : PartMap.values()) {
			TMultiPart part = container.partMap(slot.i);
			if (part != null) parts.put(slot.name().toLowerCase(Locale.ENGLISH), getBasicMeta(part));
		}

		return parts;
	}

	@PlethoraMethod(modId = "forgemultipartcbe", doc = "-- Get a reference to the part in the specified slot.")
	public static TypedLuaObject<TMultiPart> getSlottedPart(IContext<TileMultipart> context, PartMap slot) {
		TileMultipart container = context.getTarget();

		TMultiPart part = container.partMap(slot.i);
		return part == null
			? null
			: context.makeChild(part, new ReferenceMultipart(container, part)).getObject();
	}

	@Optional
	@PlethoraMethod(modId = "forgemultipartcbe", doc = "-- Get the metadata of the part in the specified slot.")
	public static TypedMeta<TMultiPart, ?> getSlottedPartMeta(IContext<TileMultipart> context, PartMap slot) {
		TileMultipart container = context.getTarget();

		TMultiPart part = container.partMap(slot.i);
		return part == null ? null : context.makePartialChild(part).getMeta();
	}
}
