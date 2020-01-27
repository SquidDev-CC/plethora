package org.squiddev.plethora.core;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import org.apache.commons.io.IOUtils;
import org.squiddev.plethora.core.docdump.HTMLWriter;
import org.squiddev.plethora.core.docdump.IDocWriter;
import org.squiddev.plethora.core.docdump.JSONWriter;

import javax.annotation.Nonnull;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Objects;

public class CommandPlethora extends CommandBase {
	private static final String USAGE = "dump | reload";
	private static final String USAGE_DUMP = "dump [--raw] <name>";
	private static final String USAGE_RELOAD = "reload";

	@Nonnull
	@Override
	public String getName() {
		return "plethora";
	}

	@Nonnull
	@Override
	public String getUsage(@Nonnull ICommandSender sender) {
		return USAGE;
	}

	@Override
	public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) throws CommandException {
		String type = getArg(args, 0);
		if (Objects.equals(type, "dump")) {
			boolean raw = Objects.equals(getArg(args, 1), "--raw");
			String name = getArg(args, raw ? 2 : 1);
			if (name == null) throw new CommandException(USAGE_DUMP);

			OutputStream stream = null;
			try {
				IDocWriter initialWriter;
				if (name.endsWith(".json")) {
					initialWriter = new JSONWriter(stream = new FileOutputStream(name), MethodRegistry.instance.providers, MetaRegistry.instance.providers);
				} else if (name.endsWith(".html") || name.endsWith(".htm")) {
					initialWriter = new HTMLWriter(stream = new FileOutputStream(name), MethodRegistry.instance.providers, MetaRegistry.instance.providers);
				} else {
					throw new CommandException("Cannot generate documentation for '" + name + "'. The file must be suffixed with '.json', '.html'");
				}

				try (IDocWriter writer = initialWriter) {
					if (!raw) writer.writeHeader();
					writer.write();
					if (!raw) writer.writeFooter();
				}

				sender.sendMessage(new TextComponentString("Documentation written to " + name));
			} catch (CommandException e) {
				throw e;
			} catch (Exception e) {
				if (stream != null) IOUtils.closeQuietly(stream);
				PlethoraCore.LOG.error("Cannot handle " + name, e);
				throw new CommandException(e.toString());
			}
		} else if (Objects.equals(type, "reload")) {
			ConfigCore.configuration.load();
			ConfigCore.sync();
			PlethoraCore.buildRegistries();
			sender.sendMessage(new TextComponentString("Config reloaded"));
		} else {
			throw new CommandException(USAGE);
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

	private static String getArg(String[] args, int i) {
		return args.length > i ? args[i] : null;
	}
}
