package org.squiddev.plethora.integration.mcmultipart;

import com.google.common.collect.Maps;
import dan200.computercraft.api.lua.LuaException;
import mcmultipart.MCMultiPart;
import mcmultipart.api.container.IMultipartContainer;
import mcmultipart.api.container.IPartInfo;
import mcmultipart.api.slot.IPartSlot;
import net.minecraft.util.ResourceLocation;
import org.squiddev.plethora.api.method.*;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;

import static dan200.computercraft.core.apis.ArgumentHelper.getString;


public class MethodsMultipart {
	@BasicObjectMethod.Inject(
		value = IMultipartContainer.class, modId = MCMultiPart.MODID, worldThread = true,
		doc = "function():table -- Get a list of all parts in the multipart."
	)
	public static Object[] listParts(IContext<IMultipartContainer> context, Object[] args) {
		Collection<? extends IPartInfo> parts = context.getTarget().getParts().values();

		int i = 0;
		Map<Integer, Map<Object, Object>> out = Maps.newHashMap();
		for (IPartInfo part : parts) {
			out.put(++i, MetaMultipart.getBasicMeta(part));
		}

		return new Object[]{out};
	}

	@BasicObjectMethod.Inject(
		value = IMultipartContainer.class, modId = MCMultiPart.MODID, worldThread = true,
		doc = "function():table -- Get a lookup of slot to parts."
	)
	public static Object[] listSlottedParts(IContext<IMultipartContainer> context, Object[] args) {
		Map<String, Map<Object, Object>> parts = Maps.newHashMap();

		for (Map.Entry<IPartSlot, ? extends IPartInfo> slot : context.getTarget().getParts().entrySet()) {
			parts.put(slot.getKey().getRegistryName().toString().toLowerCase(Locale.ENGLISH), MetaMultipart.getBasicMeta(slot.getValue()));
		}

		return new Object[]{parts};
	}

	@BasicMethod.Inject(
		value = IMultipartContainer.class, modId = MCMultiPart.MODID,
		doc = "function(slot:string):table|nil -- Get a reference to the part in the specified slot."
	)
	public static MethodResult getSlottedPart(final IUnbakedContext<IMultipartContainer> context, Object[] args) throws LuaException {
		String slotName = getString(args, 0);
		final IPartSlot slot = MCMultiPart.slotRegistry.getObject(new ResourceLocation(slotName));
		if (slot == null) throw new LuaException("Bad name '" + slotName + "' for argument 1");

		return MethodResult.nextTick(new Callable<MethodResult>() {
			@Override
			public MethodResult call() throws Exception {
				IContext<IMultipartContainer> baked = context.bake();
				IMultipartContainer container = baked.getTarget();

				IPartInfo part = container.get(slot).orElse(null);
				return part == null
					? MethodResult.empty()
					: MethodResult.result(baked.makeChild(new ReferenceMultipart(container, part)).getObject());
			}
		});
	}

	@BasicMethod.Inject(
		value = IMultipartContainer.class, modId = MCMultiPart.MODID,
		doc = "function(slot:string):table|nil -- Get the metadata of the part in the specified slot."
	)
	public static MethodResult getSlottedPartMeta(final IUnbakedContext<IMultipartContainer> context, Object[] args) throws LuaException {
		String slotName = getString(args, 0);
		final IPartSlot slot = MCMultiPart.slotRegistry.getObject(new ResourceLocation(slotName));
		if (slot == null) throw new LuaException("Bad name '" + slotName + "' for argument 1");

		return MethodResult.nextTick(new Callable<MethodResult>() {
			@Override
			public MethodResult call() throws Exception {
				IContext<IMultipartContainer> baked = context.bake();
				IMultipartContainer container = baked.getTarget();

				IPartInfo part = container.get(slot).orElse(null);
				return part == null
					? MethodResult.empty()
					: MethodResult.result(baked.makePartialChild(part).getMeta());
			}
		});
	}
}
