package org.squiddev.plethora.core.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.client.IModGuiFactory;

import java.util.Set;

public abstract class GuiConfigFactory implements IModGuiFactory {
	@Override
	public void initialize(Minecraft minecraft) {
	}

	@Override
	public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
		return null;
	}

	@Override
	public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement runtimeOptionCategoryElement) {
		return null;
	}
}
