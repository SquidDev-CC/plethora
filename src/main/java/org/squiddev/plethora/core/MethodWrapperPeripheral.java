package org.squiddev.plethora.core;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraftforge.fml.common.Optional;
import org.squiddev.cctweaks.api.network.INetworkAccess;
import org.squiddev.cctweaks.api.network.INetworkedPeripheral;
import org.squiddev.cctweaks.api.network.Packet;
import org.squiddev.cctweaks.api.peripheral.IPeripheralTargeted;
import org.squiddev.cctweaks.core.network.NetworkAccessDelegate;
import org.squiddev.plethora.api.method.IMethod;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.method.MethodResult;
import org.squiddev.plethora.api.reference.IReference;
import org.squiddev.plethora.utils.DebugLogger;
import org.squiddev.plethora.utils.Helpers;

import java.util.List;
import java.util.Map;

import static org.squiddev.plethora.api.reference.Reference.id;

/**
 * Handles integration with a {@link IPeripheral}
 */
@Optional.InterfaceList({
	@Optional.Interface(iface = "org.squiddev.cctweaks.api.network.INetworkedPeripheral", modid = "CCTweaks"),
	@Optional.Interface(iface = "org.squiddev.cctweaks.api.peripheral.IPeripheralTargeted", modid = "CCTweaks")
})
public class MethodWrapperPeripheral extends MethodWrapper implements IPeripheral, INetworkedPeripheral, IPeripheralTargeted {
	private final Object owner;
	private final String type;

	private Object delegate;

	public MethodWrapperPeripheral(Object owner, List<IMethod<?>> methods, List<IUnbakedContext<?>> contexts) {
		this(tryGetName(owner), owner, methods, contexts);
	}

	public MethodWrapperPeripheral(String name, Object owner, List<IMethod<?>> methods, List<IUnbakedContext<?>> contexts) {
		super(methods, contexts);
		this.owner = owner;
		this.type = name;
	}

	private static String tryGetName(Object owner) {
		try {
			return Helpers.getName(owner);
		} catch (Throwable e) {
			DebugLogger.error("Error getting data for " + owner.getClass().getName(), e);
			return owner.getClass().getSimpleName();
		}
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public Object[] callMethod(IComputerAccess access, ILuaContext luaContext, int method, final Object[] args) throws LuaException, InterruptedException {
		IUnbakedContext context = getContext(method).withContext(getReferences(access, luaContext));
		MethodResult result = doCallMethod(getMethod(method), context, args);

		return unwrap(result, luaContext);
	}

	@Override
	public void attach(IComputerAccess access) {
	}

	@Override
	public void detach(IComputerAccess access) {
	}

	@Override
	public boolean equals(IPeripheral other) {
		if (this == other) return true;
		if (other == null || !(other instanceof MethodWrapperPeripheral)) return false;

		MethodWrapperPeripheral otherP = (MethodWrapperPeripheral) other;
		return owner == otherP.owner && equalMethods(otherP);
	}

	//region CCTweaks
	@Override
	@Optional.Method(modid = "CCTweaks")
	protected IReference<?>[] getReferences(IComputerAccess access, ILuaContext context) {
		return new IReference[]{id(access), id(context), id(getDelegate())};
	}

	@Optional.Method(modid = "CCTweaks")
	private NetworkAccessDelegate getDelegate() {
		NetworkAccessDelegate delegate = (NetworkAccessDelegate) this.delegate;
		if (delegate == null) {
			this.delegate = delegate = new NetworkAccessDelegate();
		}

		return delegate;
	}

	@Override
	@Optional.Method(modid = "CCTweaks")
	public void attachToNetwork(INetworkAccess network, String name) {
		getDelegate().add(network);
	}

	@Override
	@Optional.Method(modid = "CCTweaks")
	public void detachFromNetwork(INetworkAccess network, String name) {
		getDelegate().remove(network);
	}

	@Override
	@Optional.Method(modid = "CCTweaks")
	public void networkInvalidated(INetworkAccess network, Map<String, IPeripheral> oldPeripherals, Map<String, IPeripheral> newPeripherals) {
	}

	@Override
	@Optional.Method(modid = "CCTweaks")
	public void receivePacket(INetworkAccess network, Packet packet, double distanceTravelled) {
	}

	@Override
	@Optional.Method(modid = "CCTweaks")
	public Object getTarget() {
		return owner;
	}
	//endregion
}
