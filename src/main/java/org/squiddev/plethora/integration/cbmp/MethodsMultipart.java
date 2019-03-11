package org.squiddev.plethora.integration.cbmp;

import codechicken.multipart.PartMap;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;
import com.google.common.collect.Maps;
import dan200.computercraft.api.lua.ILuaObject;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.wrapper.PlethoraMethod;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

public class MethodsMultipart {
	@PlethoraMethod(modId = "forgemultipartcbe", doc = "-- Get a list of all parts in the multipart.")
	public static Map<Integer, ?> listParts(TileMultipart multipart) {
		Collection<? extends TMultiPart> parts = multipart.jPartList();

		int i = 0;
		Map<Integer, Map<Object, Object>> out = Maps.newHashMap();
		for (TMultiPart part : parts) {
			out.put(++i, MetaMultipart.getBasicMeta(part));
		}

		return out;
	}

	@PlethoraMethod(modId = "forgemultipartcbe", doc = "-- Get a lookup of slot to parts.")
	public static Map<String, ?> listSlottedParts(TileMultipart container, Object[] args) {
		Map<String, Map<Object, Object>> parts = Maps.newHashMap();

		for (PartMap slot : PartMap.values()) {
			TMultiPart part = container.partMap(slot.i);
			if (part != null) {
				parts.put(slot.name().toLowerCase(Locale.ENGLISH), MetaMultipart.getBasicMeta(part));
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

	@Nullable
	@PlethoraMethod(modId = "forgemultipartcbe", doc = "-- Get the metadata of the part in the specified slot.")
	public static Map<Object, Object> getSlottedPartMeta(IContext<TileMultipart> context, PartMap slot) {
		TileMultipart container = context.getTarget();

		TMultiPart part = container.partMap(slot.i);
		return part == null ? null : context.makePartialChild(part).getMeta();
	}
}
