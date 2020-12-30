package org.squiddev.plethora.integration.xnet;

import mcjty.xnet.api.gui.IEditorGui;
import mcjty.xnet.api.gui.IndicatorIcon;
import mcjty.xnet.api.helper.AbstractConnectorSettings;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class NetworkConnectorSettings extends AbstractConnectorSettings {
	NetworkConnectorSettings(@Nonnull EnumFacing side) {
		super(side);
	}

	@Nullable
	@Override
	public IndicatorIcon getIndicatorIcon() {
		return NetworkChannelType.CONNECTOR_ICON;
	}

	@Nullable
	@Override
	public String getIndicator() {
		return null;
	}

	@Override
	public boolean isEnabled(String tag) {
		if (tag.equals(TAG_FACING)) return advanced;
		return true;
	}

	@Override
	public void createGui(IEditorGui gui) {
		advanced = gui.isAdvanced();
		sideGui(gui);
		colorsGui(gui);
	}
}
