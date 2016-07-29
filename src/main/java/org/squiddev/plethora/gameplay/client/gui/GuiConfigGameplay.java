package org.squiddev.plethora.gameplay.client.gui;

import net.minecraft.client.gui.GuiScreen;
import org.squiddev.plethora.core.client.gui.GuiConfigBase;
import org.squiddev.plethora.core.client.gui.GuiConfigFactory;
import org.squiddev.plethora.gameplay.ConfigGameplay;
import org.squiddev.plethora.gameplay.Plethora;

public class GuiConfigGameplay extends GuiConfigFactory {
	@Override
	public Class<? extends GuiScreen> mainConfigGuiClass() {
		return GuiConfigImpl.class;
	}

	public static class GuiConfigImpl extends GuiConfigBase {
		public GuiConfigImpl(GuiScreen screen) {
			super(screen, ConfigGameplay.configuration, Plethora.ID, Plethora.NAME);
		}
	}
}
