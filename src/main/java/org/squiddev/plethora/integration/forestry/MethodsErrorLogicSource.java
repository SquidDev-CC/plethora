package org.squiddev.plethora.integration.forestry;

import forestry.api.core.IErrorLogicSource;
import forestry.api.core.IErrorState;
import forestry.core.config.Constants;
import org.squiddev.plethora.api.method.LuaList;
import org.squiddev.plethora.api.method.wrapper.FromTarget;
import org.squiddev.plethora.api.method.wrapper.PlethoraMethod;

import java.util.Map;

public final class MethodsErrorLogicSource {
	private MethodsErrorLogicSource() {
	}

	@PlethoraMethod(modId = Constants.MOD_ID, doc = "-- Get any errors preventing operation")
	public static Map<Integer, String> getErrors(@FromTarget IErrorLogicSource source) {
		return LuaList.of(source.getErrorLogic().getErrorStates(), IErrorState::getUniqueName).asMap();
	}
}
