package org.squiddev.plethora.boostrap;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.shared.computer.blocks.TileCommandComputer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;

@Mod(
	modid = PlethoraTestMod.ID,
	name = "Plethora Test Runner",
	version = "1.0.0",
	dependencies = "required-after:plethora;required-after:plethora-core"
)
public class PlethoraTestMod implements ForgeChunkManager.OrderedLoadingCallback {
	static final String ID = "plethora-test";

	static final Logger LOG = LogManager.getLogger(ID);

	private ForgeChunkManager.Ticket ticket;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		ForgeChunkManager.setForcedChunkLoadingCallback(this, this);
		ComputerCraftAPI.registerAPIFactory(HowlCiStub::new);
	}

	@Mod.EventHandler
	public void onServerStarted(FMLServerStartedEvent event) {
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		World world = server.getWorld(0);

		// Stop all the clocks, cut off the telephone.
		GameRules gamerules = world.getGameRules();
		gamerules.setOrCreateGameRule("doMobSpawning", "false");
		gamerules.setOrCreateGameRule("doDaylightCycle", "false");
		gamerules.setOrCreateGameRule("doWeatherCycle", "false");
		for (World serverWorld : server.worlds) if (serverWorld != null) serverWorld.setWorldTime(6000);

		LOG.info("Dumping methods and metadata providers");
		server.getCommandManager().executeCommand(server, "plethora dump plethora-docs.json");
		server.getCommandManager().executeCommand(server, "plethora dump plethora-docs.html");

		// Force the chunk at (0, 0) to be loaded.
		if (ticket == null) {
			LOG.info("Acquiring a new chunkloading ticket");
			ticket = ForgeChunkManager.requestTicket(this, world, ForgeChunkManager.Type.NORMAL);
		}
		ForgeChunkManager.forceChunk(ticket, new ChunkPos(0, 0));
		LOG.info("Forced chunk at (0, 0)");

		setupChunk(world);
	}

	@Override
	public void ticketsLoaded(List<ForgeChunkManager.Ticket> tickets, World world) {
	}

	@Override
	public List<ForgeChunkManager.Ticket> ticketsLoaded(List<ForgeChunkManager.Ticket> tickets, World world, int maxTicketCount) {
		return Collections.emptyList();
	}

	public static void setupChunk(World world) {
		// Clear the chunk of all blocks.
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(0, 0, 0);
		IBlockState state = Blocks.AIR.getDefaultState();
		for (int y = 0; y < 256; y++) {
			for (int x = 0; x < 16; x++) {
				for (int z = 0; z < 16; z++) {
					pos.setPos(x, y, z);
					world.setBlockState(pos, state);
				}
			}
		}
		LOG.info("Cleared chunk at (0, 0)");

		BlockPos computerPos = new BlockPos(7, 128, 7);
		world.setBlockState(computerPos, ComputerCraft.Blocks.commandComputer.getDefaultState());

		TileEntity tile = world.getTileEntity(computerPos);
		if (!(tile instanceof TileCommandComputer)) {
			LOG.error("Expected TileCommandComputer, got {}", tile);
			return;
		}

		TileCommandComputer computer = (TileCommandComputer) tile;
		computer.setComputerID(0);
		computer.setLabel("Test Runner");
		computer.createProxy().turnOn();
		LOG.info("Created computer at " + computerPos);
	}
}
