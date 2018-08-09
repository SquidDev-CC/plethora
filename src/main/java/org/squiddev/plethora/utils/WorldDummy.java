package org.squiddev.plethora.utils;

import net.minecraft.profiler.Profiler;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.*;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.SaveHandlerMP;
import net.minecraft.world.storage.WorldInfo;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("NullableProblems")
public class WorldDummy extends World {
	public static final WorldDummy INSTANCE = new WorldDummy();

	private WorldDummy() {
		super(
			new SaveHandlerMP(),
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
}
