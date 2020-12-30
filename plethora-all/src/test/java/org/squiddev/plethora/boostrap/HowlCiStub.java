package org.squiddev.plethora.boostrap;

import dan200.computercraft.api.filesystem.IFileSystem;
import dan200.computercraft.api.lua.IComputerSystem;
import dan200.computercraft.api.lua.ILuaAPI;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.core.filesystem.FileMount;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.apache.logging.log4j.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;

import static dan200.computercraft.api.lua.ArgumentHelper.getString;

/**
 * Mimics parts of the howlci library, or at least those required by mcfly.
 */
public class HowlCiStub implements ILuaAPI {
	private final IComputerSystem computer;

	HowlCiStub(IComputerSystem computer) {
		this.computer = computer;
	}

	@Override
	public String[] getNames() {
		return new String[]{"howlci"};
	}

	@Nonnull
	@Override
	public String[] getMethodNames() {
		return new String[]{"log", "status"};
	}

	@Override
	public void startup() {
		IFileSystem fs = computer.getFileSystem();
		if (fs == null) {
			PlethoraTestMod.LOG.error("Cannot find filesystem");
			return;
		}

		try {
			File testRom = new File("../../src/test/resources/assets/plethora-test/test-rom").getCanonicalFile();
			if (!testRom.isDirectory()) {
				PlethoraTestMod.LOG.error("Cannot find test-rom (looked at {}", testRom);
				return;
			}

			computer.mount("test-rom", new FileMount(testRom, 0L));

			if (fs.exists("startup.lua")) fs.delete("startup.lua");
			fs.copy("test-rom/startup.lua", "startup.lua");
		} catch (IOException e) {
			PlethoraTestMod.LOG.error("Cannot create startup file", e);
		}

		PlethoraTestMod.LOG.info("Setup test-rom");
	}

	@Nullable
	@Override
	public Object[] callMethod(@Nonnull ILuaContext context, int method, @Nonnull Object[] arguments) throws LuaException, InterruptedException {
		switch (method) {
			case 0: { // log
				String level = getString(arguments, 0);
				String message = getString(arguments, 1);
				PlethoraTestMod.LOG.log(getEnum(level, Level.INFO, Level.values()), message);
				return null;
			}
			case 1: { // status
				String status = getString(arguments, 0);
				String test = getString(arguments, 1);

				FMLCommonHandler.instance().getMinecraftServerInstance()
					.sendMessage(getEnum(status, Status.PENDING).format(test));
				return null;
			}
			default:
				return null;
		}
	}

	private static <T extends Enum<T>> T getEnum(String name, T def) {
		return getEnum(name, def, def.getDeclaringClass().getEnumConstants());
	}

	private static <T> T getEnum(String name, T def, T[] values) {
		for (T value : values) {
			if (value.toString().equalsIgnoreCase(name)) return value;
		}
		return def;
	}

	private enum Status {
		PENDING(TextFormatting.YELLOW),
		ERROR(TextFormatting.DARK_PURPLE),
		FAIL(TextFormatting.RED),
		PASS(TextFormatting.GREEN);

		private final TextFormatting colour;

		Status(TextFormatting colour) {
			this.colour = colour;
		}

		public ITextComponent format(String string) {
			TextComponentString component = new TextComponentString(string);
			component.getStyle().setColor(colour);
			return component;
		}
	}
}
