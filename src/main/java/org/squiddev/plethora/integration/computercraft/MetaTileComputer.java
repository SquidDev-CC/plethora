package org.squiddev.plethora.integration.computercraft;

import com.google.common.base.Strings;
import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.computer.blocks.ComputerProxy;
import dan200.computercraft.shared.computer.blocks.TileComputerBase;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.BasicMetaProvider;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Injects(ComputerCraft.MOD_ID)
public final class MetaTileComputer extends BasicMetaProvider<TileComputerBase> {
	@Nonnull
	@Override
	public Map<String, ?> getMeta(@Nonnull TileComputerBase tile) {
		ComputerProxy computer = tile.createProxy();

		Map<String, Object> out = new HashMap<>();
		out.put("family", tile.getFamily().toString());
		out.put("id", computer.assignID());

		String label = computer.getLabel();
		if (!Strings.isNullOrEmpty(label)) out.put("label", label);

		out.put("isOn", computer.isOn());

		return Collections.singletonMap("computer", out);
	}
}
