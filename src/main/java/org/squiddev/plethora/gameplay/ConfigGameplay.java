package org.squiddev.plethora.gameplay;

import net.minecraftforge.common.config.Configuration;
import org.squiddev.configgen.*;

import java.io.File;

@Config(languagePrefix = "gui.config.plethora.")
public final class ConfigGameplay {
	public static Configuration configuration;

	public static void init(File file) {
		ConfigGameplayForgeLoader.init(file);
		configuration = ConfigGameplayForgeLoader.getConfiguration();
	}

	public static void sync() {
		ConfigGameplayForgeLoader.sync();
	}

	/**
	 * Config options for the laser module
	 */
	public static class Laser {
		/**
		 * The minimum power of a laser.
		 */
		@DefaultDouble(0.5)
		@Range(min = 0)
		public static double minimumPotency;

		/**
		 * The maximum power of a laser.
		 */
		@DefaultDouble(5)
		@Range(min = 0)
		public static double maximumPotency;

		/**
		 * The energy cost per potency for a laser.
		 * See Plethora.cfg#CostSystem for more info
		 */
		@DefaultDouble(10)
		@Range(min = 0)
		public static double cost;

		/**
		 * The damage done to an entity by a laser per potency.
		 */
		@DefaultDouble(4)
		@Range(min = 0)
		public static double damage;

		/**
		 * The maximum time in ticks a laser can exist for before it'll despawn.
		 */
		@DefaultInt(5 * 20)
		@Range(min = 0)
		public static int lifetime;
	}

	public static class Kinetic {
		/**
		 * The maximum velocity the kinetic manipulator can apply to you.
		 */
		@DefaultInt(4)
		@Range(min = 0)
		public static int launchMax;

		/**
		 * The cost per launch power
		 */
		@DefaultInt(0)
		@Range(min = 0)
		public static int launchCost;

		/**
		 * The value to scale the y velocity by, helps limit how high the player can go.
		 */
		@DefaultDouble(0.5)
		@Range(min = 0)
		public static double launchYScale;

		/**
		 * Whether to scale the fall distance after launching.
		 *
		 * This means the player will not die from fall damage if they launch themselves
		 * upwards in order to cancel out their negative velocity. This may not work correctly
		 * with mods which provide custom gravity, such as Galacticraft.
		 */
		@DefaultBoolean(true)
		public static boolean launchFallReset;

		/**
		 * Whether to reset the floating timer after launching.
		 *
		 * This means the player will not be kicked for flying after using the kinetic augment a lot.
		 */
		@DefaultBoolean(true)
		public static boolean launchFloatReset;

		/**
		 * The value to scale the velocity by when flying, helps limit how fast the player can go.
		 */
		@DefaultDouble(0.4)
		@Range(min = 0)
		public static double launchElytraScale;

		/**
		 * The maximum range that the entity can path find to.
		 */
		@DefaultInt(32)
		@Range(min = 1)
		public static int walkRange;

		/**
		 * The maximum speed that the entity can walk at.
		 */
		@DefaultInt(3)
		@Range(min = 1)
		public static int walkSpeed;

		/**
		 * The cost of walking.
		 * This is multiplied by the distance and speed walked.
		 */
		@DefaultInt(0)
		@Range(min = 0)
		public static int walkCost;

		/**
		 * The maximum range that endermen can teleport to.
		 */
		@DefaultInt(32)
		@Range(min = 1)
		public static int teleportRange;

		/**
		 * The cost per block teleported
		 */
		@DefaultInt(0)
		@Range(min = 0)
		public static int teleportCost;

		/**
		 * The cost to shoot an arrow per potency.
		 */
		@DefaultInt(20)
		@Range(min = 0)
		public static int shootCost;

		/**
		 * The maximum velocity a minecart can be propelled at.
		 */
		@DefaultDouble(2)
		@Range(min = 0)
		public static double propelMax;

		/**
		 * The cost per velocity unit.
		 */
		@DefaultDouble(0)
		@Range(min = 0)
		public static double propelCost;
	}

	public static class Scanner {
		/**
		 * The radius scanners can get blocks in
		 */
		@DefaultInt(8)
		@Range(min = 0)
		public static int radius;
	}

	public static class Sensor {
		/**
		 * The radius sensors can get entities in
		 */
		@DefaultInt(16)
		@Range(min = 0)
		public static int radius;
	}

	public static class Chat {
		/**
		 * The maximum length a chat message can be.
		 *
		 * Set to 0 to disable
		 */
		@DefaultInt(100)
		@Range(min = 0)
		public static int maxLength;

		/**
		 * Whether formatting codes are allowed in chat messages.
		 */
		@DefaultBoolean(false)
		public static boolean allowFormatting;

		/**
		 * Whether offline players are allowed to post chat messages.
		 * Otherwise this is just limited to those who are online players.
		 */
		@DefaultBoolean(true)
		public static boolean allowOffline;

		/**
		 * Whether mobs are allowed to post chat messages.
		 * Otherwise this is just limited to players.
		 */
		@DefaultBoolean(true)
		public static boolean allowMobs;
	}

	/**
	 * Various options that don't belong to anything
	 */
	public static class Miscellaneous {
		/**
		 * Fun rendering overlay for various objects.
		 * Basically I'm slightly vain.
		 */
		@DefaultBoolean(true)
		public static boolean funRender;
	}
}
