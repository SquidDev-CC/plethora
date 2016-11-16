package org.squiddev.plethora.core.docdump;

import com.google.common.base.Strings;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import org.squiddev.plethora.api.PlethoraAPI;
import org.squiddev.plethora.utils.DebugLogger;

import javax.annotation.Nonnull;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

public class CommandDump extends CommandBase {
	@Nonnull
	@Override
	public String getCommandName() {
		return "plethora_dump";
	}

	@Nonnull
	@Override
	public String getCommandUsage(@Nonnull ICommandSender sender) {
		return "plethora_dump [name]";
	}

	@Override
	public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) throws CommandException {
		if (args.length < 1 || Strings.isNullOrEmpty(args[0])) throw new CommandException(getCommandUsage(sender));

		String name = args[0];
		try {
			OutputStream file = new FileOutputStream(name);

			try {
				PrintStream writer = new PrintStream(file);

				WriteDocumentation docs = new WriteDocumentation(writer);
				docs.writeHeader();
				docs.write(PlethoraAPI.instance().methodRegistry().getMethods());
				docs.writeFooter();
			} finally {
				file.close();
			}
		} catch (Throwable e) {
			DebugLogger.error("Cannot handle " + name, e);
			throw new CommandException(e.toString());
		}
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		return !server.isDedicatedServer() || super.checkPermission(server, sender);
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 4;
	}
}
