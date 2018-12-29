package org.squiddev.plethora.integration.computercraft;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.computer.blocks.TileComputerBase;
import dan200.computercraft.shared.computer.core.ServerComputer;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;

import javax.annotation.Nonnull;
import java.util.Map;

@IMetaProvider.Inject(value = TileComputerBase.class, modId = ComputerCraft.MOD_ID, namespace = "computer")
public class MetaTileComputer extends BasicMetaProvider<TileComputerBase> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull TileComputerBase object) {
		ServerComputer computer = object.getServerComputer();

		Map<Object, Object> out = Maps.newHashMap();
		out.put("family", object.getFamily().toString());
		out.put("id", computer.getID());

		String label = computer.getLabel();
		if (!Strings.isNullOrEmpty(label)) out.put("label", label);

		out.put("isOn", computer.isOn());

		return out;
	}
}
