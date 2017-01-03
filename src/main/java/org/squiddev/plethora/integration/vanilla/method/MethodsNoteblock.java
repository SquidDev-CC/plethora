package org.squiddev.plethora.integration.vanilla.method;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.block.BlockNote;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.method.MethodResult;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.api.module.SubtargetedModuleMethod;
import org.squiddev.plethora.integration.vanilla.IntegrationVanilla;

import java.util.List;
import java.util.concurrent.Callable;

import static org.squiddev.plethora.api.method.ArgumentHelper.*;

/**
 * Various methods for playing sounds in world
 */
public class MethodsNoteblock {
	private static List<String> instruments;

	private static List<String> getInstruments() {
		if (instruments == null) {
			instruments = ObfuscationReflectionHelper.getPrivateValue(BlockNote.class, null, "field_176434_a");
		}

		return instruments;
	}

	@SubtargetedModuleMethod.Inject(
		module = IntegrationVanilla.noteblock, target = IWorldLocation.class,
		doc = "function(instrument:string|number, pitch:number[, volumne:number]) -- Plays a note block note"
	)
	public static MethodResult playNote(final IUnbakedContext<IModuleContainer> context, Object[] arguments) throws LuaException {
		List<String> instruments = getInstruments();

		final String name;
		if (arguments.length == 0) {
			throw badArgument(null, 0, "string|number");
		} else if (arguments[0] instanceof Number) {
			int instrument = ((Number) arguments[0]).intValue();
			assertBetween(instrument, 0, instruments.size() - 1, "Instrument out of bounds (%s)");

			name = instruments.get(instrument);
		} else if (arguments[0] instanceof String) {
			name = (String) arguments[0];
			if (!(instruments.contains(name))) {
				throw new LuaException("Unknown instrument '" + name + "'");
			}
		} else {
			throw badArgument(arguments[0], 0, "string|number");
		}

		int pitch = getInt(arguments, 1);
		final float volume = (float) optNumber(arguments, 2, 3);

		assertBetween(pitch, 0, 24, "Pitch out of bounds (%s)");
		assertBetween(volume, 0.1, 5, "Volume out of bounds (%s)");

		final float adjPitch = (float) Math.pow(2d, (double) (pitch - 12) / 12d);

		return MethodResult.nextTick(new Callable<MethodResult>() {
			@Override
			public MethodResult call() throws Exception {
				IWorldLocation location = context.bake().getContext(IWorldLocation.class);
				Vec3 pos = location.getLoc();

				location.getWorld().playSoundEffect(pos.xCoord, pos.yCoord, pos.zCoord, "note." + name, volume, adjPitch);
				return MethodResult.empty();
			}
		});
	}

	@SubtargetedModuleMethod.Inject(
		module = IntegrationVanilla.noteblock, target = IWorldLocation.class,
		doc = "function(sound:string[, pitch:number][, volume:number]) -- Play a sound"
	)
	public static MethodResult playSound(final IUnbakedContext<IModuleContainer> context, Object[] arguments) throws LuaException {
		final String sound = getString(arguments, 0);
		final float pitch = (float) optNumber(arguments, 1, 0);
		final float volume = (float) optNumber(arguments, 2, 1);

		assertBetween(pitch, 0, 2, "Pitch out of bounds (%s)");
		assertBetween(volume, 0.1, 5, "Volume out of bounds (%s)");

		return MethodResult.nextTick(new Callable<MethodResult>() {
			@Override
			public MethodResult call() throws Exception {
				IWorldLocation location = context.bake().getContext(IWorldLocation.class);
				Vec3 pos = location.getLoc();

				location.getWorld().playSoundEffect(pos.xCoord, pos.yCoord, pos.zCoord, sound, volume, pitch);
				return MethodResult.empty();
			}
		});
	}
}
