package org.squiddev.plethora.integration.astralsorcery;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.IMinorConstellation;
import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.constellation.starmap.ActiveStarMap;
import hellfirepvp.astralsorcery.common.enchantment.amulet.AmuletEnchantment;
import hellfirepvp.astralsorcery.common.item.ItemColoredLens;
import hellfirepvp.astralsorcery.common.item.ItemConstellationPaper;
import hellfirepvp.astralsorcery.common.item.ItemInfusedGlass;
import hellfirepvp.astralsorcery.common.item.ItemJournal;
import hellfirepvp.astralsorcery.common.item.crystal.CrystalProperties;
import hellfirepvp.astralsorcery.common.item.crystal.base.ItemTunedCrystalBase;
import hellfirepvp.astralsorcery.common.item.tool.ItemSkyResonator;
import hellfirepvp.astralsorcery.common.item.tool.sextant.ItemSextant;
import hellfirepvp.astralsorcery.common.item.tool.sextant.SextantFinder;
import hellfirepvp.astralsorcery.common.item.tool.wand.ItemWand;
import hellfirepvp.astralsorcery.common.item.tool.wand.WandAugment;
import hellfirepvp.astralsorcery.common.item.wand.ItemIlluminationWand;
import hellfirepvp.astralsorcery.common.item.wearable.ItemCape;
import hellfirepvp.astralsorcery.common.item.wearable.ItemEnchantmentAmulet;
import hellfirepvp.astralsorcery.common.lib.Constellations;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.api.meta.ItemStackContextMetaProvider;
import org.squiddev.plethora.api.method.IPartialContext;
import org.squiddev.plethora.api.method.LuaList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

@Injects(AstralSorcery.MODID)
public final class MetaItems {
	private MetaItems() {
	}

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
			if (constellation == null) return null;

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

			ItemJournal.setStoredConstellations(stack, constellations);

			return stack;
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
}
