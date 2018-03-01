package org.squiddev.plethora.integration.cbmp;

import codechicken.multipart.PartMap;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;
import com.google.common.collect.Maps;
import dan200.computercraft.api.lua.LuaException;
import org.squiddev.plethora.api.method.*;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;

import static org.squiddev.plethora.api.method.ArgumentHelper.getEnum;

public class MethodsMultipart {
	@BasicObjectMethod.Inject(
		value = TileMultipart.class, modId = "forgemultipartcbe", worldThread = true,
		doc = "function():table -- Get a list of all parts in the multipart."
	)
	public static Object[] listParts(IContext<TileMultipart> context, Object[] args) {
		Collection<? extends TMultiPart> parts = context.getTarget().jPartList();

		int i = 0;
		Map<Integer, Map<Object, Object>> out = Maps.newHashMap();
		for (TMultiPart part : parts) {
			out.put(++i, MetaMultipart.getBasicMeta(part));
		}

		return new Object[]{out};
	}

	@BasicObjectMethod.Inject(
		value = TileMultipart.class, modId = "forgemultipartcbe", worldThread = true,
		doc = "function():table -- Get a lookup of slot to parts."
	)
	public static Object[] listSlottedParts(IContext<TileMultipart> context, Object[] args) {
		Map<String, Map<Object, Object>> parts = Maps.newHashMap();

		TileMultipart container = context.getTarget();
		for (PartMap slot : PartMap.values()) {
			TMultiPart part = container.partMap(slot.i);
			if (part != null) {
				parts.put(slot.name().toLowerCase(Locale.ENGLISH), MetaMultipart.getBasicMeta(part));
			}
		}

		return new Object[]{parts};
	}

	@BasicMethod.Inject(
		value = TileMultipart.class, modId = "forgemultipartcbe",
		doc = "function(slot:string):table|nil -- Get a reference to the part in the specified slot."
	)
	public static MethodResult getSlottedPart(final IUnbakedContext<TileMultipart> context, Object[] args) throws LuaException {
		final PartMap slot = getEnum(args, 0, PartMap.class);

		return MethodResult.nextTick(() -> {
			IContext<TileMultipart> baked = context.bake();
			TileMultipart container = baked.getTarget();

			TMultiPart part = container.partMap(slot.i);
			return part == null
				? MethodResult.empty()
				: MethodResult.result(baked.makeChild(part, new ReferenceMultipart(container, part)).getObject());
		});
	}

	@BasicMethod.Inject(
		value = TileMultipart.class, modId = "forgemultipartcbe",
		doc = "function(slot:string):table|nil -- Get the metadata of the part in the specified slot."
	)
	public static MethodResult getSlottedPartMeta(final IUnbakedContext<TileMultipart> context, Object[] args) throws LuaException {
		final PartMap slot = getEnum(args, 0, PartMap.class);

		return MethodResult.nextTick(() -> {
			IContext<TileMultipart> baked = context.bake();
			TileMultipart container = baked.getTarget();

			TMultiPart part = container.partMap(slot.i);
			return part == null
				? MethodResult.empty()
				: MethodResult.result(baked.makePartialChild(part).getMeta());
		});
	}
}
