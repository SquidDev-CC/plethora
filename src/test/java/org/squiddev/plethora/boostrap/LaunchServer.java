package org.squiddev.plethora.boostrap;

import net.minecraftforge.gradle.GradleStartCommon;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public final class LaunchServer extends GradleStartCommon {
	private static final Logger LOG = LogManager.getLogger();


	@Override
	protected String getTweakClass() {
		return "net.minecraftforge.fml.common.launcher.FMLServerTweaker";
	}

	@Override
	protected String getBounceClass() {
		return "net.minecraft.launchwrapper.Launch";
	}

	@Override
	protected void preLaunch(Map<String, String> argMap, List<String> extras) {
	}

	@Override
	protected void setDefaultArguments(Map<String, String> argMap) {
	}

	public static void main(String[] args) throws Throwable {
		// Verify we're running from the correct directory.
		File rootDir = new File(".").getCanonicalFile();
		if (!rootDir.getName().equals("server") || !rootDir.getParentFile().getName().equals("test-files")) {
			LOG.error("Should run test server from 'test-files/server' directory");
			System.exit(1);
			return;
		}

		File eulaFile = new File(rootDir, "eula.txt");
		if (!eulaFile.exists()) {
			LOG.warn("EULA has not been signed, generating it.");
			// OK, I know this is super dubious. But one needs a way to automate this.
			try (Writer writer = new OutputStreamWriter(new FileOutputStream(eulaFile), StandardCharsets.UTF_8)) {
				writer.write("# Automatically generated EULA. Please don't use this for a real server.\neula=true\n");
			}
		}

		File propertiesFile = new File(rootDir, "server.properties");
		if (!propertiesFile.exists()) {
			LOG.warn("server.properties has not been written, generating it");
			try (Writer writer = new OutputStreamWriter(new FileOutputStream(propertiesFile), StandardCharsets.UTF_8)) {
				writer.write("level-type=FLAT\n");
				writer.write("gamemode=1\n");
				writer.write("online-mode=false\n");
				writer.write("enable-command-block=true\n");
			}
		}

		System.setProperty("java.awt.headless", "true"); // No GUI
		System.setProperty("buildcraft.debug", "disable"); // No Buildcraft logging

		LOG.info("Launching Minecraft in {}", rootDir);
		new LaunchServer().launch(new String[]{"--username", "PlethoraDev"});
	}
}
