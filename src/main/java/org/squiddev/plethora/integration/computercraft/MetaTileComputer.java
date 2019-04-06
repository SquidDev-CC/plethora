package org.squiddev.plethora.integration.computercraft;

import com.google.common.base.Strings;
import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.computer.blocks.TileComputerBase;
import dan200.computercraft.shared.computer.core.ServerComputer;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

@IMetaProvider.Inject(value = TileComputerBase.class, modId = ComputerCraft.MOD_ID, namespace = "computer")
public class MetaTileComputer extends BasicMetaProvider<TileComputerBase> {
	@Nonnull
	@Override
	public Map<String, ?> getMeta(@Nonnull TileComputerBase object) {
		ServerComputer computer = object.getServerComputer();

		Map<String, Object> out = new HashMap<>();
		out.put("family", object.getFamily().toString());
		out.put("id", computer.getID());

		String label = computer.getLabel();
		if (!Strings.isNullOrEmpty(label)) out.put("label", label);

		out.put("isOn", computer.isOn());

		return out;
	}
}
