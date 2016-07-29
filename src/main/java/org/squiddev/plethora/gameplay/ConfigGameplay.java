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
	 * Config options for the various modules
	 */
	public static class Modules {
		/**
		 * The minimum power of a laser.
		 */
		@DefaultDouble(0.5)
		@Range(min = 0)
		public static double laserMinimum;

		/**
		 * The maximum power of a laser.
		 */
		@DefaultDouble(5)
		@Range(min = 0)
		public static double laserMaximum;

		/**
		 * The fuel cost of a laser
		 * See Plethora.cfg#CostSystem for more info
		 */
		@DefaultDouble(10)
		@Range(min = 0)
		public static double laserCost;

		/**
		 * The maximum velocity the kinetic manipulator
		 * can apply to you.
		 */
		@DefaultInt(4)
		@Range(min = 0)
		public static int kineticLaunchMax;

		/**
		 * The radius scanners can get blocks in
		 */
		@DefaultInt(8)
		@Range(min = 0)
		public static int scannerRadius;

		/**
		 * The radius sensors can get entities in
		 */
		@DefaultInt(16)
		@Range(min = 0)
		public static int sensorRadius;
	}
}
