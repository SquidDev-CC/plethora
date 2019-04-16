package org.squiddev.plethora.integration.astralsorcery;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.constellation.*;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ResearchManager;
import hellfirepvp.astralsorcery.common.data.research.ResearchProgression;
import hellfirepvp.astralsorcery.common.enchantment.amulet.AmuletEnchantment;
import hellfirepvp.astralsorcery.common.enchantment.dynamic.DynamicEnchantment;
import hellfirepvp.astralsorcery.common.item.ItemColoredLens;
import hellfirepvp.astralsorcery.common.item.ItemConstellationPaper;
import hellfirepvp.astralsorcery.common.item.ItemInfusedGlass;
import hellfirepvp.astralsorcery.common.item.ItemJournal;
import hellfirepvp.astralsorcery.common.item.base.ItemConstellationFocus;
import hellfirepvp.astralsorcery.common.item.block.ItemCollectorCrystal;
import hellfirepvp.astralsorcery.common.item.crystal.CrystalProperties;
import hellfirepvp.astralsorcery.common.item.crystal.CrystalPropertyItem;
import hellfirepvp.astralsorcery.common.item.crystal.base.ItemRockCrystalBase;
import hellfirepvp.astralsorcery.common.item.crystal.base.ItemTunedCrystalBase;
import hellfirepvp.astralsorcery.common.item.tool.ItemCrystalToolBase;
import hellfirepvp.astralsorcery.common.item.tool.ItemSkyResonator;
import hellfirepvp.astralsorcery.common.item.tool.sextant.ItemSextant;
import hellfirepvp.astralsorcery.common.item.tool.wand.ItemWand;
import hellfirepvp.astralsorcery.common.item.tool.wand.WandAugment;
import hellfirepvp.astralsorcery.common.item.wand.ItemIlluminationWand;
import hellfirepvp.astralsorcery.common.item.wearable.ItemCape;
import hellfirepvp.astralsorcery.common.item.wearable.ItemEnchantmentAmulet;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import hellfirepvp.astralsorcery.common.tile.*;
import hellfirepvp.astralsorcery.common.tile.base.TileInventoryBase;
import hellfirepvp.astralsorcery.common.tile.base.TileReceiverBaseInventory;
import hellfirepvp.astralsorcery.common.tile.network.TileCollectorCrystal;
import hellfirepvp.astralsorcery.common.tile.network.TileCrystalLens;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;
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

import static org.squiddev.plethora.integration.PlethoraIntegration.LOG;

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
	 * Many multiblocks _CANNOT_ have methods, as they do not
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

	//FIXME Since this method doesn't return `null`, it overrides the above converter.
	// How should I handle the inconsistency in how CrystalProperties are stored to NBT?
	//... ... yeah, don't expect Astral's code to be consistent.
	//public static final ConstantConverter<ItemStack, CrystalProperties> ITEM_STACK_CRYSTAL_TOOL_TO_CRYSTAL_PROPERTIES = ItemCrystalToolBase::getToolProperties;

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

			return Collections.singletonMap("crystalProperties", out);
		}

		@Nullable //If it returns Null, Astral broke something...
		@Override
		public CrystalProperties getExample() {
			return CrystalProperties.getCrystalProperties(ItemRockCrystalBase.createMaxBaseCrystal());
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
			out.put("constellationFocus", constellation == null ? null : context.makePartialChild(constellation).getMeta());

			IConstellation trait = ItemTunedCrystalBase.getTrait(stack);
			out.put("constellationTrait", trait == null ? null : context.makePartialChild(trait).getMeta());

			return out;
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
			IConstellation constellation = ItemConstellationPaper.getConstellation(context.getTarget());
			return Collections.singletonMap("constellation",
				constellation != null ? context.makePartialChild(constellation).getMeta() : null);
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

			//REFINE Do we want this stack wrapped, or should we just `getMeta`?
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

			IWeakConstellation mainConstellation = target.getConstellation();
			out.put("constellation", mainConstellation != null ? context.makePartialChild(mainConstellation).getMeta() : null);

			IMinorConstellation traitConstellation = target.getTrait();
			out.put("traitConstellation", traitConstellation != null ? context.makePartialChild(traitConstellation).getMeta() : null);

			out.put("crystalType", target.getType().name());

			return out;
		}

		@Nullable
		@Override
		public TileCollectorCrystal getExample() {
			return null;
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
			out.put("constellation", mainConstellation != null ? context.makePartialChild(mainConstellation).getMeta() : null);

			IMinorConstellation traitConstellation = ItemCollectorCrystal.getTrait(stack);
			out.put("traitConstellation", traitConstellation != null ? context.makePartialChild(traitConstellation).getMeta() : null);

			out.put("crystalType", ItemCollectorCrystal.getType(stack).name());

			return out;
		}

		@Nullable
		@Override
		public ItemStack getExample() {
			return null;
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

			out.put("lensColor", colorName);
			out.put("lensColour", colorName);

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

			/*
			ItemStack stack = context.getTarget();
			List<ItemSkyResonator.ResonatorUpgrade> modes = ItemSkyResonator.getUpgrades(stack);
			LuaList<String> modeNames = new LuaList<>(modes.size());

			for (ItemSkyResonator.ResonatorUpgrade mode : modes) {
				modeNames.add(mode.getUnlocalizedName());
			}

			return Collections.singletonMap("modes", modeNames.asMap()); */

			return Collections.singletonMap("modes",
				ItemSkyResonator.getUpgrades(context.getTarget()).stream()
					.map(ItemSkyResonator.ResonatorUpgrade::getUnlocalizedName)
					.collect(LuaList.toLuaList()).asMap());
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

			Map<String, Object> out = new HashMap<>(2);
			out.put("lensColor", colorName);
			out.put("lensColour", colorName);

			return out;
		}

		@Nullable
		@Override
		public ItemStack getExample() {
			return null;
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
				ItemEnchantmentAmulet.getAmuletEnchantments(context.getTarget()).stream()
				.map(e -> context.makePartialChild(e).getMeta())
				.collect(LuaList.toLuaList()).asMap());
		}

		@Nullable
		@Override
		public ItemStack getExample() {
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
			return Collections.singletonMap("constellation",
				constellation != null
					? context.makePartialChild(constellation).getMeta() : null);
		}

		@Nullable
		@Override
		public ItemStack getExample() {
			return null;
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

		@Nullable
		@Override
		public ItemStack getExample() {
			return null;
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
			Map<String, Object> out = new HashMap<>();
			TileMapDrawingTable target = context.getTarget();

			//TODO Expose the infused glass, processing slot

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
			ItemStack stack = context.getTarget();

			//TODO Expose the engraved constellations
			//We are only exposing the constellations on this piece of Infused Glass, nothing more!

			return Collections.emptyMap();
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
			//TODO Expose whether the sextant is augmented
			return Collections.emptyMap();
		}

		@Nullable
		@Override
		public ItemStack getExample() {
			return null;
		}
	};

	@Nullable
	private static IWeakConstellation getExampleConstellation() {
		//Yes, I just picked Discidia at random; feel free to swap with `ConstellationRegistry.getMajorConstellations.get(0)`
		return ConstellationRegistry.getMajorConstellationByName(AstralSorcery.MODID + ".constellation.discidia");
	}


}
