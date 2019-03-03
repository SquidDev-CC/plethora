package org.squiddev.plethora.integration.appliedenergistics;

import appeng.api.networking.crafting.ICraftingCPU;
import appeng.core.AppEng;
import com.google.common.collect.Maps;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.BasicMetaProvider;

import javax.annotation.Nonnull;
import java.util.Map;

@Injects(AppEng.MOD_ID)
public class MetaCraftingCPU extends BasicMetaProvider<ICraftingCPU> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull ICraftingCPU cpu) {
		Map<Object, Object> out = Maps.newHashMap();
		out.put("name", cpu.getName());
		out.put("busy", cpu.isBusy());
		out.put("coprocessors", cpu.getCoProcessors());
		out.put("storage", cpu.getAvailableStorage());
		return out;
	}
}
