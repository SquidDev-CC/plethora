package org.squiddev.plethora.integration.vanilla.meta;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.WeightedSpawnerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.BaseMetaProvider;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.api.method.ContextKeys;
import org.squiddev.plethora.api.method.IPartialContext;
import org.squiddev.plethora.integration.ItemEntityStorageMetaProvider;
import org.squiddev.plethora.utils.Helpers;
import org.squiddev.plethora.utils.TypedField;
import org.squiddev.plethora.utils.WorldDummy;

import javax.annotation.Nonnull;
import java.util.*;

@Injects
public final class VanillaMeta {
	private VanillaMeta() {
	}

	public static final IMetaProvider<TileEntitySign> TILE_SIGN = new BasicMetaProvider<TileEntitySign>(
		"Provides the text upon the sign."
	) {
		@Nonnull
		@Override
		public Map<String, ?> getMeta(@Nonnull TileEntitySign target) {
			return Collections.singletonMap("lines", getSignLines(target));
		}

		@Nonnull
		@Override
		public TileEntitySign getExample() {
			TileEntitySign sign = new TileEntitySign();
			sign.signText[0] = new TextComponentString("This is");
			sign.signText[1] = new TextComponentString("my rather fancy");
			sign.signText[2] = new TextComponentString("sign.");
			return sign;
		}
	};

	public static Map<Integer, String> getSignLines(TileEntitySign sign) {
		ITextComponent[] lines = sign.signText;
		Map<Integer, String> text = new HashMap<>(lines.length);
		for (int i = 0; i < lines.length; i++) text.put(i + 1, lines[i].getUnformattedText());
		return text;
	}

	public static final IMetaProvider<MobSpawnerBaseLogic> SPAWNER_LOGIC = new BaseMetaProvider<MobSpawnerBaseLogic>(
		"Provides the entities that this spawner will spawn."
	) {
		private final TypedField<MobSpawnerBaseLogic, List<WeightedSpawnerEntity>> FIELD_POTENTIAL =
			TypedField.of(MobSpawnerBaseLogic.class, "potentialSpawns", "field_98285_e");

		private final TypedField<MobSpawnerBaseLogic, WeightedSpawnerEntity> FIELD_DATA =
			TypedField.of(MobSpawnerBaseLogic.class, "spawnData", "field_98282_f");

		@Nonnull
		@Override
		public Map<String, List<Map<String, ?>>> getMeta(@Nonnull IPartialContext<MobSpawnerBaseLogic> context) {
			List<WeightedSpawnerEntity> potentialSpawns = FIELD_POTENTIAL.get(context.getTarget());
			WeightedSpawnerEntity spawnData = FIELD_DATA.get(context.getTarget());
			if (potentialSpawns == null && spawnData == null) return Collections.emptyMap();

			if (potentialSpawns == null || potentialSpawns.isEmpty()) {
				potentialSpawns = Collections.singletonList(spawnData);
			}

			IWorldLocation location = context.getContext(ContextKeys.ORIGIN, IWorldLocation.class);
			return Collections.singletonMap("spawnedEntities", Helpers.map(potentialSpawns, entry -> {
				if (location != null) {
					Vec3d pos = location.getLoc();
					Entity entity = AnvilChunkLoader.readWorldEntityPos(entry.getNbt(), location.getWorld(), pos.x, pos.y, pos.z, false);
					if (entity != null) return context.makePartialChild(entity).getMeta();
				}

				return ItemEntityStorageMetaProvider.getBasicDetails(entry.getNbt());
			}));
		}

		@Nonnull
		@Override
		public MobSpawnerBaseLogic getExample() {
			MobSpawnerBaseLogic logic = new MobSpawnerBaseLogic() {
				@Override
				public void broadcastEvent(int id) {
				}

				@Nonnull
				@Override
				public World getSpawnerWorld() {
					return WorldDummy.INSTANCE;
				}

				@Nonnull
				@Override
				public BlockPos getSpawnerPosition() {
					return BlockPos.ORIGIN;
				}
			};
			logic.setEntityId(new ResourceLocation("minecraft", "squid"));
			return logic;
		}
	};

	public static final IMetaProvider<ItemStack> ENCHANTED_ITEM = new BasicMetaProvider<ItemStack>(
		"Provides the enchantments on an item"
	) {
		@Nonnull
		@Override
		public Map<String, ?> getMeta(@Nonnull ItemStack target) {
			Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(target);
			if (enchants.isEmpty()) return Collections.emptyMap();

			return Collections.singletonMap("enchantments", enchants.entrySet().stream()
				.filter(Objects::nonNull)
				.map(entry -> {
					Enchantment enchantment = entry.getKey();
					int level = entry.getValue();
					HashMap<String, Object> enchant = new HashMap<>(3);
					enchant.put("name", enchantment.getName());
					enchant.put("level", level);
					enchant.put("fullName", enchantment.getTranslatedName(level));

					return enchant;
				}).collect(Helpers.tinyCollect()));
		}

		@Nonnull
		@Override
		public ItemStack getExample() {
			ItemStack stack = new ItemStack(Items.DIAMOND_HOE);
			EnchantmentHelper.setEnchantments(Collections.singletonMap(Enchantments.UNBREAKING, 5), stack);
			return stack;
		}
	};

	public static final IMetaProvider<IEnergyStorage> ENERGY = new BasicMetaProvider<IEnergyStorage>(
		"Provides the currently stored energy and capacity of a Forge Energy cell"
	) {
		@Nonnull
		@Override
		public Map<String, ?> getMeta(@Nonnull IEnergyStorage handler) {
			Map<String, Object> out = new HashMap<>(2);
			out.put("stored", handler.getEnergyStored());
			out.put("capacity", handler.getMaxEnergyStored());
			return Collections.singletonMap("energy", out);
		}

		@Nonnull
		@Override
		public IEnergyStorage getExample() {
			return new EnergyStorage(50000, 100, 100, 1000);
		}
	};
}
