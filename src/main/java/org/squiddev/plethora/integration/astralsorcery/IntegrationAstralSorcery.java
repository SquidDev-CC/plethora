package org.squiddev.plethora.integration.astralsorcery;

import dan200.computercraft.api.lua.ILuaObject;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.block.network.BlockCollectorCrystalBase;
import hellfirepvp.astralsorcery.common.constellation.*;
import hellfirepvp.astralsorcery.common.constellation.starmap.ActiveStarMap;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ResearchManager;
import hellfirepvp.astralsorcery.common.data.research.ResearchProgression;
import hellfirepvp.astralsorcery.common.data.world.WorldCacheManager;
import hellfirepvp.astralsorcery.common.enchantment.amulet.AmuletEnchantment;
import hellfirepvp.astralsorcery.common.enchantment.dynamic.DynamicEnchantment;
import hellfirepvp.astralsorcery.common.item.*;
import hellfirepvp.astralsorcery.common.item.block.ItemCollectorCrystal;
import hellfirepvp.astralsorcery.common.item.crystal.CrystalProperties;
import hellfirepvp.astralsorcery.common.item.crystal.CrystalPropertyItem;
import hellfirepvp.astralsorcery.common.item.crystal.base.ItemRockCrystalBase;
import hellfirepvp.astralsorcery.common.item.crystal.base.ItemTunedCrystalBase;
import hellfirepvp.astralsorcery.common.item.tool.ItemSkyResonator;
import hellfirepvp.astralsorcery.common.item.tool.sextant.ItemSextant;
import hellfirepvp.astralsorcery.common.item.tool.sextant.SextantFinder;
import hellfirepvp.astralsorcery.common.item.tool.wand.ItemWand;
import hellfirepvp.astralsorcery.common.item.tool.wand.WandAugment;
import hellfirepvp.astralsorcery.common.item.wand.ItemIlluminationWand;
import hellfirepvp.astralsorcery.common.item.wearable.ItemCape;
import hellfirepvp.astralsorcery.common.item.wearable.ItemEnchantmentAmulet;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.Constellations;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import hellfirepvp.astralsorcery.common.tile.*;
import hellfirepvp.astralsorcery.common.tile.network.TileCollectorCrystal;
import hellfirepvp.astralsorcery.common.tile.network.TileCrystalLens;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
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

	/*Providers that could be added:
	 * Evershifting Fountain - blockbore
	 * Celestial Gateway - The 'display name' is not exposed
	 * TODO Ask Squid about support for `IWorldNameable` and whether it'd cause issues
	 *
	 * Some multiblocks _CANNOT_ have methods, as they do not
	 * permit any other blocks to intrude into their space.  Period.
	 * Not even if you return `true` for `isAir()`.
	 * Having said that, metadata is visible via the Block Scanner's `getBlockMeta`
	 */

	public static final ConstantConverter<ItemStack, CrystalProperties> ITEM_STACK_TO_CRYSTAL_PROPERTIES = stack -> {
		Item item = stack.getItem();
		return item instanceof CrystalPropertyItem
			? ((CrystalPropertyItem) item).provideCurrentPropertiesOrNull(stack)
			: CrystalProperties.getCrystalProperties(stack);

	};

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

			return Collections.singletonMap("crystalProperties", out);
		}

		@Nullable //If it returns Null, Astral broke something...
		@Override
		public CrystalProperties getExample() {
			return CrystalProperties.getMaxRockProperties();
		}
	};

	public static final IMetaProvider<ItemStack> META_TUNED_CRYSTAL_BASE = new ItemStackContextMetaProvider<ItemTunedCrystalBase>(
		ItemTunedCrystalBase.class,
		"Provides the constellation(s) for this item"
	) {
		@Nonnull
		@Override
		public Map<String, Object> getMeta(@Nonnull IPartialContext<ItemStack> context, @Nonnull ItemTunedCrystalBase item) {
			ItemStack stack = context.getTarget();
			Map<String, Object> out = new HashMap<>(2);

			IConstellation constellation = item.getFocusConstellation(stack);
			if (constellation != null) {
				out.put("constellationFocus", context.makePartialChild(constellation).getMeta());
			}

			IConstellation trait = ItemTunedCrystalBase.getTrait(stack);
			if (trait != null) {
				out.put("constellationTrait", context.makePartialChild(trait).getMeta());
			}

			return out;
		}

		@Nullable
		@Override
		public ItemStack getExample() {
			ItemStack crystalStack = new ItemStack(ItemsAS.tunedRockCrystal);
			CrystalProperties.applyCrystalProperties(crystalStack, CrystalProperties.getMaxRockProperties());

			IWeakConstellation constellation = Constellations.discidia;
			if (constellation == null) return null;

			ItemTunedCrystalBase.applyMainConstellation(crystalStack, constellation);

			IMinorConstellation trait = Constellations.gelu;
			if (trait == null) return crystalStack;

			ItemTunedCrystalBase.applyTrait(crystalStack, trait);

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
			if (attuned != null) {
				out.put("attunedConstellation", context.makePartialChild(attuned).getMeta());
			}
			out.put("progressTier", progress.getTierReached().toString()); //REFINE Do we want the name, the ordinal, or a LuaList with both?

			// ... shouldn't the `progressId` field be the same as the ordinal? ... whatever.
			//noinspection SimplifyOptionalCallChains It may be simpler, but it (to me) hurts readability...
			String researchTier = progress.getResearchProgression().stream()
				.max(Comparator.comparingInt(ResearchProgression::getProgressId))
				.map(Enum::toString).orElse(null);
			if (researchTier != null) {
				out.put("researchTier", researchTier);
			}

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

			out.put("tier", getConstellationTier(context));

			//Exposing the stars and the connections wouldn't help players much, unless they want to visualize
			// constellations on a computer...?

			//Not wrapping in a namespace, as IConstellation is _usually_ a field on other objects
			return out;
		}

		@Nullable
		@Override
		public IConstellation getExample() {
			return Constellations.discidia;
		}
	};

	public static final IMetaProvider<ItemStack> META_CONSTELLATION_PAPER = new ItemStackContextMetaProvider<ItemConstellationPaper>(
		ItemConstellationPaper.class,
		"Provides the Constellation, if any, for this Constellation Paper"
	) {
		@Nonnull
		@Override
		public Map<String, Object> getMeta(@Nonnull IPartialContext<ItemStack> context, @Nonnull ItemConstellationPaper item) {
			IConstellation constellation = ItemConstellationPaper.getConstellation(context.getTarget());
			return constellation != null
				? Collections.singletonMap("constellation", context.makePartialChild(constellation).getMeta())
				: Collections.emptyMap();
		}

		@Nullable
		@Override
		public ItemStack getExample() {
			ItemStack paperStack = new ItemStack(ItemsAS.constellationPaper);

			IWeakConstellation constellation = Constellations.discidia;
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
			//REFINE Or should we use `getStoredConstellations` and not worry about the papers themselves?
			// Saves a slight bit of work in Astral's code...
			return Collections.singletonMap("papers",
				LuaList.of(Arrays.asList(ItemJournal.getStoredConstellationStacks(context.getTarget())),
					paper -> context.makePartialChild(paper).getMeta()).asMap());
		}

		@Nonnull
		@Override
		public ItemStack getExample() {
			ItemStack stack = new ItemStack(ItemsAS.journal);

			LinkedList<IConstellation> constellations = new LinkedList<>();
			constellations.add(Constellations.discidia);

			ItemJournal.setStoredConstellations(stack,constellations);

			return stack;
		}
	};

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

	public static final IMetaProvider<TileCrystalLens> META_TILE_CRYSTAL_LENS= new BaseMetaProvider<TileCrystalLens>() {

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
			if (!(te instanceof TileCrystalLens)) return  null;

			TileCrystalLens lens = (TileCrystalLens) te;
			lens.onPlace(CrystalProperties.getMaxRockProperties());
			lens.setLensColor(ItemColoredLens.ColorType.SPECTRAL);

			return lens;
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

			/*
			ItemStack stack = context.getTarget();
			List<ItemSkyResonator.ResonatorUpgrade> modes = ItemSkyResonator.getUpgrades(stack);
			LuaList<String> modeNames = new LuaList<>(modes.size());

			for (ItemSkyResonator.ResonatorUpgrade mode : modes) {
				modeNames.add(mode.getUnlocalizedName());
			}

			return Collections.singletonMap("modes", modeNames.asMap()); */

			return Collections.singletonMap("modes",
				LuaList.of(ItemSkyResonator.getUpgrades(context.getTarget()),
					ItemSkyResonator.ResonatorUpgrade::getUnlocalizedName).asMap());
		}

		@Nonnull
		@Override
		public ItemStack getExample() {
			ItemStack stack = new ItemStack(ItemsAS.skyResonator);
			ItemSkyResonator.setUpgradeUnlocked(stack, ItemSkyResonator.ResonatorUpgrade.AREA_SIZE);
			ItemSkyResonator.setUpgradeUnlocked(stack, ItemSkyResonator.ResonatorUpgrade.FLUID_FIELDS);

			return stack;
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

	public static final IMetaProvider<TileRitualPedestal> META_RITUAL_PEDESTAL= new BaseMetaProvider<TileRitualPedestal>() {

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

	public static final IMetaProvider<ItemStack> META_COLORED_LENS = new ItemStackContextMetaProvider<ItemColoredLens>(
		ItemColoredLens.class,
		"FIXME Set the description"
	) {
		@Nonnull
		@Override
		public Map<String, Object> getMeta(@Nonnull IPartialContext<ItemStack> context, @Nonnull ItemColoredLens item) {
			ItemStack stack = context.getTarget();

			// ... yay for the lack of a native reverse lookup or conversion based on the ordinal...
			// (e.g. casting `int` to enum or a `TryParse` type method...)
			ItemColoredLens.ColorType[] colors = ItemColoredLens.ColorType.values();
			int meta = stack.getMetadata();

			// Didn't find a concise yet readable format I liked, so a comment it is!
			// Basically, run a bounds check, then get the name of the enum if in bounds
			String colorName = meta >= colors.length || meta < 0 ? null : colors[meta].getUnlocalizedName();

			if (colorName == null) return Collections.emptyMap();

			Map<String, Object> out = new HashMap<>(2);
			out.put("lensColor", colorName);
			out.put("lensColour", colorName);

			return out;
		}

		@Nonnull
		@Override
		public ItemStack getExample() {
			return new ItemStack(ItemsAS.coloredLens, 1, ItemColoredLens.ColorType.SPECTRAL.getMeta());
		}
	};

	public static final IMetaProvider<ItemStack> META_RESPLENDENT_PRISM = new ItemStackContextMetaProvider<ItemEnchantmentAmulet>(
		ItemEnchantmentAmulet.class,
		"FIXME Set the description"
	) {
		@Nonnull
		@Override
		public Map<String, Object> getMeta(@Nonnull IPartialContext<ItemStack> context, @Nonnull ItemEnchantmentAmulet item) {
			return Collections.singletonMap("amuletEnchantments",
				LuaList.of(ItemEnchantmentAmulet.getAmuletEnchantments(context.getTarget()),
					e -> context.makePartialChild(e).getMeta()).asMap());
		}

		@Nullable
		@Override
		public ItemStack getExample() {
			ItemStack stack = new ItemStack(ItemsAS.enchantmentAmulet);
			ItemEnchantmentAmulet.freezeAmuletColor(stack); //Not strictly necessary, but...

			List<AmuletEnchantment> enchants = new ArrayList<>(2);

			//FIXME Determine what enchants we want on our example
			//enchants.add(new AmuletEnchantment(AmuletEnchantment.Type.))

			return null;
		}
	};

	//REFINE As this is only ever called via META_RESPLENDENT_PRISM, we technically could just use a method
	// rather than a full-fledged IMetaProvider
	public static final IMetaProvider<AmuletEnchantment> META_AMULET_ENCHANTMENT = new BaseMetaProvider<AmuletEnchantment>() {

		@Nonnull
		@Override
		public Map<String, Object> getMeta(@Nonnull IPartialContext<AmuletEnchantment> context) {
			Map<String, Object> out = new HashMap<>(3);
			AmuletEnchantment target = context.getTarget();

			DynamicEnchantment.Type enchantType = target.getType();
			out.put("bonusType", enchantType.toString());

			//REFINE I feel like I'm missing some simple logical fallacy here...
			// Could just be that the code is brittle, as broken data could cause a type other than
			// `ADD_TO_EXISTING_ALL` to have a `null` enchant...
			Enchantment enchant = target.getEnchantment();
			out.put("boostedEnchant", (enchantType.hasEnchantmentTag() && enchant != null)
				? enchant.getName() : "all");

			out.put("bonusLevel", target.getLevelAddition());

			return out;
		}

		@Nullable
		@Override
		public AmuletEnchantment getExample() {
			return null;
		}
	};

	public static final IMetaProvider<ItemStack> META_MANTLE_OF_THE_STARS = new ItemStackContextMetaProvider<ItemCape>(
		ItemCape.class,
		"FIXME Set the description"
	) {
		@Nonnull
		@Override
		public Map<String, Object> getMeta(@Nonnull IPartialContext<ItemStack> context, @Nonnull ItemCape item) {
			IConstellation constellation = ItemCape.getAttunedConstellation(context.getTarget());
			return constellation != null
				? Collections.singletonMap("constellation", context.makePartialChild(constellation).getMeta())
				: Collections.emptyMap();
		}

		@Nonnull
		@Override
		public ItemStack getExample() {
			ItemStack stack = new ItemStack(ItemsAS.armorImbuedCape);
			ItemCape.setAttunedConstellation(stack, Constellations.discidia);

			return stack;
		}
	};

	public static final IMetaProvider<ItemStack> META_ILLUMINATION_WAND = new ItemStackContextMetaProvider<ItemIlluminationWand>(
		ItemIlluminationWand.class,
		"FIXME Set the description"
	) {
		@Nonnull
		@Override
		public Map<String, Object> getMeta(@Nonnull IPartialContext<ItemStack> context, @Nonnull ItemIlluminationWand item) {
			EnumDyeColor color = ItemIlluminationWand.getConfiguredColor(context.getTarget());
			if (color == null) return Collections.emptyMap();

			Map<String, Object> out = new HashMap<>(2);

			out.put("flareColor", color.toString());
			out.put("flareColour", color.toString());

			return out;
		}

		@Nonnull
		@Override
		public ItemStack getExample() {
			ItemStack wand = new ItemStack(ItemsAS.illuminationWand);
			ItemIlluminationWand.setConfiguredColor(wand, EnumDyeColor.YELLOW);
			return wand;
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

	public static final IMetaProvider<ItemStack> META_RESONATING_WAND = new ItemStackContextMetaProvider<ItemWand>(
		ItemWand.class,
		"FIXME Set the description"
	) {
		@Nonnull
		@Override
		public Map<String, Object> getMeta(@Nonnull IPartialContext<ItemStack> context, @Nonnull ItemWand item) {
			WandAugment augment = ItemWand.getAugment(context.getTarget());
			if (augment == null) return Collections.emptyMap();

			IConstellation constellation = augment.getAssociatedConstellation();
			return constellation != null
				? Collections.singletonMap("constellation", context.makePartialChild(constellation).getMeta())
				: Collections.emptyMap();
		}

		@Nullable
		@Override
		public ItemStack getExample() {
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

	public static final IMetaProvider<ItemStack> META_INFUSED_GLASS = new ItemStackContextMetaProvider<ItemInfusedGlass>(
		ItemInfusedGlass.class,
		"FIXME Set the description"
	) {
		@Nonnull
		@Override
		public Map<String, Object> getMeta(@Nonnull IPartialContext<ItemStack> context, @Nonnull ItemInfusedGlass item) {
			//We are only exposing the constellations on this piece of Infused Glass, nothing more!
			ActiveStarMap starMap = ItemInfusedGlass.getMapEngravingInformations(context.getTarget());
			return starMap == null
				? Collections.emptyMap()
				: Collections.singletonMap("constellations",
				LuaList.of(starMap.getConstellations(),
					c -> context.makePartialChild(c).getMeta()).asMap());
		}

		@Nullable
		@Override
		public ItemStack getExample() {
			return null;
		}
	};

	public static final IMetaProvider<ItemStack> META_SEXTANT = new ItemStackContextMetaProvider<ItemSextant>(
		ItemSextant.class,
		"FIXME Set the description"
	) {
		@Nonnull
		@Override
		public Map<String, Object> getMeta(@Nonnull IPartialContext<ItemStack> context, @Nonnull ItemSextant item) {
			ItemStack stack = context.getTarget();

			Map<String, Object> out = new HashMap<>(2);

			out.put("augmented", ItemSextant.isAdvanced(stack));

			SextantFinder.TargetObject target = ItemSextant.getTarget(stack);
			if (target != null) {
				out.put("targetType", target.getRegistryName());
			}

			return out;
		}

		@Nullable
		@Override
		public ItemStack getExample() {
			return null;
		}
	};


	@SuppressWarnings("SimplifiableIfStatement")
	private static String getConstellationTier(IConstellation constellation) {
		if (constellation instanceof IMinorConstellation) return "minor";
		if (constellation instanceof IMajorConstellation) return "major";
		return "weak";
	}
}
