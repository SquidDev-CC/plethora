package org.squiddev.plethora.utils;

import baubles.common.Baubles;
import net.minecraftforge.fml.common.Loader;
import vazkii.botania.common.lib.LibMisc;

public class LoadedCache {
	private static boolean loaded;
	private static boolean hasBotania;
	private static boolean hasBaubles;

	private static void load() {
		hasBotania = Loader.isModLoaded(LibMisc.MOD_ID);
		hasBaubles = Loader.isModLoaded(Baubles.MODID);
		loaded = true;
	}

	public static boolean hasBotania() {
		if (!loaded) load();
		return hasBotania;
	}

	public static boolean hasBaubles() {
		if (!loaded) load();
		return hasBaubles;
	}
}
