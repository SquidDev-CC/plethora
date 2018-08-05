package org.squiddev.plethora.core;

import com.google.common.base.Strings;
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

public class CommandPlethora extends CommandBase {
	@Nonnull
	@Override
	public String getName() {
		return "plethora";
	}

	@Nonnull
	@Override
	public String getUsage(@Nonnull ICommandSender sender) {
		return getUsage("");
	}

	private String getUsage(String type) {
		switch (type) {
			case "dump":
				return "dump <name>";
			case "reload":
				return "reload";
			default:
				return "dump | reload";
		}
	}

	@Override
	public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) throws CommandException {
		if (args.length < 1) throw new CommandException(getUsage(sender));

		String type = args[0];
		if (type.equals("dump")) {
			if (args.length < 2 || Strings.isNullOrEmpty(args[1])) throw new CommandException(getUsage(type));

			String name = args[1];
			try {
				IDocWriter initialWriter;
				if (name.endsWith(".json")) {
					initialWriter = new JSONWriter(new FileOutputStream(name));
				} else if (name.endsWith(".raw.html") || name.endsWith(".raw.htm")) {
					initialWriter = new HTMLWriter(false, new FileOutputStream(name));
				} else if (name.endsWith(".html") || name.endsWith(".htm")) {
					initialWriter = new HTMLWriter(true, new FileOutputStream(name));
				} else {
					throw new CommandException("Cannot generate documentation for '" + name + "'. The file must be suffixed with '.json', '.html' or '.raw.html'");
				}

				try (IDocWriter writer = initialWriter) {
					writer.writeHeader();
					writer.write(PlethoraAPI.instance().methodRegistry().getMethods());
					writer.writeFooter();
				}

				sender.sendMessage(new TextComponentString("Documentation written to " + name));
			} catch (CommandException e) {
				throw e;
			} catch (Exception e) {
				DebugLogger.error("Cannot handle " + name, e);
				throw new CommandException(e.toString());
			}
		} else if (type.equals("reload")) {
			ConfigCore.configuration.load();
			ConfigCore.sync();
		} else {
			throw new CommandException(getUsage(""));
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
