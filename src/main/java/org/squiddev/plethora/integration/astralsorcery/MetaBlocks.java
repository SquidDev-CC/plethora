package org.squiddev.plethora.integration.astralsorcery;

import dan200.computercraft.api.lua.ILuaObject;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.block.network.BlockCollectorCrystalBase;
import hellfirepvp.astralsorcery.common.constellation.IMinorConstellation;
import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.item.ItemColoredLens;
import hellfirepvp.astralsorcery.common.item.ItemCraftingComponent;
import hellfirepvp.astralsorcery.common.item.block.ItemCollectorCrystal;
import hellfirepvp.astralsorcery.common.item.crystal.CrystalProperties;
import hellfirepvp.astralsorcery.common.item.crystal.base.ItemTunedCrystalBase;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.Constellations;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import hellfirepvp.astralsorcery.common.tile.*;
import hellfirepvp.astralsorcery.common.tile.network.TileCollectorCrystal;
import hellfirepvp.astralsorcery.common.tile.network.TileCrystalLens;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.BaseMetaProvider;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.api.meta.ItemStackContextMetaProvider;
import org.squiddev.plethora.api.method.ContextHelpers;
import org.squiddev.plethora.api.method.IPartialContext;
import org.squiddev.plethora.utils.WorldDummy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

// Despite the name, this class also houses the providers for TileEntities, and their item forms.
@Injects(AstralSorcery.MODID)
public final class MetaBlocks {
	private MetaBlocks() {
	}

	public static final IMetaProvider<TileWell> META_LIGHT_WELL = new BaseMetaProvider<TileWell>(
		"Provides the catalyst item in this Lightwell"
	) {

		@Nonnull
		@Override
		public Map<String, Object> getMeta(@Nonnull IPartialContext<TileWell> context) {
			//REFINE Do we want this stack wrapped, or should we just `getMeta`?
			ILuaObject stack = ContextHelpers.wrapStack(context, context.getTarget().getInventoryHandler().getStackInSlot(0));
			return stack != null
				? Collections.singletonMap("catalyst", stack)
				: Collections.emptyMap();
		}

		@Nonnull
		@Override
		public TileWell getExample() {
			TileWell well = new TileWell();
			well.getInventoryHandler().setStackInSlot(0,
				new ItemStack(ItemsAS.craftingComponent, 1, ItemCraftingComponent.MetaType.AQUAMARINE.getMeta()));
			return well;
		}
	};

	public static final IMetaProvider<TileCollectorCrystal> META_TILE_COLLECTOR_CRYSTAL = new BaseMetaProvider<TileCollectorCrystal>() {

		@Nonnull
		@Override
		public Map<String, Object> getMeta(@Nonnull IPartialContext<TileCollectorCrystal> context) {
			Map<String, Object> out = new HashMap<>(4);
			TileCollectorCrystal target = context.getTarget();

			out.putAll(context.makePartialChild(target.getCrystalProperties()).getMeta());

			IWeakConstellation mainConstellation = target.getConstellation();
			if (mainConstellation != null) {
				out.put("constellation", context.makePartialChild(mainConstellation).getMeta());
			}

			IMinorConstellation traitConstellation = target.getTrait();
			if (traitConstellation != null) {
				out.put("traitConstellation", context.makePartialChild(traitConstellation).getMeta());
			}

			out.put("crystalType", target.getType().name());

			return out;
		}

		@Nullable
		@Override
		public TileCollectorCrystal getExample() {
			WorldDummy.INSTANCE.setBlockState(BlockPos.ORIGIN, BlocksAS.collectorCrystal.getDefaultState());
			TileEntity te = WorldDummy.INSTANCE.getTileEntity(BlockPos.ORIGIN);
			if (!(te instanceof TileCollectorCrystal)) return null;

			TileCollectorCrystal crystal = (TileCollectorCrystal) te;
			crystal.onPlace(Constellations.discidia, Constellations.gelu, CrystalProperties.getMaxRockProperties(), true, BlockCollectorCrystalBase.CollectorCrystalType.ROCK_CRYSTAL);

			return crystal;
		}
	};

	public static final IMetaProvider<ItemStack> META_ITEM_COLLECTOR_CRYSTAL = new ItemStackContextMetaProvider<ItemCollectorCrystal>(
		ItemCollectorCrystal.class,
		"FIXME Set the description"
	) {
		@Nonnull
		@Override
		public Map<String, Object> getMeta(@Nonnull IPartialContext<ItemStack> context, @Nonnull ItemCollectorCrystal item) {
			Map<String, Object> out = new HashMap<>(3);
			ItemStack stack = context.getTarget();

			//CrystalProperties of the item form are already exposed
			IWeakConstellation mainConstellation = ItemCollectorCrystal.getConstellation(stack);
			if (mainConstellation != null) {
				out.put("constellation", context.makePartialChild(mainConstellation).getMeta());
			}

			IMinorConstellation traitConstellation = ItemCollectorCrystal.getTrait(stack);
			if (traitConstellation != null) {
				out.put("traitConstellation", context.makePartialChild(traitConstellation).getMeta());
			}

			out.put("crystalType", ItemCollectorCrystal.getType(stack).name());

			return out;
		}

		@Nullable
		@Override
		public ItemStack getExample() {
			ItemStack stack = new ItemStack(BlocksAS.collectorCrystal);
			CrystalProperties.applyCrystalProperties(stack, CrystalProperties.getMaxRockProperties());
			ItemCollectorCrystal.setType(stack, BlockCollectorCrystalBase.CollectorCrystalType.ROCK_CRYSTAL);

			//REFINE Do we want to add null checks to the `Constellations` field access?
			ItemCollectorCrystal.setConstellation(stack, Constellations.discidia);
			ItemCollectorCrystal.setTraitConstellation(stack, Constellations.gelu);

			return stack;
		}
	};

	public static final IMetaProvider<TileCrystalLens> META_TILE_CRYSTAL_LENS = new BaseMetaProvider<TileCrystalLens>() {

		@Nonnull
		@Override
		public Map<String, Object> getMeta(@Nonnull IPartialContext<TileCrystalLens> context) {
			Map<String, Object> out = new HashMap<>(3);
			TileCrystalLens target = context.getTarget();

			out.putAll(context.makePartialChild(target.getCrystalProperties()).getMeta());

			ItemColoredLens.ColorType lens = target.getLensColor();
			String colorName = lens != null ? lens.getUnlocalizedName() : null;

			if (colorName != null) {
				out.put("lensColor", colorName);
				out.put("lensColour", colorName);
			}

			return out;
		}

		@Nullable
		@Override
		public TileCrystalLens getExample() {
			WorldDummy.INSTANCE.setBlockState(BlockPos.ORIGIN, BlocksAS.lens.getDefaultState());
			TileEntity te = WorldDummy.INSTANCE.getTileEntity(BlockPos.ORIGIN);
			if (!(te instanceof TileCrystalLens)) return null;

			TileCrystalLens lens = (TileCrystalLens) te;
			lens.onPlace(CrystalProperties.getMaxRockProperties());
			lens.setLensColor(ItemColoredLens.ColorType.SPECTRAL);

			return lens;
		}
	};

	public static final IMetaProvider<TileGrindstone> META_GRINDSTONE = new BaseMetaProvider<TileGrindstone>(
		"Provides the item currently on this Grindstone"
	) {

		@Nonnull
		@Override
		public Map<String, Object> getMeta(@Nonnull IPartialContext<TileGrindstone> context) {
			return Collections.singletonMap("item",
				context.makePartialChild(context.getTarget().getGrindingItem()).getMeta());
		}

		@Nullable
		@Override
		public TileGrindstone getExample() {
			//FIXME Implement this example
			// Since `setGrindingItem` updates the BlockState, we will have to create the block for the TE too
			return null;
		}
	};

	public static final IMetaProvider<TileRitualPedestal> META_RITUAL_PEDESTAL = new BaseMetaProvider<TileRitualPedestal>() {

		@Nonnull
		@Override
		public Map<String, Object> getMeta(@Nonnull IPartialContext<TileRitualPedestal> context) {
			Map<String, Object> out = new HashMap<>(2);
			TileRitualPedestal target = context.getTarget();

			out.put("focus", context.makePartialChild(target.getInventoryHandler().getStackInSlot(0)).getMeta());

			//Ritual constellation(s) provided by the crystal's meta

			out.put("isWorking", target.isWorking());

			return out;
		}

		@Nonnull
		@Override
		public TileRitualPedestal getExample() {
			//FIXME This probably needs a Block set too...
			TileRitualPedestal tile = new TileRitualPedestal();
			ItemStack focus = new ItemStack(ItemsAS.tunedCelestialCrystal);
			CrystalProperties.applyCrystalProperties(focus, CrystalProperties.getMaxCelestialProperties());
			ItemTunedCrystalBase.applyMainConstellation(focus, Constellations.discidia);

			tile.placeCrystalIntoPedestal(focus);
			return tile;
		}
	};

	public static final IMetaProvider<TileIlluminator> META_CAVE_ILLUMINATOR = new BasicMetaProvider<TileIlluminator>() {

		@Nonnull
		@Override
		public Map<String, Object> getMeta(@Nonnull TileIlluminator context) {
			NBTTagCompound nbt = new NBTTagCompound();
			context.writeCustomNBT(nbt);

			if (!nbt.hasKey("wandColor", Constants.NBT.TAG_INT)) return Collections.emptyMap();

			Map<String, Object> out = new HashMap<>(2);

			EnumDyeColor color = EnumDyeColor.byMetadata(nbt.getInteger("wandColor"));
			out.put("flareColor", color.toString());
			out.put("flareColour", color.toString());

			return out;
		}

		@Nullable
		@Override
		public TileIlluminator getExample() {
			return null;
		}
	};

	public static final IMetaProvider<TileAttunementRelay> META_SPECTRAL_RELAY = new BaseMetaProvider<TileAttunementRelay>() {

		@Nonnull
		@Override
		public Map<String, Object> getMeta(@Nonnull IPartialContext<TileAttunementRelay> context) {
			// While it also functions as a starlight collector, and is part of attunement and top-tier altar crafting,
			// the stored item is the only thing that we can easily access.
			// We _could_ extract the linked BlockPos and check the type of TileEntity there, but... meh.
			return Collections.singletonMap("item",
				context.makePartialChild(context.getTarget().getInventoryHandler().getStackInSlot(0)).getMeta());
		}

		@Nullable
		@Override
		public TileAttunementRelay getExample() {
			return null;
		}
	};


	public static final IMetaProvider<TileMapDrawingTable> META_STELLAR_REFRACTION_TABLE = new BaseMetaProvider<TileMapDrawingTable>() {

		@Nonnull
		@Override
		public Map<String, Object> getMeta(@Nonnull IPartialContext<TileMapDrawingTable> context) {
			Map<String, Object> out = new HashMap<>(2);
			TileMapDrawingTable target = context.getTarget();

			//TODO Discuss the difference between `getMeta` and `wrapStack` with Squid, regarding behavior of empty stacks
			// `getMeta` appears to result in an empty table, while `wrapStack` returns `null`
			//noinspection ConstantConditions
			out.put("infusedGlass", ContextHelpers.wrapStack(context, target.getSlotGlassLens()));
			//noinspection ConstantConditions
			out.put("processingSlot", ContextHelpers.wrapStack(context, target.getSlotIn()));

			return out;
		}

		@Nullable
		@Override
		public TileMapDrawingTable getExample() {
			return null;
		}
	};


}
