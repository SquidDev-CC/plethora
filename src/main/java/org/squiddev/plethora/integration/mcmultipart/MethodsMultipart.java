package org.squiddev.plethora.integration.mcmultipart;

import com.google.common.collect.Maps;
import dan200.computercraft.api.lua.LuaException;
import mcmultipart.MCMultiPartMod;
import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.IMultipartContainer;
import mcmultipart.multipart.PartSlot;
import org.squiddev.plethora.api.method.*;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

import static org.squiddev.plethora.api.method.ArgumentHelper.getEnum;
import static org.squiddev.plethora.api.method.ArgumentHelper.getUUID;


public class MethodsMultipart {
	@BasicObjectMethod.Inject(
		value = IMultipartContainer.class, modId = MCMultiPartMod.MODID, worldThread = true,
		doc = "function():table -- Get a list of all parts in the multipart."
	)
	public static Object[] listParts(IContext<IMultipartContainer> context, Object[] args) {
		Collection<? extends IMultipart> parts = context.getTarget().getParts();

		int i = 0;
		Map<Integer, Map<Object, Object>> out = Maps.newHashMap();
		for (IMultipart part : parts) {
			out.put(++i, MetaMultipart.getBasicMeta(part));
		}

		return new Object[]{out};
	}

	@BasicMethod.Inject(
		value = IMultipartContainer.class, modId = MCMultiPartMod.MODID,
		doc = "function(id:string):table|nil -- Get a reference to one part."
	)
	public static MethodResult getPart(final IUnbakedContext<IMultipartContainer> context, Object[] args) throws LuaException {
		final UUID id = getUUID(args, 0);

		return MethodResult.nextTick(new Callable<MethodResult>() {
			@Override
			public MethodResult call() throws Exception {
				IContext<IMultipartContainer> baked = context.bake();
				IMultipartContainer container = baked.getTarget();

				IMultipart part = container.getPartFromID(id);
				return part == null
					? MethodResult.empty()
					: MethodResult.result(baked.makeChild(part, new ReferenceMultipart(container, id)).getObject());
			}
		});
	}

	@BasicMethod.Inject(
		value = IMultipartContainer.class, modId = MCMultiPartMod.MODID,
		doc = "function(id:string):table|nil -- Get the metadata of one part."
	)
	public static MethodResult getPartMeta(final IUnbakedContext<IMultipartContainer> context, Object[] args) throws LuaException {
		final UUID id = getUUID(args, 0);

		return MethodResult.nextTick(new Callable<MethodResult>() {
			@Override
			public MethodResult call() throws Exception {
				IContext<IMultipartContainer> baked = context.bake();
				IMultipartContainer container = baked.getTarget();

				IMultipart part = container.getPartFromID(id);
				return part == null
					? MethodResult.empty()
					: MethodResult.result(baked.makePartialChild(part).getMeta());
			}
		});
	}

	@BasicObjectMethod.Inject(
		value = IMultipartContainer.class, modId = MCMultiPartMod.MODID, worldThread = true,
		doc = "function():table -- Get a lookup of slot to parts."
	)
	public static Object[] listSlottedParts(IContext<IMultipartContainer> context, Object[] args) {
		Map<String, Map<Object, Object>> parts = Maps.newHashMap();

		IMultipartContainer container = context.getTarget();
		for (PartSlot slot : PartSlot.VALUES) {
			IMultipart part = container.getPartInSlot(slot);
			if (part != null) {
				parts.put(slot.name().toLowerCase(Locale.ENGLISH), MetaMultipart.getBasicMeta(part));
			}
		}

		return new Object[]{parts};
	}

	@BasicMethod.Inject(
		value = IMultipartContainer.class, modId = MCMultiPartMod.MODID,
		doc = "function(slot:string):table|nil -- Get a reference to the part in the specified slot."
	)
	public static MethodResult getSlottedPart(final IUnbakedContext<IMultipartContainer> context, Object[] args) throws LuaException {
		final PartSlot slot = getEnum(args, 0, PartSlot.class);

		return MethodResult.nextTick(new Callable<MethodResult>() {
			@Override
			public MethodResult call() throws Exception {
				IContext<IMultipartContainer> baked = context.bake();
				IMultipartContainer container = baked.getTarget();

				IMultipart part = container.getPartInSlot(slot);
				return part == null
					? MethodResult.empty()
					: MethodResult.result(baked.makeChild(part, new ReferenceMultipart(container, part)).getObject());
			}
		});
	}

	@BasicMethod.Inject(
		value = IMultipartContainer.class, modId = MCMultiPartMod.MODID,
		doc = "function(slot:string):table|nil -- Get the metadata of the part in the specified slot."
	)
	public static MethodResult getSlottedPartMeta(final IUnbakedContext<IMultipartContainer> context, Object[] args) throws LuaException {
		final PartSlot slot = getEnum(args, 0, PartSlot.class);

		return MethodResult.nextTick(new Callable<MethodResult>() {
			@Override
			public MethodResult call() throws Exception {
				IContext<IMultipartContainer> baked = context.bake();
				IMultipartContainer container = baked.getTarget();

				IMultipart part = container.getPartInSlot(slot);
				return part == null
					? MethodResult.empty()
					: MethodResult.result(baked.makePartialChild(part).getMeta());
			}
		});
	}
}
