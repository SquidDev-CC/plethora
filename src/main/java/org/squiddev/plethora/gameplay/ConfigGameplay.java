package org.squiddev.plethora.gameplay;

import net.minecraftforge.common.config.Configuration;
import org.squiddev.configgen.Config;
import org.squiddev.configgen.DefaultDouble;
import org.squiddev.configgen.DefaultInt;
import org.squiddev.configgen.Range;
import org.squiddev.plethora.gameplay.ConfigGameplayForgeLoader;

import java.io.File;

@Config(languagePrefix = "gui.config.plethora.")
public final class ConfigGameplay {
	public static Configuration configuration;

	public static void init(File file) {
		ConfigGameplayForgeLoader.init(file);
		configuration = ConfigGameplayForgeLoader.getConfiguration();
	}

	public static void sync() {
		ConfigGameplayForgeLoader.doSync();
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
		 * The fuel cost per potency for a laser.
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
}
