package org.squiddev.plethora.gameplay.minecart;

import com.google.common.collect.Maps;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.core.apis.ILuaAPI;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandManager;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.CommandBlockBaseLogic;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.squiddev.plethora.gameplay.Plethora;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;

/**
 * Copy of {@link CommandAPI} but with the Minecart Computer instead.
 */
public class CommandAPI extends CommandBlockBaseLogic implements ILuaAPI {
	private final Entity entity;
	private final MinecraftServer server;

	private final Map<Integer, String> output = Maps.newHashMap();

	public CommandAPI(Entity entity) {
		this.entity = entity;
		this.server = entity.getEntityWorld().getMinecraftServer();
	}

	@Override
	public String[] getNames() {
		return new String[]{"commands"};
	}

	@Nonnull
	@Override
	public String[] getMethodNames() {
		return new String[]{"exec", "execAsync", "list", "getBlockPosition", "getBlockInfos", "getBlockInfo"};
	}

	private Object[] doCommand(String command) {
		if (server != null && server.isCommandBlockEnabled()) {
			ICommandManager commandManager = server.getCommandManager();
			try {
				output.clear();

				int result = commandManager.executeCommand(this, command);
				return new Object[]{result > 0, Maps.newHashMap(output)};
			} catch (Throwable t) {
				return new Object[]{false, Collections.singletonMap(1, "Java Exception Thrown: " + t)};
			}
		}
		return new Object[]{false, Collections.singletonMap(1, ("Command blocks disabled by server"))};
	}

	private Object getBlockInfo(World world, BlockPos pos) {
		IBlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		String name = Block.REGISTRY.getNameForObject(block).toString();
		int metadata = block.getMetaFromState(state);

		Map<Object, Object> table = Maps.newHashMap();
		table.put("name", name);
		table.put("metadata", metadata);

		Map<Object, Object> stateTable = Maps.newHashMap();
		for (Map.Entry<IProperty<?>, ?> entry : state.getActualState(world, pos).getProperties().entrySet()) {
			String propertyName = entry.getKey().getName();
			Object value = entry.getValue();
			if (value instanceof String || value instanceof Number || value instanceof Boolean) {
				stateTable.put(propertyName, value);
			} else {
				stateTable.put(propertyName, value.toString());
			}
		}
		table.put("state", stateTable);

		return table;
	}

	@Override
	public Object[] callMethod(@Nonnull ILuaContext context, int method, @Nonnull Object[] arguments)
		throws LuaException, InterruptedException {
		switch (method) {
			case 0: { // exec
				if (arguments.length < 1 || !(arguments[0] instanceof String)) {
					throw new LuaException("Expected string");
				}
				final String command = (String) arguments[0];
				return context.executeMainThreadTask(() -> doCommand(command));
			}
			case 1: { // execAsync
				if (arguments.length < 1 || !(arguments[0] instanceof String)) {
					throw new LuaException("Expected string");
				}
				final String command = (String) arguments[0];
				long taskID = context.issueMainThreadTask(() -> doCommand(command));
				return new Object[]{taskID};
			}
			case 2: { // list
				return context.executeMainThreadTask(() -> {
					int i = 1;
					Map<Object, Object> result = Maps.newHashMap();
					if (server != null) {
						ICommandManager commandManager = server.getCommandManager();
						Map commands = commandManager.getCommands();
						for (Object entryObject : commands.entrySet()) {
							Map.Entry entry = (Map.Entry) entryObject;
							String name = (String) entry.getKey();
							ICommand command = (ICommand) entry.getValue();
							try {
								if (command.checkPermission(server, CommandAPI.this)) {
									result.put(i++, name);
								}
							} catch (RuntimeException e) {
								Plethora.LOG.error("Error executing command", e);
							}
						}
					}
					return new Object[]{result};
				});
			}
			case 3: // getBlockPosition
				BlockPos pos = getPosition();
				return new Object[]{pos.getX(), pos.getY(), pos.getZ()};
			case 4: // getBlockInfos
				if (arguments.length < 6 || !(arguments[0] instanceof Number) || !(arguments[1] instanceof Number) || !(arguments[2] instanceof Number) || !(arguments[3] instanceof Number) || !(arguments[4] instanceof Number) || !(arguments[5] instanceof Number)) {
					throw new LuaException("Expected number, number, number, number, number, number");
				}
				final int minx = ((Number) arguments[0]).intValue();
				final int miny = ((Number) arguments[1]).intValue();
				final int minz = ((Number) arguments[2]).intValue();
				final int maxx = ((Number) arguments[3]).intValue();
				final int maxy = ((Number) arguments[4]).intValue();
				final int maxz = ((Number) arguments[5]).intValue();
				return context.executeMainThreadTask(() -> {
					World world = entity.getEntityWorld();
					BlockPos min = new BlockPos(Math.min(minx, maxx), Math.min(miny, maxy), Math.min(minz, maxz));

					BlockPos max = new BlockPos(Math.max(minx, maxx), Math.max(miny, maxy), Math.max(minz, maxz));
					if (!world.isValid(min) || !world.isValid(max)) {
						throw new LuaException("Co-ordinates out or range");
					}
					if ((max.getX() - min.getX() + 1) * (max.getY() - min.getY() + 1) * (max.getZ() - min.getZ() + 1) > 4096) {
						throw new LuaException("Too many blocks");
					}
					int i = 1;
					Map<Object, Object> results = Maps.newHashMap();
					for (int y = min.getY(); y <= max.getY(); y++) {
						for (int z = min.getZ(); z <= max.getZ(); z++) {
							for (int x = min.getX(); x <= max.getX(); x++) {
								BlockPos pos1 = new BlockPos(x, y, z);
								results.put(i++, getBlockInfo(world, pos1));
							}
						}
					}
					return new Object[]{results};
				});
			case 5: // getBlockInfo
				if (arguments.length < 3 || !(arguments[0] instanceof Number) || !(arguments[1] instanceof Number) || !(arguments[2] instanceof Number)) {
					throw new LuaException("Expected number, number, number");
				}
				final int x = ((Number) arguments[0]).intValue();
				final int y = ((Number) arguments[1]).intValue();
				final int z = ((Number) arguments[2]).intValue();
				context.executeMainThreadTask(() -> {
					World world = entity.getEntityWorld();
					BlockPos position = new BlockPos(x, y, z);
					if (world.isValid(position)) {
						return new Object[]{getBlockInfo(world, position)};
					}
					throw new LuaException("Co-ordinates out or range");
				});
		}
		return null;
	}

	@Nonnull
	@Override
	public String getName() {
		return entity.getName();
	}

	@Nonnull
	@Override
	public ITextComponent getDisplayName() {
		return entity.getDisplayName();
	}

	@Override
	public void sendMessage(@Nonnull ITextComponent component) {
		output.put(output.size() + 1, component.getUnformattedText());
	}

	@Override
	public boolean canUseCommand(int permLevel, String commandName) {
		return permLevel <= 2;
	}

	@Nonnull
	@Override
	public BlockPos getPosition() {
		return entity.getPosition();
	}

	@Nonnull
	@Override
	public Vec3d getPositionVector() {
		return entity.getPositionVector();
	}

	@Nonnull
	@Override
	public World getEntityWorld() {
		return entity.getEntityWorld();
	}

	@Override
	public Entity getCommandSenderEntity() {
		return entity;
	}

	@Override
	public void setCommandStat(@Nonnull CommandResultStats.Type type, int amount) {
	}

	@Override
	public void updateCommand() {
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getCommandBlockType() {
		return 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void fillInInfo(@Nonnull ByteBuf byteBuf) {
	}

	@Nullable
	@Override
	public MinecraftServer getServer() {
		return server;
	}

	@Override
	public void startup() {
	}

	@Override
	public void shutdown() {
	}

	@Override
	public void advance(double v) {
	}
}
