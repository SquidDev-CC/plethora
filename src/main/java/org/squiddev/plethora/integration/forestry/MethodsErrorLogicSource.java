package org.squiddev.plethora.integration.forestry;

import forestry.api.core.IErrorLogicSource;
import forestry.api.core.IErrorState;
import forestry.core.config.Constants;
import org.squiddev.plethora.api.method.wrapper.FromTarget;
import org.squiddev.plethora.api.method.wrapper.PlethoraMethod;
import org.squiddev.plethora.utils.LuaList;

import java.util.Map;

public final class MethodsErrorLogicSource {
	private MethodsErrorLogicSource() {
	}

	@PlethoraMethod(modId = Constants.MOD_ID, doc = "-- Get any errors preventing operation")
	public static Map<Integer, String> getErrors(@FromTarget IErrorLogicSource source) {
		return source.getErrorLogic().getErrorStates().stream()
			.map(IErrorState::getUniqueName)
			.collect(LuaList.toLuaList())
			.asMap();
	}
}
