package org.squiddev.plethora.gameplay.client.gui;

import net.minecraft.client.gui.GuiScreen;
import org.squiddev.plethora.core.client.gui.GuiConfigBase;
import org.squiddev.plethora.core.client.gui.GuiConfigFactory;
import org.squiddev.plethora.gameplay.ConfigGameplay;
import org.squiddev.plethora.gameplay.Plethora;

public class GuiConfigGameplay extends GuiConfigFactory {
	@Override
	public GuiScreen createConfigGui(GuiScreen parentScreen) {
		return new GuiConfigImpl(parentScreen);
	}

	private static class GuiConfigImpl extends GuiConfigBase {
		GuiConfigImpl(GuiScreen screen) {
			super(screen, ConfigGameplay.configuration, Plethora.ID, Plethora.NAME);
		}
	}
}
