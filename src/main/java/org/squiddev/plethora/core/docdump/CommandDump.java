package org.squiddev.plethora.core.docdump;

import com.google.common.base.Strings;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import org.squiddev.plethora.api.PlethoraAPI;
import org.squiddev.plethora.utils.DebugLogger;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

public class CommandDump extends CommandBase {
	private final boolean restricted;

	public CommandDump(boolean restricted) {
		this.restricted = restricted;
	}

	@Override
	public String getCommandName() {
		return "plethora_dump";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "plethora_dump [name]";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
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
	public boolean canCommandSenderUseCommand(ICommandSender p_canCommandSenderUseCommand_1_) {
		return !restricted || super.canCommandSenderUseCommand(p_canCommandSenderUseCommand_1_);
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 4;
	}
}
