package org.squiddev.plethora.integration.astralsorcery;

import dan200.computercraft.api.lua.ILuaObject;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.block.network.BlockCollectorCrystalBase;
import hellfirepvp.astralsorcery.common.constellation.IMinorConstellation;
import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.item.ItemColoredLens;
import hellfirepvp.astralsorcery.common.item.block.ItemCollectorCrystal;
import hellfirepvp.astralsorcery.common.item.crystal.CrystalProperties;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.Constellations;
import hellfirepvp.astralsorcery.common.tile.*;
import hellfirepvp.astralsorcery.common.tile.network.TileCollectorCrystal;
import hellfirepvp.astralsorcery.common.tile.network.TileCrystalLens;
import net.minecraft.block.state.IBlockState;
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
import org.squiddev.plethora.api.method.TypedLuaObject;
import org.squiddev.plethora.integration.MetaWrapper;
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

	//REFINE Enable the examples if we find a way to work around Astral's `WorldNetworkHandler` logging errors
	// due to the tiles not being registered in the starlight network.
	// See TileNetwork.onFirstTick

	public static final IMetaProvider<TileWell> META_LIGHT_WELL = new BaseMetaProvider<TileWell>(
		"Provides the catalyst item in a Lightwell"
	) {

		@Nonnull
		@Override
		public Map<String, ?> getMeta(@Nonnull IPartialContext<TileWell> context) {
			//REFINE Do we want this stack wrapped, or should we just `getMeta`?
			ILuaObject stack = ContextHelpers.wrapStack(context, context.getTarget().getInventoryHandler().getStackInSlot(0));
			return stack != null
				? Collections.singletonMap("catalyst", stack)
				: Collections.emptyMap();
		}

		@Nullable
		@Override
		public TileWell getExample() {
			return null;
			/*TileWell well = new TileWell();
			setExampleBlockStateWithTile(BlocksAS.blockWell.getDefaultState(), well);

			well.getInventoryHandler().setStackInSlot(0, ItemCraftingComponent.MetaType.AQUAMARINE.asStack());
			return well;*/
		}
	};

	public static final IMetaProvider<TileCollectorCrystal> META_TILE_COLLECTOR_CRYSTAL = new BaseMetaProvider<TileCollectorCrystal>(
		"Provides the CrystalProperties, attunement, and crystal type for a Collector Crystal"
	) {

		@Nonnull
		@Override
		public Map<String, ?> getMeta(@Nonnull IPartialContext<TileCollectorCrystal> context) {
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
			return null;
			/*TileCollectorCrystal crystal = new TileCollectorCrystal();
			setExampleBlockStateWithTile(BlocksAS.collectorCrystal.getDefaultState(), crystal);

			crystal.onPlace(Constellations.discidia, Constellations.gelu, CrystalProperties.getMaxRockProperties(), true, BlockCollectorCrystalBase.CollectorCrystalType.ROCK_CRYSTAL);

			return crystal;*/
		}
	};

	public static final IMetaProvider<ItemStack> META_ITEM_COLLECTOR_CRYSTAL = new ItemStackContextMetaProvider<ItemCollectorCrystal>(
		ItemCollectorCrystal.class,
		"Provides the attunement and crystal type for a Collector Crystal in item form"
	) {
		@Nonnull
		@Override
		public Map<String, ?> getMeta(@Nonnull IPartialContext<ItemStack> context, @Nonnull ItemCollectorCrystal item) {
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

		@Nonnull
		@Override
		public ItemStack getExample() {
			ItemStack stack = new ItemStack(BlocksAS.collectorCrystal);
			CrystalProperties.applyCrystalProperties(stack, CrystalProperties.getMaxRockProperties());
			ItemCollectorCrystal.setType(stack, BlockCollectorCrystalBase.CollectorCrystalType.ROCK_CRYSTAL);

			ItemCollectorCrystal.setConstellation(stack, Constellations.discidia);
			ItemCollectorCrystal.setTraitConstellation(stack, Constellations.gelu);

			return stack;
		}
	};

	public static final IMetaProvider<TileCrystalLens> META_TILE_CRYSTAL_LENS = new BaseMetaProvider<TileCrystalLens>(
		"Provides the CrystalProperties and lens color of a Crystal Lens"
	) {

		@Nonnull
		@Override
		public Map<String, ?> getMeta(@Nonnull IPartialContext<TileCrystalLens> context) {
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
			return null;
			/*TileCrystalLens lens = new TileCrystalLens();
			setExampleBlockStateWithTile(BlocksAS.lens.getDefaultState(), lens);

			lens.onPlace(CrystalProperties.getMaxRockProperties());
			lens.setLensColor(ItemColoredLens.ColorType.SPECTRAL);

			return lens;*/
		}
	};

	public static final IMetaProvider<TileGrindstone> META_GRINDSTONE = new BaseMetaProvider<TileGrindstone>(
		"Provides the item currently on a Grindstone"
	) {

		@Nonnull
		@Override
		public Map<String, ?> getMeta(@Nonnull IPartialContext<TileGrindstone> context) {
			return Collections.singletonMap("item",
				context.makePartialChild(context.getTarget().getGrindingItem()).getMeta());
		}

		@Nullable
		@Override
		public TileGrindstone getExample() {
			return null;
			/*TileGrindstone grindstone = new TileGrindstone();
			setExampleBlockStateWithTile(BlocksAS.blockMachine.getDefaultState().withProperty(BlockMachine.MACHINE_TYPE, BlockMachine.MachineType.GRINDSTONE), grindstone);

			ItemStack stack = new ItemStack(ItemsAS.rockCrystal);
			CrystalProperties.applyCrystalProperties(stack, new CrystalProperties(CrystalProperties.MAX_SIZE_ROCK, 100, 90, 0, -1));

			grindstone.setGrindingItem(stack);

			return grindstone;*/
		}
	};

	public static final IMetaProvider<TileRitualPedestal> META_RITUAL_PEDESTAL = new BaseMetaProvider<TileRitualPedestal>(
		"Provides the focus crystal and work status of a Ritual Pedestal"
	) {

		@Nonnull
		@Override
		public Map<String, ?> getMeta(@Nonnull IPartialContext<TileRitualPedestal> context) {
			Map<String, Object> out = new HashMap<>(2);
			TileRitualPedestal target = context.getTarget();

			out.put("focus", context.makePartialChild(target.getInventoryHandler().getStackInSlot(0)).getMeta());

			//Ritual constellation(s) provided by the crystal's meta

			out.put("isWorking", target.isWorking());

			return out;
		}

		@Nullable
		@Override
		public TileRitualPedestal getExample() {
			return null;
			/*TileRitualPedestal pedestal = new TileRitualPedestal();
			setExampleBlockStateWithTile(BlocksAS.ritualPedestal.getDefaultState(), pedestal);

			ItemStack focus = new ItemStack(ItemsAS.tunedCelestialCrystal);
			CrystalProperties.applyCrystalProperties(focus, CrystalProperties.getMaxCelestialProperties());
			ItemTunedCrystalBase.applyMainConstellation(focus, Constellations.discidia);

			pedestal.placeCrystalIntoPedestal(focus);
			return pedestal;*/
		}
	};

	public static final IMetaProvider<TileIlluminator> META_CAVE_ILLUMINATOR = new BasicMetaProvider<TileIlluminator>(
		"Provides the flare color for a Cave Illuminator"
	) {

		@Nonnull
		@Override
		public Map<String, ?> getMeta(@Nonnull TileIlluminator context) {
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
			/*TileIlluminator illuminator = new TileIlluminator();
			setExampleBlockStateWithTile(BlocksAS.blockIlluminator.getDefaultState(), illuminator);

			illuminator.onWandUsed(EnumDyeColor.YELLOW);
			return illuminator;*/
		}
	};

	public static final IMetaProvider<TileAttunementRelay> META_SPECTRAL_RELAY = new BaseMetaProvider<TileAttunementRelay>(
		"Provides the item in a Spectral Relay"
	) {

		@Nonnull
		@Override
		public Map<String, ?> getMeta(@Nonnull IPartialContext<TileAttunementRelay> context) {
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
			/*TileAttunementRelay relay = new TileAttunementRelay();
			setExampleBlockStateWithTile(BlocksAS.attunementRelay.getDefaultState(), relay);

			relay.getInventoryHandler().insertItem(0, ItemCraftingComponent.MetaType.GLASS_LENS.asStack(), false);
			return relay;*/
		}
	};


	public static final IMetaProvider<TileMapDrawingTable> META_STELLAR_REFRACTION_TABLE = new BaseMetaProvider<TileMapDrawingTable>(
		"Provides the items in a Stellar Refraction Table"
	) {

		@Nonnull
		@Override
		public Map<String, ?> getMeta(@Nonnull IPartialContext<TileMapDrawingTable> context) {
			Map<String, Object> out = new HashMap<>(2);
			TileMapDrawingTable target = context.getTarget();

			TypedLuaObject<MetaWrapper<ItemStack>> lensStack = ContextHelpers.wrapStack(context, target.getSlotGlassLens());
			if (lensStack != null) {
				out.put("infusedGlass", lensStack);
			}

			TypedLuaObject<MetaWrapper<ItemStack>> inputStack = ContextHelpers.wrapStack(context, target.getSlotIn());
			if (inputStack != null) {
				out.put("processingSlot", inputStack);
			}

			return out;
		}

		@Nullable
		@Override
		public TileMapDrawingTable getExample() {
			return null;
			/*TileMapDrawingTable table = new TileMapDrawingTable();
			setExampleBlockStateWithTile(BlocksAS.drawingTable.getDefaultState(), table);

			table.addParchment(32);
			table.putGlassLens(new ItemStack(ItemsAS.infusedGlass));

			return table;*/
		}
	};

	//TODO Move this to a better location for utilization in other examples
	// Also, come up with a better name...
	//Not currently used, as the examples for Astral tiles are disabled, but likely to be important if we
	// add support for more mods that require the BlockState as well as the TileEntity for examples.
	public static void setExampleBlockStateWithTile(@Nonnull IBlockState state, @Nonnull TileEntity tile) {
		//Originally, this was going to instantiate the TileEntity internally,
		// but that would have required passing a Class<T> and calling `newInstance`,
		// which would have in turn created a LOT of extra overhead.
		WorldDummy.INSTANCE.setBlockState(BlockPos.ORIGIN, state);
		tile.setPos(BlockPos.ORIGIN);
		WorldDummy.INSTANCE.setTileEntity(BlockPos.ORIGIN, tile);
	}

}
