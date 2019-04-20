package org.squiddev.plethora.integration.astralsorcery;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.IMinorConstellation;
import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.constellation.starmap.ActiveStarMap;
import hellfirepvp.astralsorcery.common.enchantment.amulet.AmuletEnchantment;
import hellfirepvp.astralsorcery.common.enchantment.dynamic.DynamicEnchantment;
import hellfirepvp.astralsorcery.common.item.ItemColoredLens;
import hellfirepvp.astralsorcery.common.item.ItemConstellationPaper;
import hellfirepvp.astralsorcery.common.item.ItemInfusedGlass;
import hellfirepvp.astralsorcery.common.item.ItemJournal;
import hellfirepvp.astralsorcery.common.item.crystal.CrystalProperties;
import hellfirepvp.astralsorcery.common.item.crystal.base.ItemTunedCrystalBase;
import hellfirepvp.astralsorcery.common.item.tool.ItemSkyResonator;
import hellfirepvp.astralsorcery.common.item.tool.sextant.ItemSextant;
import hellfirepvp.astralsorcery.common.item.tool.sextant.SextantFinder;
import hellfirepvp.astralsorcery.common.item.tool.sextant.SextantTargets;
import hellfirepvp.astralsorcery.common.item.tool.wand.ItemWand;
import hellfirepvp.astralsorcery.common.item.tool.wand.WandAugment;
import hellfirepvp.astralsorcery.common.item.wand.ItemIlluminationWand;
import hellfirepvp.astralsorcery.common.item.wearable.ItemCape;
import hellfirepvp.astralsorcery.common.item.wearable.ItemEnchantmentAmulet;
import hellfirepvp.astralsorcery.common.lib.Constellations;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentLootBonus;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.api.meta.ItemStackContextMetaProvider;
import org.squiddev.plethora.api.method.IPartialContext;
import org.squiddev.plethora.api.method.LuaList;
import org.squiddev.plethora.utils.Helpers;

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
		"Provides the Constellation for this Constellation Paper"
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
			List<ItemSkyResonator.ResonatorUpgrade> modes = ItemSkyResonator.getUpgrades(context.getTarget());
			LuaList<Map<String, String>> modesOut = new LuaList<>(modes.size());

			for (ItemSkyResonator.ResonatorUpgrade mode : modes) {
				Map<String, String> modeMap = new HashMap<>(2);
				String translationKey = mode.getUnlocalizedUpgradeName();
				modeMap.put("name", translationKey);
				modeMap.put("displayName", Helpers.translateToLocal(translationKey));

				modesOut.add(modeMap);
			}

			return Collections.singletonMap("modes", modesOut.asMap());

			//REFINE This could be converted to use `LuaList.of`, but the lambda would be a bit unwieldy...
/*			return Collections.singletonMap("modes",
				LuaList.of(ItemSkyResonator.getUpgrades(context.getTarget()),
					ItemSkyResonator.ResonatorUpgrade::getUnlocalizedUpgradeName).asMap());*/
		}

		@Nonnull
		@Override
		public ItemStack getExample() {
			ItemStack stack = new ItemStack(ItemsAS.skyResonator);
			ItemSkyResonator.setEnhanced(stack);
			ItemSkyResonator.setUpgradeUnlocked(stack, ItemSkyResonator.ResonatorUpgrade.AREA_SIZE);
			ItemSkyResonator.setUpgradeUnlocked(stack, ItemSkyResonator.ResonatorUpgrade.FLUID_FIELDS);

			return stack;
		}
	};

	public static final IMetaProvider<ItemStack> META_COLORED_LENS = new ItemStackContextMetaProvider<ItemColoredLens>(
		ItemColoredLens.class,
		"Provides the color of this lens"
	) {
		@Nonnull
		@Override
		public Map<String, Object> getMeta(@Nonnull IPartialContext<ItemStack> context, @Nonnull ItemColoredLens item) {
			// ... yay for the lack of a native reverse lookup or conversion based on the ordinal...
			// (e.g. casting `int` to enum or a `TryParse` type method...)
			ItemColoredLens.ColorType[] colors = ItemColoredLens.ColorType.values();
			int meta = context.getTarget().getMetadata();

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
		"Provides the enchantment bonuses for this Resplendent Prism"
	) {
		@Nonnull
		@Override
		public Map<String, Object> getMeta(@Nonnull IPartialContext<ItemStack> context, @Nonnull ItemEnchantmentAmulet item) {
			return Collections.singletonMap("amuletEnchantments",
				LuaList.of(ItemEnchantmentAmulet.getAmuletEnchantments(context.getTarget()),
					e -> context.makePartialChild(e).getMeta()).asMap());
		}

		@Nonnull
		@Override
		public ItemStack getExample() {
			ItemStack stack = new ItemStack(ItemsAS.enchantmentAmulet);
			ItemEnchantmentAmulet.freezeAmuletColor(stack); //Not strictly necessary, but...

			List<AmuletEnchantment> enchants = new ArrayList<>(3);

			Enchantment looting = ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("looting"));
			if (looting != null) {
				enchants.add(new AmuletEnchantment(AmuletEnchantment.Type.ADD_TO_EXISTING_SPECIFIC, looting, 1));
			}
			enchants.add(new AmuletEnchantment(AmuletEnchantment.Type.ADD_TO_EXISTING_ALL, 2));

			ItemEnchantmentAmulet.setAmuletEnchantments(stack, enchants);

			return stack;
		}
	};

	public static final IMetaProvider<ItemStack> META_MANTLE_OF_THE_STARS = new ItemStackContextMetaProvider<ItemCape>(
		ItemCape.class,
		"Provides the constellation this Mantle of the Stars is attuned to"
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
		"Provides the color of flares place by this Illumination Wand"
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
		"Provides the constellation this Resonating Wand is attuned to"
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

		@Nonnull
		@Override
		public ItemStack getExample() {
			ItemStack stack = new ItemStack(ItemsAS.wand);
			ItemWand.setAugment(stack, WandAugment.DISCIDIA);
			return stack;
		}
	};

	public static final IMetaProvider<ItemStack> META_INFUSED_GLASS = new ItemStackContextMetaProvider<ItemInfusedGlass>(
		ItemInfusedGlass.class,
		"Provides the constellations etched on this Infused Glass"
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
			//TODO Provide this example
			// This example will require defining the `DrawnConstellation` list to build an `ActiveStarMap`,
			// ensuring that the list is valid input.
			// I simply don't feel like dealing with that at present.
			return null;
		}
	};

	public static final IMetaProvider<ItemStack> META_SEXTANT = new ItemStackContextMetaProvider<ItemSextant>(
		ItemSextant.class,
		"Provides the active target of this Sextant, and whether the Sextant is augmented"
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

		@Nonnull
		@Override
		public ItemStack getExample() {
			ItemStack stack = new ItemStack(ItemsAS.sextant);
			ItemSextant.setAdvanced(stack);
			ItemSextant.setTarget(stack, SextantTargets.TARGET_SMALL_SHRINE);
			return stack;
		}
	};
}
