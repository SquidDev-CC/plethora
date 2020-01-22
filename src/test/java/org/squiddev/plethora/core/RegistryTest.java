package org.squiddev.plethora.core;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import org.junit.Test;
import org.squiddev.plethora.integration.vanilla.transfer.TransferTileEntity;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class RegistryTest {

	@Test
	public void registerInstanceClass() {
		ConfigCore.Blacklist.blacklistProviders = new ArrayList<>();
		Registry.register(TransferTileEntity.class, null);
		PlethoraCore.buildRegistries();

		TileEntity te = new TileEntityFurnace();
		assertEquals(te, TransferRegistry.instance.getTransferLocation(te, "self"));
	}
}
