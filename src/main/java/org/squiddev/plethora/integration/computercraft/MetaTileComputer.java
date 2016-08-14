package org.squiddev.plethora.integration.computercraft;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import dan200.computercraft.shared.computer.blocks.IComputerTile;
import dan200.computercraft.shared.computer.core.IComputer;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;

import javax.annotation.Nonnull;
import java.util.Map;

@IMetaProvider.Inject(value = IComputerTile.class, modId = "ComputerCraft", namespace = "computer")
public class MetaTileComputer extends BasicMetaProvider<IComputerTile> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull IComputerTile object) {
		IComputer computer = object.getComputer();

		Map<Object, Object> out = Maps.newHashMap();
		out.put("family", object.getFamily().toString());
		out.put("id", computer.getID());

		String label = computer.getLabel();
		if (!Strings.isNullOrEmpty(label)) out.put("label", label);

		out.put("isOn", computer.isOn());

		return out;
	}
}
