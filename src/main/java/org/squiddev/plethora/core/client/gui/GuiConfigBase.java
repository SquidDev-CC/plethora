package org.squiddev.plethora.core.client.gui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;

import java.util.ArrayList;
import java.util.List;

public abstract class GuiConfigBase extends GuiConfig {
	public GuiConfigBase(GuiScreen screen, Configuration config, String id, String name) {
		super(screen, getConfigElements(config), id, false, false, name);
	}

	private static List<IConfigElement> getConfigElements(Configuration config) {
		ArrayList<IConfigElement> elements = new ArrayList<>();
		for (String category : config.getCategoryNames()) {
			if (!category.contains(".")) {
				elements.add(new ConfigElement(config.getCategory(category)));
			}
		}
		return elements;
	}
}
