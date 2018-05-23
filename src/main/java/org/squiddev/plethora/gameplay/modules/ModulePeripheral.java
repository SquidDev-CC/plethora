package org.squiddev.plethora.gameplay.modules;

import dan200.computercraft.api.peripheral.IPeripheral;
import org.apache.commons.lang3.tuple.Pair;
import org.squiddev.plethora.api.IAttachable;
import org.squiddev.plethora.api.method.IMethod;
import org.squiddev.plethora.core.AttachableWrapperPeripheral;
import org.squiddev.plethora.core.UnbakedContext;
import org.squiddev.plethora.core.executor.TaskRunner;

import java.util.Collection;
import java.util.List;

public class ModulePeripheral extends AttachableWrapperPeripheral {
	private final int stackHash;

	public ModulePeripheral(
		String name, Object owner, Pair<List<IMethod<?>>, List<UnbakedContext<?>>> methods,
		TaskRunner runner, Collection<IAttachable> attachments,
		int stackHash
	) {
		super(name, owner, methods, runner, attachments);
		this.stackHash = stackHash;
	}

	@Override
	public boolean equals(IPeripheral other) {
		return super.equals(other) && other instanceof ModulePeripheral && stackHash == ((ModulePeripheral) other).stackHash;
	}
}
