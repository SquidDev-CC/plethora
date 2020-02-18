package org.squiddev.plethora.integration.buildcraft;

import buildcraft.api.tiles.IControllable;
import buildcraft.core.BCCore;
import dan200.computercraft.api.lua.LuaException;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.method.wrapper.FromTarget;
import org.squiddev.plethora.api.method.wrapper.PlethoraMethod;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Injects(BCCore.MODID)
public final class TileControllable {

	@PlethoraMethod(modId = BCCore.MODID, doc = "-- Get a list of all supported control modes.")
	public static List<String> getSupportedControlModes(@FromTarget IControllable controllable) {
		return Arrays.stream(IControllable.Mode.VALUES).filter(controllable::acceptsControlMode).map(mode -> mode.lowerCaseName).collect(Collectors.toList());
	}

	@PlethoraMethod(modId = BCCore.MODID, doc = "-- Get the control mode.")
	public static String getControlMode(@FromTarget IControllable controllable) {
		return controllable.getControlMode().lowerCaseName;
	}

	@PlethoraMethod(modId = BCCore.MODID, doc = "-- Reports true if the Block accept the given mode.")
	public static boolean acceptsControlMode(@FromTarget IControllable controllable, String modeName) throws LuaException {
		IControllable.Mode mode;
		try {
			mode = IControllable.Mode.valueOf(modeName.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new LuaException("No such control mode");
		}


		return controllable.acceptsControlMode(mode);
	}

	@PlethoraMethod(modId = BCCore.MODID, doc = "-- Set the Control mode of the block to the given mode.")
	public static void setControlMode(@FromTarget IControllable controllable, String modeName) throws LuaException {
		IControllable.Mode mode;
		try {
			mode = IControllable.Mode.valueOf(modeName.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new LuaException("No such control mode");
		}

		if (controllable.acceptsControlMode(mode)) {
			controllable.setControlMode(mode);
		}
	}
}
