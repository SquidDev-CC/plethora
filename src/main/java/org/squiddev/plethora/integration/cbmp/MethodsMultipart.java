package org.squiddev.plethora.integration.cbmp;

import codechicken.multipart.PartMap;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;
import dan200.computercraft.api.lua.ILuaObject;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.wrapper.FromTarget;
import org.squiddev.plethora.api.method.wrapper.Optional;
import org.squiddev.plethora.api.method.wrapper.PlethoraMethod;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.squiddev.plethora.integration.cbmp.IntegrationMultipart.getBasicMeta;

public final class MethodsMultipart {
	private MethodsMultipart() {
	}

	@PlethoraMethod(modId = "forgemultipartcbe", doc = "-- Get a list of all parts in the multipart.")
	public static Map<Integer, ?> listParts(@FromTarget TileMultipart multipart) {
		Collection<? extends TMultiPart> parts = multipart.jPartList();

		int i = 0;
		Map<Integer, Map<Object, Object>> out = new HashMap<>();
		for (TMultiPart part : parts) out.put(++i, getBasicMeta(part));

		return out;
	}

	@PlethoraMethod(modId = "forgemultipartcbe", doc = "-- Get a lookup of slot to parts.")
	public static Map<String, ?> listSlottedParts(@FromTarget TileMultipart container) {
		Map<String, Map<Object, Object>> parts = new HashMap<>();

		for (PartMap slot : PartMap.values()) {
			TMultiPart part = container.partMap(slot.i);
			if (part != null) {
				parts.put(slot.name().toLowerCase(Locale.ENGLISH), getBasicMeta(part));
			}
		}

		return parts;
	}

	@PlethoraMethod(modId = "forgemultipartcbe", doc = "-- Get a reference to the part in the specified slot.")
	public static ILuaObject getSlottedPart(IContext<TileMultipart> context, PartMap slot) {
		TileMultipart container = context.getTarget();

		TMultiPart part = container.partMap(slot.i);
		return part == null
			? null
			: context.makeChild(part, new ReferenceMultipart(container, part)).getObject();
	}

	@Optional
	@PlethoraMethod(modId = "forgemultipartcbe", doc = "-- Get the metadata of the part in the specified slot.")
	public static Map<Object, Object> getSlottedPartMeta(IContext<TileMultipart> context, PartMap slot) {
		TileMultipart container = context.getTarget();

		TMultiPart part = container.partMap(slot.i);
		return part == null ? null : context.makePartialChild(part).getMeta();
	}
}
