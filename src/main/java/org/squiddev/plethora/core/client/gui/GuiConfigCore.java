package org.squiddev.plethora.core.client.gui;

import net.minecraft.client.gui.GuiScreen;
import org.squiddev.plethora.core.ConfigCore;
import org.squiddev.plethora.core.PlethoraCore;
import org.squiddev.plethora.gameplay.Plethora;

public class GuiConfigCore extends GuiConfigFactory {
	@Override
	public GuiScreen createConfigGui(GuiScreen screen) {
		return new GuiConfigImpl(screen);
	}

	private static class GuiConfigImpl extends GuiConfigBase {
		GuiConfigImpl(GuiScreen screen) {
			super(screen, ConfigCore.configuration, PlethoraCore.ID, Plethora.NAME);
		}
	}
}
