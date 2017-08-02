package org.squiddev.plethora.integration.vanilla.method;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.block.BlockNote;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.method.MethodResult;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.api.module.SubtargetedModuleMethod;
import org.squiddev.plethora.integration.vanilla.IntegrationVanilla;

import java.util.List;

import static dan200.computercraft.core.apis.ArgumentHelper.*;
import static org.squiddev.plethora.api.method.ArgumentHelper.assertBetween;

/**
 * Various methods for playing sounds in world
 */
public class MethodsNoteblock {
	private static List<SoundEvent> instruments;

	private static List<SoundEvent> getInstruments() {
		if (instruments == null) {
			instruments = ObfuscationReflectionHelper.getPrivateValue(BlockNote.class, null, "field_176434_a");
		}

		return instruments;
	}

	private static SoundEvent getInstrument(String name) {
		ResourceLocation id = new ResourceLocation(name);

		for (SoundEvent sound : instruments) {
			if (sound.getRegistryName().equals(id)) {
				return sound;
			}
		}

		return null;
	}

	@SubtargetedModuleMethod.Inject(
		module = IntegrationVanilla.noteblock, target = IWorldLocation.class,
		doc = "function(instrument:string|number, pitch:number[, volume:number]) -- Plays a note block note"
	)
	public static MethodResult playNote(final IUnbakedContext<IModuleContainer> context, Object[] arguments) throws LuaException {
		List<SoundEvent> instruments = getInstruments();

		final SoundEvent sound;
		if (arguments.length == 0) {
			throw badArgument(0, "string|number", "no value");
		} else if (arguments[0] instanceof Number) {
			int instrument = ((Number) arguments[0]).intValue();
			assertBetween(instrument, 0, instruments.size() - 1, "Instrument out of bounds (%s)");

			sound = instruments.get(instrument);
		} else if (arguments[0] instanceof String) {
			String name = (String) arguments[0];
			sound = getInstrument("block.note." + name);
			if (sound == null) throw new LuaException("Unknown instrument '" + name + "'");
		} else {
			throw badArgument(0, "string|number", arguments[0]);
		}

		final int pitch = getInt(arguments, 1);
		final float volume = (float) optNumber(arguments, 2, 3);

		assertBetween(pitch, 0, 24, "Pitch out of bounds (%s)");
		assertBetween(volume, 0.1, 5, "Volume out of bounds (%s)");

		final float adjPitch = (float) Math.pow(2d, (double) (pitch - 12) / 12d);

		context.safeBake();
		context.getExecutor().executeAsync(MethodResult.nextTick(() -> {
			IWorldLocation location = context.bake().getContext(IWorldLocation.class);
			BlockPos pos = location.getPos();
			Vec3d vec = location.getLoc();

			World world = location.getWorld();
			world.playSound(null, pos, sound, SoundCategory.RECORDS, volume, adjPitch);
			if (world instanceof WorldServer) {
				((WorldServer) world).spawnParticle(EnumParticleTypes.NOTE, false, vec.x, vec.y, vec.z, 0, pitch / 24.0, 0, 0, 1.0);
			}
			return MethodResult.empty();
		}));

		return MethodResult.empty();
	}

	@SubtargetedModuleMethod.Inject(
		module = IntegrationVanilla.noteblock, target = IWorldLocation.class,
		doc = "function(sound:string[, pitch:number][, volume:number]) -- Play a sound"
	)
	public static MethodResult playSound(final IUnbakedContext<IModuleContainer> context, Object[] arguments) throws LuaException {
		final String name = getString(arguments, 0);
		final float pitch = (float) optNumber(arguments, 1, 0);
		final float volume = (float) optNumber(arguments, 2, 1);

		assertBetween(pitch, 0, 2, "Pitch out of bounds (%s)");
		assertBetween(volume, 0.1, 5, "Volume out of bounds (%s)");

		final SoundEvent sound = SoundEvent.REGISTRY.getObject(new ResourceLocation(name));
		if (sound == null) throw new LuaException("No such sound '" + name + "'");

		context.safeBake();
		context.getExecutor().executeAsync(MethodResult.nextTick(() -> {
			IWorldLocation location = context.bake().getContext(IWorldLocation.class);
			BlockPos pos = location.getPos();

			location.getWorld().playSound(null, pos, sound, SoundCategory.RECORDS, volume, pitch);
			return MethodResult.empty();
		}));

		return MethodResult.empty();
	}
}
