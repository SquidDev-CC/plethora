package org.squiddev.plethora.integration.appliedenergistics;

import appeng.api.storage.data.IAEItemStack;
import appeng.core.AppEng;
import com.google.common.collect.Maps;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.integration.vanilla.meta.MetaItemBasic;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Injects(AppEng.MOD_ID)
public class MetaAEItemStack extends BasicMetaProvider<IAEItemStack> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull IAEItemStack object) {
		return Collections.singletonMap("count", object.getStackSize());
	}

	@Nonnull
	public static HashMap<Object, Object> getBasicProperties(@Nonnull IAEItemStack stack) {
		HashMap<Object, Object> data = Maps.newHashMap();
		data.putAll(MetaItemBasic.getBasicMeta(stack.getDefinition()));
		data.put("count", stack.getStackSize());
		data.put("isCraftable", stack.isCraftable());
		return data;
	}
}
