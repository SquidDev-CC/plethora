package org.squiddev.plethora.utils;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.*;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraft.world.storage.IPlayerFileData;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;

import javax.annotation.Nullable;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("NullableProblems")
public class WorldDummy extends World {
	public static final WorldDummy INSTANCE = new WorldDummy();

	private WorldDummy() {
		super(
			new SaveHandler(),
			new WorldInfo(new WorldSettings(0, GameType.SPECTATOR, false, false, WorldType.FLAT), "dummy"),
			new WorldProvider() {

				@Override
				public DimensionType getDimensionType() {
					return DimensionType.OVERWORLD;
				}
			},
			new Profiler(),
			false
		);

		provider.setWorld(this);
		chunkProvider = createChunkProvider();
	}

	@Override
	protected IChunkProvider createChunkProvider() {
		return new ChunkProvider();
	}

	@Override
	protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
		return true;
	}

	private final class ChunkProvider implements IChunkProvider {
		private final Map<ChunkPos, Chunk> chunks = new HashMap<>();

		@Nullable
		@Override
		public Chunk getLoadedChunk(int x, int z) {
			ChunkPos chunkPos = new ChunkPos(x, z);
			if (!chunks.containsKey(chunkPos)) {
				chunks.put(chunkPos, new Chunk(WorldDummy.this, x, z) {
					@Override
					public void generateSkylightMap() {
					}
				});
			}
			return chunks.get(chunkPos);
		}

		@Override
		public Chunk provideChunk(int x, int z) {
			return getLoadedChunk(x, z);
		}

		@Override
		public boolean tick() {
			return false;
		}

		@Override
		public String makeString() {
			return "dummy";
		}

		@Override
		public boolean isChunkGeneratedAt(int x, int z) {
			return true;
		}
	}

	public static class SaveHandler implements ISaveHandler {
		@Override
		public WorldInfo loadWorldInfo() {
			return null;
		}

		@Override
		public void checkSessionLock() {
		}

		@Override
		public IChunkLoader getChunkLoader(WorldProvider provider) {
			return null;
		}

		@Override
		public void saveWorldInfoWithPlayer(WorldInfo worldInformation, NBTTagCompound tagCompound) {
		}

		@Override
		public void saveWorldInfo(WorldInfo worldInformation) {
		}

		@Override
		public IPlayerFileData getPlayerNBTManager() {
			return null;
		}

		@Override
		public void flush() {
		}

		@Override
		public File getMapFileFromName(String mapName) {
			return null;
		}

		@Override
		public File getWorldDirectory() {
			return null;
		}

		@Override
		public TemplateManager getStructureTemplateManager() {
			return null;
		}
	}
}
