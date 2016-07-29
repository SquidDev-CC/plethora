package org.squiddev.plethora.core.client.gui;

import net.minecraft.client.gui.GuiScreen;
import org.squiddev.plethora.core.ConfigCore;
import org.squiddev.plethora.core.PlethoraCore;
import org.squiddev.plethora.gameplay.Plethora;

public class GuiConfigCore extends GuiConfigFactory {
	@Override
	public Class<? extends GuiScreen> mainConfigGuiClass() {
		return GuiConfigImpl.class;
	}

	public static class GuiConfigImpl extends GuiConfigBase {
		public GuiConfigImpl(GuiScreen screen) {
			super(screen, ConfigCore.configuration, PlethoraCore.ID, Plethora.NAME);
		}
	}
}
