package org.squiddev.plethora.core;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.network.Packet;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraftforge.fml.common.Optional;
import org.apache.commons.lang3.tuple.Pair;
import org.squiddev.cctweaks.CCTweaks;
import org.squiddev.cctweaks.api.network.INetworkAccess;
import org.squiddev.cctweaks.api.network.INetworkedPeripheral;
import org.squiddev.cctweaks.api.peripheral.IPeripheralTargeted;
import org.squiddev.cctweaks.core.network.NetworkAccessDelegate;
import org.squiddev.plethora.api.method.ContextKeys;
import org.squiddev.plethora.api.method.IMethod;
import org.squiddev.plethora.api.method.IResultExecutor;
import org.squiddev.plethora.api.method.MethodResult;
import org.squiddev.plethora.core.executor.IExecutorFactory;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

/**
 * Handles integration with a {@link IPeripheral}
 */
@Optional.InterfaceList({
	@Optional.Interface(iface = "org.squiddev.cctweaks.api.network.INetworkedPeripheral", modid = CCTweaks.ID),
	@Optional.Interface(iface = "org.squiddev.cctweaks.api.peripheral.IPeripheralTargeted", modid = CCTweaks.ID)
})
public class MethodWrapperPeripheral extends MethodWrapper implements IPeripheral, INetworkedPeripheral, IPeripheralTargeted {
	private final Object owner;
	private final String type;
	private final IExecutorFactory factory;

	private Object delegate;

	public MethodWrapperPeripheral(String name, Object owner, List<IMethod<?>> methods, List<UnbakedContext<?>> contexts, IExecutorFactory factory) {
		super(methods, contexts);
		this.owner = owner;
		this.type = name;
		this.factory = factory;
	}

	public MethodWrapperPeripheral(String name, Object owner, Pair<List<IMethod<?>>, List<UnbakedContext<?>>> methods, IExecutorFactory factory) {
		this(name, owner, methods.getLeft(), methods.getRight(), factory);
	}

	@Nonnull
	@Override
	public String getType() {
		return type;
	}

	@Override
	public Object[] callMethod(@Nonnull IComputerAccess access, @Nonnull ILuaContext luaContext, int method, @Nonnull final Object[] args) throws LuaException, InterruptedException {
		IResultExecutor executor = factory.createExecutor(access);

		UnbakedContext context = getContext(method);
		Object[] extraRef = getReferences(access, luaContext);

		int totalSize = context.keys.length + extraRef.length;
		String[] keys = new String[totalSize];
		Object[] references = new Object[totalSize];

		for (int i = 0; i < extraRef.length; i++) {
			keys[i] = ContextKeys.COMPUTER;
			references[i] = extraRef[i];
		}

		System.arraycopy(context.keys, 0, keys, extraRef.length, context.keys.length);
		System.arraycopy(context.references, 0, references, extraRef.length, context.references.length);

		UnbakedContext<?> full = new UnbakedContext(
			context.target, keys, references, context.handler, context.modules, context.executor
		);

		MethodResult result = doCallMethod(getMethod(method), full, args);
		return executor.execute(result, luaContext);
	}

	@Override
	public void attach(@Nonnull IComputerAccess access) {
	}

	@Override
	public void detach(@Nonnull IComputerAccess access) {
	}

	protected IExecutorFactory getExecutorFactory() {
		return factory;
	}

	@Override
	public boolean equals(IPeripheral other) {
		if (this == other) return true;
		if (other == null || !(other instanceof MethodWrapperPeripheral)) return false;
		if (!getType().equals(other.getType())) return false;

		MethodWrapperPeripheral otherP = (MethodWrapperPeripheral) other;
		return owner == otherP.owner && equalMethods(otherP);
	}

	//region CCTweaks
	@Override
	@Optional.Method(modid = CCTweaks.ID)
	protected Object[] getReferences(IComputerAccess access, ILuaContext context) {
		return new Object[]{access, context, getDelegate()};
	}

	@Optional.Method(modid = CCTweaks.ID)
	private NetworkAccessDelegate getDelegate() {
		NetworkAccessDelegate delegate = (NetworkAccessDelegate) this.delegate;
		if (delegate == null) {
			this.delegate = delegate = new NetworkAccessDelegate();
		}

		return delegate;
	}

	@Override
	@Optional.Method(modid = CCTweaks.ID)
	public void attachToNetwork(@Nonnull INetworkAccess network, @Nonnull String name) {
		getDelegate().add(network);
	}

	@Override
	@Optional.Method(modid = CCTweaks.ID)
	public void detachFromNetwork(@Nonnull INetworkAccess network, @Nonnull String name) {
		getDelegate().remove(network);
	}

	@Override
	@Optional.Method(modid = CCTweaks.ID)
	public void networkInvalidated(@Nonnull INetworkAccess network, @Nonnull Map<String, IPeripheral> oldPeripherals, @Nonnull Map<String, IPeripheral> newPeripherals) {
	}

	@Override
	@Optional.Method(modid = CCTweaks.ID)
	public void receivePacket(@Nonnull INetworkAccess network, @Nonnull Packet packet, double distanceTravelled) {
	}

	@Override
	@Optional.Method(modid = CCTweaks.ID)
	public Object getTarget() {
		return owner;
	}
	//endregion
}
