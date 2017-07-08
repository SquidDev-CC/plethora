package org.squiddev.plethora.core;

import com.google.common.base.Strings;
import com.google.common.io.Files;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import org.squiddev.plethora.api.PlethoraAPI;
import org.squiddev.plethora.core.docdump.HTMLWriter;
import org.squiddev.plethora.core.docdump.IDocWriter;
import org.squiddev.plethora.core.docdump.JSONWriter;
import org.squiddev.plethora.utils.DebugLogger;

import javax.annotation.Nonnull;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

public class CommandPlethora extends CommandBase {
	@Nonnull
	@Override
	public String getName() {
		return "plethora";
	}

	@Nonnull
	@Override
	public String getUsage(@Nonnull ICommandSender sender) {
		return "dump|reload";
	}

	private String getUsage(String type) {
		switch (type) {
			case "dump":
				return "dump <name>";
			case "reload":
				return "reload";
			default:
				return "dump|reload";
		}
	}

	@Override
	public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) throws CommandException {
		if (args.length < 1 || Strings.isNullOrEmpty(args[0])) throw new CommandException(getUsage(sender));

		String type = args[0];
		if (type.equals("dump")) {
			if (args.length < 2 || Strings.isNullOrEmpty(args[1])) throw new CommandException(getUsage(type));

			String name = args[1];
			try {
				String extension = Files.getFileExtension(name);

				try (OutputStream file = new FileOutputStream(name)) {
					PrintStream writer = new PrintStream(file);

					IDocWriter docs;
					switch (extension) {
						case "json":
							docs = new JSONWriter(writer);
							break;
						case "html":
						case "htm":
							docs = new HTMLWriter(writer);
							break;
						default:
							throw new CommandException("Unknown extension '" + extension + "'. Please use html or json");
					}

					docs.writeHeader();
					docs.write(PlethoraAPI.instance().methodRegistry().getMethods());
					docs.writeFooter();
				}

				sender.sendMessage(new TextComponentString("Documentation written to " + name));
			} catch (Throwable e) {
				DebugLogger.error("Cannot handle " + name, e);
				throw new CommandException(e.toString());
			}
		} else if (type.equals("reload")) {
			ConfigCore.configuration.load();
			ConfigCore.sync();
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
