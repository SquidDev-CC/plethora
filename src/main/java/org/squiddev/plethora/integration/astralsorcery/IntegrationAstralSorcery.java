package org.squiddev.plethora.integration.astralsorcery;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.IMajorConstellation;
import hellfirepvp.astralsorcery.common.constellation.IMinorConstellation;
import hellfirepvp.astralsorcery.common.enchantment.amulet.AmuletEnchantment;
import hellfirepvp.astralsorcery.common.enchantment.dynamic.DynamicEnchantment;
import hellfirepvp.astralsorcery.common.item.crystal.CrystalProperties;
import hellfirepvp.astralsorcery.common.item.crystal.CrystalPropertyItem;
import hellfirepvp.astralsorcery.common.lib.Constellations;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.converter.ConstantConverter;
import org.squiddev.plethora.api.meta.BaseMetaProvider;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.api.method.IPartialContext;
import org.squiddev.plethora.utils.Helpers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Injects(AstralSorcery.MODID)
public final class IntegrationAstralSorcery {
	private IntegrationAstralSorcery() {
	}

	//FIXME Determine whether access to fields in `Constellations` should be tested for `null`,
	// then ensure consistency in the code

	/* Note that we will mainly be adding meta-providers for Astral Sorcery,
	 * as I do NOT want to deal with HellfirePvP's stance on fake players
	 * ( {@see hellfirepvp.astralsorcery.common.util.MiscUtils.isPlayerFakeMP})
	 *
	 *MEMO The following pieces need to be set up for the test environment to work properly:
	 * Bump the Forge version to AT LEAST 14.23.5.2781
	 * Disable the Patreon flare effects for Astral (it will crash otherwise... >_> )
	 */

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

	public static final IMetaProvider<IConstellation> META_I_CONSTELLATION = new BasicMetaProvider<IConstellation>() {

		@Nonnull
		@Override
		public Map<String, Object> getMeta(@Nonnull IConstellation context) {
			Map<String, Object> out = new HashMap<>(7);

			String translationKey = context.getUnlocalizedName();
			out.put("name", translationKey);
			out.put("simpleName", context.getSimpleName());
			out.put("displayName", Helpers.translateToLocal(translationKey));

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

	//REFINE As this is only ever called via META_RESPLENDENT_PRISM, we technically could just use a method
	// rather than a full-fledged IMetaProvider
	public static final IMetaProvider<AmuletEnchantment> META_AMULET_ENCHANTMENT = new BaseMetaProvider<AmuletEnchantment>() {

		@Nonnull
		@Override
		public Map<String, Object> getMeta(@Nonnull IPartialContext<AmuletEnchantment> context) {
			Map<String, Object> out = new HashMap<>(4);
			AmuletEnchantment target = context.getTarget();

			DynamicEnchantment.Type enchantType = target.getType();
			out.put("bonusType", enchantType.toString());

			//REFINE I feel like I'm missing some simple logical fallacy here...
			// Could just be that the code is brittle, as broken data could cause a type other than
			// `ADD_TO_EXISTING_ALL` to have a `null` enchant...
			Enchantment enchant = target.getEnchantment();
			if (enchantType.hasEnchantmentTag() && enchant != null) {
				out.put("boostedEnchant", enchant.getName());
				out.put("boostedEnchantFullName", enchant.getTranslatedName(1));
			} else {
				out.put("boostedEnchant", "all");
			}

			out.put("bonusLevel", target.getLevelAddition());

			return out;
		}

		@Nullable
		@Override
		public AmuletEnchantment getExample() {
			return null;
		}
	};


	@Nonnull
	@SuppressWarnings("SimplifiableIfStatement")
	private static String getConstellationTier(IConstellation constellation) {
		if (constellation instanceof IMinorConstellation) return "minor";
		if (constellation instanceof IMajorConstellation) return "major";
		return "weak";
	}
}
