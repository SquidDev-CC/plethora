package org.squiddev.plethora.gameplay.minecart;

import com.google.common.collect.ImmutableMap;
import dan200.computercraft.api.lua.ILuaAPI;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
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
import java.util.HashMap;
import java.util.Map;

import static dan200.computercraft.core.apis.ArgumentHelper.getInt;
import static dan200.computercraft.core.apis.ArgumentHelper.getString;

/**
 * Copy of {@link CommandAPI} but with the Minecart Computer instead.
 */
public class CommandAPI extends CommandBlockBaseLogic implements ILuaAPI {
	private final Entity entity;
	private final MinecraftServer server;

	private final Map<Integer, String> output = new HashMap<>();

	public CommandAPI(Entity entity) {
		this.entity = entity;
		server = entity.getEntityWorld().getMinecraftServer();
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

	private static Map<Object, Object> createOutput(String output) {
		return Collections.singletonMap(1, output);
	}

	private Object[] doCommand(String command) {
		if (server == null || !server.isCommandBlockEnabled()) {
			return new Object[]{false, Collections.singletonMap(1, "Command blocks disabled by server")};
		}

		ICommandManager commandManager = server.getCommandManager();
		try {
			output.clear();

			int result = commandManager.executeCommand(this, command);
			return new Object[]{result > 0, new HashMap<>(output)};
		} catch (Exception | LinkageError t) {
			return new Object[]{false, Collections.singletonMap(1, "Java Exception Thrown: " + t)};
		}
	}

	private static Object getBlockInfo(World world, BlockPos pos) {
		// Get the details of the block
		IBlockState state = world.getBlockState(pos);
		Block block = state.getBlock();

		Map<Object, Object> table = new HashMap<>(3);
		table.put("name", Block.REGISTRY.getNameForObject(block).toString());
		table.put("metadata", block.getMetaFromState(state));

		Map<Object, Object> stateTable = new HashMap<>();
		for (ImmutableMap.Entry<IProperty<?>, Comparable<?>> entry : state.getActualState(world, pos).getProperties().entrySet()) {
			IProperty<?> property = entry.getKey();
			stateTable.put(property.getName(), getPropertyValue(property, entry.getValue()));
		}
		table.put("state", stateTable);

		return table;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Object getPropertyValue(IProperty property, Comparable value) {
		if (value instanceof String || value instanceof Number || value instanceof Boolean) return value;
		return property.getName(value);
	}

	@Override
	public Object[] callMethod(@Nonnull ILuaContext context, int method, @Nonnull Object[] arguments) throws LuaException, InterruptedException {
		switch (method) {
			case 0: { // exec
				final String command = getString(arguments, 0);
				return context.executeMainThreadTask(() -> doCommand(command));
			}
			case 1: { // execAsync
				final String command = getString(arguments, 0);
				long taskID = context.issueMainThreadTask(() -> doCommand(command));
				return new Object[]{taskID};
			}
			case 2: // list
				return context.executeMainThreadTask(() ->
				{
					int i = 1;
					Map<Object, Object> result = new HashMap<>();
					if (server != null) {
						ICommandManager commandManager = server.getCommandManager();
						Map<String, ICommand> commands = commandManager.getCommands();
						for (Map.Entry<String, ICommand> entry : commands.entrySet()) {
							String name = entry.getKey();
							ICommand command = entry.getValue();
							try {
								if (command.checkPermission(server, entity)) {
									result.put(i++, name);
								}
							} catch (Throwable t) {
								Plethora.LOG.error("Error checking permissions of command.", t);
							}
						}
					}
					return new Object[]{result};
				});
			case 3: { // getBlockPosition
				// This is probably safe to do on the Lua thread. Probably.
				BlockPos pos = entity.getPosition();
				return new Object[]{pos.getX(), pos.getY(), pos.getZ()};
			}
			case 4: { // getBlockInfos
				final int minX = getInt(arguments, 0);
				final int minY = getInt(arguments, 1);
				final int minZ = getInt(arguments, 2);
				final int maxX = getInt(arguments, 3);
				final int maxY = getInt(arguments, 4);
				final int maxZ = getInt(arguments, 5);
				return context.executeMainThreadTask(() ->
				{
					World world = getEntityWorld();
					BlockPos min = new BlockPos(Math.min(minX, maxX), Math.min(minY, maxY), Math.min(minZ, maxZ));
					BlockPos max = new BlockPos(Math.max(minX, maxX), Math.max(minY, maxY), Math.max(minZ, maxZ));
					if (!world.isValid(min) || !world.isValid(max)) {
						throw new LuaException("Co-ordinates out or range");
					}
					if ((max.getX() - min.getX() + 1) * (max.getY() - min.getY() + 1) * (max.getZ() - min.getZ() + 1) > 4096) {
						throw new LuaException("Too many blocks");
					}
					int i = 1;
					Map<Object, Object> results = new HashMap<>();
					for (int y = min.getY(); y <= max.getY(); y++) {
						for (int z = min.getZ(); z <= max.getZ(); z++) {
							for (int x = min.getX(); x <= max.getX(); x++) {
								BlockPos pos = new BlockPos(x, y, z);
								results.put(i++, getBlockInfo(world, pos));
							}
						}
					}
					return new Object[]{results};
				});
			}
			case 5: { // getBlockInfo
				final int x = getInt(arguments, 0);
				final int y = getInt(arguments, 1);
				final int z = getInt(arguments, 2);
				return context.executeMainThreadTask(() ->
				{
					// Get the details of the block
					World world = getEntityWorld();
					BlockPos position = new BlockPos(x, y, z);
					if (!world.isValid(position)) throw new LuaException("co-ordinates out or range");
					return new Object[]{getBlockInfo(world, position)};
				});
			}
			default: {
				return null;
			}
		}
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
}
