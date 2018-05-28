package org.squiddev.plethora.integration.forestry;

import forestry.api.core.IErrorLogicSource;
import forestry.api.core.IErrorState;
import forestry.core.config.Constants;
import org.squiddev.plethora.api.method.BasicObjectMethod;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.utils.LuaList;

public class MethodsErrorLogicSource {
	@BasicObjectMethod.Inject(
			value = IErrorLogicSource.class, modId = Constants.MOD_ID, worldThread = true,
			doc = "function():table -- Get any errors preventing operation"
	)
	public static Object[] getErrors(IContext<IErrorLogicSource> context, Object[] arg) {
		return new Object[] {
				context.getTarget().getErrorLogic().getErrorStates().stream()
						.map(IErrorState::getUniqueName)
						.collect(LuaList.toLuaList()).asMap()
		};
	}
}
