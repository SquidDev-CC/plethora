package org.squiddev.plethora.gameplay;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.squiddev.plethora.BlockBase;
import org.squiddev.plethora.client.tile.RenderManipulator;

public class BlockManipulator extends BlockBase<TileManipulator> {
	public static final double OFFSET = 10.0 / 16.0;

	public BlockManipulator() {
		super("manipulator", TileManipulator.class);
		setBlockBounds(0, 0, 0, 1, (float) OFFSET, 1);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int i) {
		return new TileManipulator();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void clientInit() {
		super.clientInit();
		ClientRegistry.bindTileEntitySpecialRenderer(TileManipulator.class, new RenderManipulator());
	}

	@Override
	public boolean isFullBlock() {
		return false;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}
}
