package org.squiddev.plethora.gameplay.registry;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.squiddev.plethora.gameplay.client.RenderInterfaceLiving;
import org.squiddev.plethora.gameplay.client.RenderOverlay;
import org.squiddev.plethora.gameplay.keyboard.ItemKeyboard;
import org.squiddev.plethora.gameplay.minecart.EntityMinecartComputer;
import org.squiddev.plethora.gameplay.modules.BlockManipulator;
import org.squiddev.plethora.gameplay.modules.ChatListener;
import org.squiddev.plethora.gameplay.modules.ChatVisualiser;
import org.squiddev.plethora.gameplay.modules.ItemModule;
import org.squiddev.plethora.gameplay.neural.ItemNeuralConnector;
import org.squiddev.plethora.gameplay.neural.ItemNeuralInterface;
import org.squiddev.plethora.gameplay.redstone.BlockRedstoneIntegrator;

import java.util.HashSet;
import java.util.Set;

/**
 * The proxy class
 */
public final class Registry {
	private static final Set<IModule> modules = new HashSet<>();

	private static boolean setup = false;
	private static boolean preInit = false;
	private static boolean init = false;
	private static boolean postInit = false;

	public static ItemNeuralInterface itemNeuralInterface;
	public static ItemModule itemModule;
	public static ItemKeyboard itemKeyboard;
	public static BlockManipulator blockManipulator;
	public static BlockRedstoneIntegrator blockRedstoneIntegrator;

	private static void addModule(IModule module) {
		if (module instanceof IClientModule) {
			module = new RegisterWrapperClient((IClientModule) module);
		}

		modules.add(module);

		if (preInit && module.canLoad()) {
			module.preInit();
			if (init) {
				module.init();
				if (postInit) module.postInit();
			}
		}
	}

	public static void setup() {
		if (setup) throw new IllegalStateException("Attempting to setup twice");
		setup = true;

		addModule(itemModule = new ItemModule());
		addModule(blockManipulator = new BlockManipulator());
		addModule(new RenderOverlay());
		addModule(new ChatVisualiser());
		addModule(new ChatListener());

		addModule(itemNeuralInterface = new ItemNeuralInterface());
		addModule(new ItemNeuralConnector());
		addModule(new RenderInterfaceLiving());

		addModule(new EntityMinecartComputer.MinecartModule());

		addModule(itemKeyboard = new ItemKeyboard());

		addModule(blockRedstoneIntegrator = new BlockRedstoneIntegrator());
	}

	public static void preInit() {
		if (!setup) throw new IllegalStateException("Cannot preInit before setup");
		if (preInit) throw new IllegalStateException("Attempting to preInit twice");
		preInit = true;
		for (IModule module : modules) {
			if (module.canLoad()) module.preInit();
		}
	}

	public static void init() {
		if (!preInit) throw new IllegalStateException("Cannot init before preInit");
		if (init) throw new IllegalStateException("Attempting to init twice");

		init = true;
		for (IModule module : modules) {
			if (module.canLoad()) module.init();
		}
	}

	public static void postInit() {
		if (!preInit) throw new IllegalStateException("Cannot init before preInit");
		if (!init) throw new IllegalStateException("Cannot postInit before init");
		if (postInit) throw new IllegalStateException("Attempting to postInit twice");

		postInit = true;
		for (IModule module : modules) {
			if (module.canLoad()) module.postInit();
		}
	}

	/**
	 * Magic classes to allow calling client only methods
	 */
	private static class RegisterWrapper implements IModule {
		protected final IClientModule base;

		private RegisterWrapper(IClientModule base) {
			this.base = base;
		}

		@Override
		public boolean canLoad() {
			return base.canLoad();
		}

		@Override
		public void preInit() {
			base.preInit();
		}

		@Override
		public void init() {
			base.init();
		}

		@Override
		public void postInit() {
			base.postInit();
		}

		@Override
		public String toString() {
			return base.toString();
		}
	}

	/**
	 * Magic classes to allow calling client only methods
	 */
	private static class RegisterWrapperClient extends RegisterWrapper {
		private RegisterWrapperClient(IClientModule base) {
			super(base);
		}

		@Override
		@SideOnly(Side.CLIENT)
		public void init() {
			super.init();
			base.clientInit();
		}

		@Override
		@SideOnly(Side.CLIENT)
		public void preInit() {
			super.preInit();
			base.clientPreInit();
		}

		@Override
		public String toString() {
			return base.toString();
		}
	}
}

