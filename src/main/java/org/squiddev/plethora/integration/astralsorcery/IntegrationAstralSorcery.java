package org.squiddev.plethora.integration.astralsorcery;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.constellation.*;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ResearchManager;
import hellfirepvp.astralsorcery.common.data.research.ResearchProgression;
import hellfirepvp.astralsorcery.common.item.ItemColoredLens;
import hellfirepvp.astralsorcery.common.item.ItemConstellationPaper;
import hellfirepvp.astralsorcery.common.item.ItemJournal;
import hellfirepvp.astralsorcery.common.item.base.ItemConstellationFocus;
import hellfirepvp.astralsorcery.common.item.crystal.CrystalProperties;
import hellfirepvp.astralsorcery.common.item.crystal.base.ItemRockCrystalBase;
import hellfirepvp.astralsorcery.common.item.crystal.base.ItemTunedCrystalBase;
import hellfirepvp.astralsorcery.common.item.tool.ItemSkyResonator;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import hellfirepvp.astralsorcery.common.tile.TileGrindstone;
import hellfirepvp.astralsorcery.common.tile.TileRitualPedestal;
import hellfirepvp.astralsorcery.common.tile.TileWell;
import hellfirepvp.astralsorcery.common.tile.base.TileInventoryBase;
import hellfirepvp.astralsorcery.common.tile.base.TileReceiverBaseInventory;
import hellfirepvp.astralsorcery.common.tile.network.TileCollectorCrystal;
import hellfirepvp.astralsorcery.common.tile.network.TileCrystalLens;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.converter.ConstantConverter;
import org.squiddev.plethora.api.meta.BaseMetaProvider;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.api.meta.ItemStackContextMetaProvider;
import org.squiddev.plethora.api.method.ContextHelpers;
import org.squiddev.plethora.api.method.IPartialContext;
import org.squiddev.plethora.api.method.LuaList;
import org.squiddev.plethora.utils.EntityPlayerDummy;
import org.squiddev.plethora.utils.WorldDummy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

@Injects(AstralSorcery.MODID)
public final class IntegrationAstralSorcery {
	private IntegrationAstralSorcery() {
	}

	/* Note that we will mainly be adding meta-providers for Astral Sorcery,
	 * as I do NOT want to deal with HellfirePvP's stance on fake players
	 * ( {@see hellfirepvp.astralsorcery.common.util.MiscUtils.isPlayerFakeMP})
	 *
	 *MEMO The following pieces need to be set up for the test environment to work properly:
	 * Bump the Forge version to AT LEAST 14.23.5.2781
	 * Disable the Patreon flare effects for Astral (it will crash otherwise... >_> )
	 */

	//TODO Clean up this file; cluttered mess!

	/*TODO Providers to add:
	 * Constellation attunement - items
	 * Spectral Relay - TileAttunementRelay - Has SO MANY uses in the mod...
	 *
	 *
	 * Many multiblocks _CANNOT_ have methods, as they do not
	 * permit any other blocks to intrude into their space.  Period.
	 * Not even if you return `true` for `isAir()`.
	 * Having said that, metadata is visible via the Block Scanner's `getBlockMeta`
	 */

	public static final ConstantConverter<ItemStack, CrystalProperties> ITEM_STACK_TO_CRYSTAL_PROPERTIES = CrystalProperties::getCrystalProperties;

/*
	//Didn't work because the item form is of type `ItemCollectorCrystal`, which does NOT implement CrystalPropertyItem
	public static final IMetaProvider<ItemStack> META_CRYSTAL_PROPERTY_ITEM = new ItemStackContextMetaProvider<CrystalPropertyItem>(
		CrystalPropertyItem.class,
		"Provides the cutting, size, purity, and fracturing of this CrystalPropertyItem"
	) {
		@Nonnull
		@Override
		public Map<String, Object> getMeta(@Nonnull IPartialContext<ItemStack> context, @Nonnull CrystalPropertyItem item) {
			ItemStack stack = context.getTarget();

			CrystalProperties properties = item.provideCurrentPropertiesOrNull(stack);
			if (properties == null) return Collections.emptyMap();

			Map<String, Object> out = new HashMap<>(4);

			out.put("cutting", properties.getCollectiveCapability());
			out.put("size", properties.getSize());
			out.put("purity", properties.getPurity());

			out.put("fracture", properties.getFracturation());

			//This field doesn't appear to be used
			//out.put("sizeOverride", properties.getSizeOverride());

			return Collections.singletonMap("astralCrystal2", out);
		}

		@Nonnull
		@Override
		public ItemStack getExample() {
			return ItemRockCrystalBase.createMaxBaseCrystal();
		}
	};
*/

	public static final IMetaProvider<CrystalProperties> META_CRYSTAL_PROPERTY = new BasicMetaProvider<CrystalProperties>(
		"Provides the cutting, size, purity, and fracturing from this CrystalProperties"
	) {
		@Nonnull
		@Override
		public Map<String, Object> getMeta(@Nonnull CrystalProperties props) {
			Map<String, Object> out = new HashMap<>(4);

			out.put("cutting", props.getCollectiveCapability());
			out.put("size", props.getSize());
			out.put("purity", props.getPurity());

			out.put("fracture", props.getFracturation());

			//This field doesn't appear to be used
			//out.put("sizeOverride", properties.getSizeOverride());

			//REFINE Not happy with this namespace...
			return Collections.singletonMap("astralCrystal", out);
		}

		@Nullable //If it returns Null, Astral broke something...
		@Override
		public CrystalProperties getExample() {
			return CrystalProperties.getCrystalProperties(ItemRockCrystalBase.createMaxBaseCrystal());
		}
	};

	public static final IMetaProvider<ItemStack> META_ITEM_CONSTELLATION_FOCUS = new ItemStackContextMetaProvider<ItemConstellationFocus>(
		ItemConstellationFocus.class,
		"Provides the constellation for this item"
	) {
		@Nonnull
		@Override
		public Map<String, Object> getMeta(@Nonnull IPartialContext<ItemStack> context, @Nonnull ItemConstellationFocus item) {
			ItemStack stack = context.getTarget();

			IConstellation constellation = item.getFocusConstellation(stack);
			if (constellation == null) return Collections.emptyMap();

			Map<String, Object> out = new HashMap<>(2);

			//TODO Do we want this, or should we simply `getMeta` on the `IConstellation`?
			out.put("constellationSimpleName", constellation.getSimpleName());
			out.put("constellationUnlocalizedName", constellation.getUnlocalizedName());

			return Collections.singletonMap("astralConstellation", out);
		}

		@Nullable
		@Override
		public ItemStack getExample() {
			ItemStack crystalStack = new ItemStack(ItemsAS.tunedRockCrystal);

			IWeakConstellation constellation = getExampleConstellation();
			if (constellation == null) return null;

			ItemTunedCrystalBase.applyMainConstellation(crystalStack, constellation);

			return crystalStack;
		}
	};

	//Mainly useful if a player wants to design some sort of organizer to track their progress
	public static final IMetaProvider<EntityPlayer> META_PLAYER_PROGRESS = new BaseMetaProvider<EntityPlayer>(
		"Provides the player's progress in Astral Sorcery"
	) {

		@Nonnull
		@Override
		public Map<String, Object> getMeta(@Nonnull IPartialContext<EntityPlayer> context) {
			EntityPlayer player = context.getTarget();
			Map<String, Object> out = new HashMap<>();

			PlayerProgress progress = ResearchManager.getProgress(player);

			//Refers to the constellations that you have seen on a paper
			out.put("seenConstellations", new LuaList<>(progress.getSeenConstellations()).asMap()); //FIXME ... great, it's the unlocalized name...

			//Refers to the constellations that you have discovered via telescope, after seeing them on a paper
			out.put("knownConstellations", new LuaList<>(progress.getKnownConstellations()).asMap()); //FIXME ... great, it's the unlocalized name...

			out.put("availablePerkPoints", progress.getAvailablePerkPoints(player)); //SO MANY METHODS THAT SHOULD BE STATIC, NOT INSTANCE!!!

			IConstellation attuned = progress.getAttunedConstellation();
			out.put("attunedConstellation", attuned != null ? context.makePartialChild(attuned).getMeta() : null);
			out.put("progressTier", progress.getTierReached().toString()); //REFINE Do we want the name, the ordinal, or a LuaList with both?

			// ... shouldn't the `progressId` field be the same as the ordinal? ... whatever.
			out.put("researchTier", progress.getResearchProgression().stream()
				.max(Comparator.comparingInt(ResearchProgression::getProgressId))
				.<Object>map(Enum::toString).orElse(null));

			//REFINE Someone else can expose the Perks if they want; cost/benefit says "no" at this time

			return Collections.singletonMap("astralProgress", out);
		}

		@Nonnull
		@Override
		public EntityPlayer getExample() {
			return new EntityPlayerDummy(WorldDummy.INSTANCE);
		}
	};

	public static final IMetaProvider<IConstellation> META_I_CONSTELLATION = new BasicMetaProvider<IConstellation>() {

		@Nonnull
		@Override
		public Map<String, Object> getMeta(@Nonnull IConstellation context) {
			Map<String, Object> out = new HashMap<>(7);

			out.put("unlocalizedName", context.getUnlocalizedName());
			out.put("simpleName", context.getSimpleName());

			out.put("color", context.getConstellationColor().getRGB()); //Used for particles on rituals, collectors?
			out.put("colour", context.getConstellationColor().getRGB());
			out.put("tierColor", context.getTierRenderColor().getRGB());
			out.put("tierColour", context.getTierRenderColor().getRGB());

			//Because there isn't a nice, clean `getTier` method...
			out.put("tier", context instanceof IMinorConstellation ? "minor"
				: context instanceof IMajorConstellation ? "major" : "weak");

			//Exposing the stars and the connections wouldn't help players much, unless they want to visualize
			// constellations on a computer...?

			//Not wrapping in a namespace, as IConstellation is _usually_ a field on other objects
			return out;
		}

		@Nullable
		@Override
		public IConstellation getExample() {
			return getExampleConstellation();
		}
	};

	public static final IMetaProvider<ItemStack> META_CONSTELLATION_PAPER = new ItemStackContextMetaProvider<ItemConstellationPaper>(
		ItemConstellationPaper.class,
		"Provides the Constellation, if any, for this Constellation Paper"
	) {
		@Nonnull
		@Override
		public Map<String, Object> getMeta(@Nonnull IPartialContext<ItemStack> context, @Nonnull ItemConstellationPaper item) {
			ItemStack stack = context.getTarget();

			return Collections.singletonMap("astralConstellation", context.makePartialChild(ItemConstellationPaper.getConstellation(stack)).getMeta());
		}

		@Nullable
		@Override
		public ItemStack getExample() {
			ItemStack paperStack = new ItemStack(ItemsAS.constellationPaper);

			IWeakConstellation constellation = getExampleConstellation();
			if (constellation == null) return  null;

			ItemConstellationPaper.setConstellation(paperStack, constellation);

			return paperStack;
		}
	};

	public static final IMetaProvider<ItemStack> META_ITEM_JOURNAL = new ItemStackContextMetaProvider<ItemJournal>(
		ItemJournal.class,
		"Provides the Constellation Papers stored in this Journal"
	) {
		@Nonnull
		@Override
		public Map<String, Object> getMeta(@Nonnull IPartialContext<ItemStack> context, @Nonnull ItemJournal item) {
			ItemStack stack = context.getTarget();

			//ItemJournal also has a method to map an IInventory, BUT said IInventory does not apply any
			// of the slot restrictions imposed by the ContainerJournal...
			//REFINE We mainly care about the name of the constellation on the paper...
			return Collections.singletonMap("papers",
				Arrays.stream(ItemJournal.getStoredConstellationStacks(stack))
					.map(paper -> context.makePartialChild(paper).getMeta())
					.collect(LuaList.toLuaList())
					.asMap());
		}

		@Nullable
		@Override
		public ItemStack getExample() {
			//FIXME Set the example
			return null;
		}
	};

	public static final IMetaProvider<TileWell> META_LIGHT_WELL = new BaseMetaProvider<TileWell>(
		"Provides the catalyst item in this Lightwell"
	) {

		@Nonnull
		@Override
		public Map<String, Object> getMeta(@Nonnull IPartialContext<TileWell> context) {

			//TODO turtle.placeDown() doesn't work to insert the catalyst; does this simulate a click, or just try to place a block?
			// Further, the Lightwell won't accept a catalyst if a block is directly above it...
			// And it needs a clear line to the sky... looking a bit like 'No automation via CC for you!'...

			return Collections.singletonMap("catalyst",
				ContextHelpers.wrapStack(context, context.getTarget().getInventoryHandler().getStackInSlot(0)));
		}

		@Nonnull
		@Override
		public TileWell getExample() {
			TileWell well = new TileWell();
			well.getInventoryHandler().setStackInSlot(0, new ItemStack(ItemsAS.craftingComponent));
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
			out.put("constellation", context.makePartialChild(target.getConstellation()).getMeta());
			out.put("traitConstellation", context.makePartialChild(target.getTrait()).getMeta());
			out.put("crystalType", target.getType().name());

			return out;
		}

		@Nullable
		@Override
		public TileCollectorCrystal getExample() {
			return null;
		}
	};

	public static final IMetaProvider<TileCrystalLens> META_TILE_CRYSTAL_LENS= new BaseMetaProvider<TileCrystalLens>() {

		@Nonnull
		@Override
		public Map<String, Object> getMeta(@Nonnull IPartialContext<TileCrystalLens> context) {
			Map<String, Object> out = new HashMap<>(2);
			TileCrystalLens target = context.getTarget();

			out.putAll(context.makePartialChild(target.getCrystalProperties()).getMeta());

			ItemColoredLens.ColorType lens = target.getLensColor();
			out.put("lensColor", lens != null ? lens.getUnlocalizedName() : null);

			return out;
		}

		@Nullable
		@Override
		public TileCrystalLens getExample() {
			return null;
		}
	};

	public static final IMetaProvider<ItemStack> META_SKY_RESONATOR = new ItemStackContextMetaProvider<ItemSkyResonator>(
		ItemSkyResonator.class,
		"Provides the available modes for this Resonator"
	) {
		@Nonnull
		@Override
		public Map<String, Object> getMeta(@Nonnull IPartialContext<ItemStack> context, @Nonnull ItemSkyResonator item) {
			//REFINE Do we want `getUnlocalizedName` or `getUnlocalizedUpgradeName`?
			// Further, should we localize the String?
			return Collections.singletonMap("modes",
				ItemSkyResonator.getUpgrades(context.getTarget()).stream()
					.map(ItemSkyResonator.ResonatorUpgrade::getUnlocalizedName)
					.collect(LuaList.toLuaList()));
		}

		@Nullable
		@Override
		public ItemStack getExample() {
			return null;
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
			//REFINE Implement this example
			// Since `setGrindingItem` updates the BlockState, we will have to create the block for the TE too
			return null;
		}
	};

	public static final IMetaProvider<TileRitualPedestal> META_RITUAL_PEDESTAL= new BaseMetaProvider<TileRitualPedestal>() {

		@Nonnull
		@Override
		public Map<String, Object> getMeta(@Nonnull IPartialContext<TileRitualPedestal> context) {
			Map<String, Object> out = new HashMap<>(4);
			TileRitualPedestal target = context.getTarget();

			out.putAll(context.makePartialChild(target.getInventoryHandler().getStackInSlot(0)).getMeta());

			IConstellation ritual = target.getRitualConstellation();
			out.put("constellation", ritual != null ? context.makePartialChild(ritual).getMeta() : null);

			IConstellation trait = target.getRitualTrait();
			out.put("traitConstellation", trait != null ? context.makePartialChild(trait).getMeta() : null);

			out.put("isWorking", target.isWorking());

			return out;
		}

		@Nullable
		@Override
		public TileRitualPedestal getExample() {
			return null;
		}
	};

	@Nullable
	private static IWeakConstellation getExampleConstellation() {
		//Yes, I just picked Discidia at random; feel free to swap with `ConstellationRegistry.getMajorConstellations.get(0)`
		return ConstellationRegistry.getMajorConstellationByName(AstralSorcery.MODID + ".constellation.discidia");
	}


}
