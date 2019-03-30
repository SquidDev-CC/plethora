package org.squiddev.plethora.gameplay.modules.methods;

import dan200.computercraft.api.lua.LuaException;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.wrapper.FromSubtarget;
import org.squiddev.plethora.api.method.wrapper.PlethoraMethod;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.gameplay.modules.PlethoraModules;
import org.squiddev.plethora.integration.EntityIdentifier;

import javax.annotation.Nonnull;
import java.util.Map;

public final class MethodsIntrospection {
	private MethodsIntrospection() {
	}

	@PlethoraMethod(module = PlethoraModules.INTROSPECTION_S, doc = "-- Get this entity's UUID.")
	public static String getID(@FromSubtarget EntityIdentifier identifier) {
		return identifier.getId().toString();
	}

	@PlethoraMethod(module = PlethoraModules.INTROSPECTION_S, doc = "-- Get this entity's UUID.")
	public static String getName(@FromSubtarget EntityIdentifier identifier) {
		return identifier.getName();
	}

	@PlethoraMethod(
		module = {PlethoraModules.INTROSPECTION_S, PlethoraModules.SENSOR_S},
		doc = "-- Get this entity's UUID."
	)
	public static Map<Object, Object> getMetaOwner(@FromSubtarget EntityIdentifier identifier, @Nonnull IContext<IModuleContainer> context) throws LuaException {
		return context.makePartialChild(identifier.getEntity()).getMeta();
	}
}
