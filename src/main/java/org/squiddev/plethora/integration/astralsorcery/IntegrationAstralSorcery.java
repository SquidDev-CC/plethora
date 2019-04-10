package org.squiddev.plethora.integration.astralsorcery;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.constellation.*;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ResearchManager;
import hellfirepvp.astralsorcery.common.item.ItemConstellationPaper;
import hellfirepvp.astralsorcery.common.item.ItemJournal;
import hellfirepvp.astralsorcery.common.item.base.ItemConstellationFocus;
import hellfirepvp.astralsorcery.common.item.crystal.CrystalProperties;
import hellfirepvp.astralsorcery.common.item.crystal.CrystalPropertyItem;
import hellfirepvp.astralsorcery.common.item.crystal.base.ItemRockCrystalBase;
import hellfirepvp.astralsorcery.common.item.crystal.base.ItemTunedCrystalBase;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.api.meta.ItemStackContextMetaProvider;
import org.squiddev.plethora.api.method.IPartialContext;
import org.squiddev.plethora.utils.EntityPlayerDummy;
import org.squiddev.plethora.utils.WorldDummy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Injects(AstralSorcery.MODID)
public final class IntegrationAstralSorcery {
	private IntegrationAstralSorcery() {
	}

	/* Note that we will mainly be adding meta-providers for Astral Sorcery,
	 * as I do NOT want to deal with HellfirePvP's stance on fake players
	 * ( {@see hellfirepvp.astralsorcery.common.util.MiscUtils.isPlayerFakeMP})
	 *
	 */

	/*MEMO The following pieces need to be set up for the test environment to work properly:
	 * Bump the Forge version to AT LEAST 14.23.5.2781
	 * Disable the Patreon flare effects for Astral (it will crash otherwise... >_> )
	 */

	/*Providers to add:
	 * Constellation Paper - constellation name
	 * Crystal Properties
	 * Constellation attunement - items, players; TODO Check if other entities can be attuned
	 * Astral Tome - Stored constellation papers -- itemjournal
	 * Grindstone - Expose item currently placed
	 *
	 * Many multiblocks _CANNOT_ have providers nor methods, as they do not
	 * permit any other blocks to intrude into their space.  Period.
	 * Not even if you return `true` for `isAir()`.
	 */

	//TODO Research a 'Phantomface' or 'Transvector Interface' type modem; players would probably like to at least see if rituals need repair...
	// Or would the meta be exposed okay using a Block Scanner?

	/*TODO Research the following Astral classes:
	 * ItemGatedVisibility
	 * PlayerProgress
	 * EnumGatedKnowledge
	 * ProgressionTier
	 */

	//TODO Doesn't appear to expose values on item forms of Collector Crystals?
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

			Map<String, Object> out = new HashMap<>(3);

			out.put("cutting", properties.getCollectiveCapability());
			out.put("size", properties.getSize());
			out.put("purity", properties.getPurity());

			out.put("fracture", properties.getFracturation());

			//TODO Review the other properties and determine if we should expose them
			// In particular the size override (what does this even DO?)
			out.put("sizeOverride", properties.getSizeOverride());

			//REFINE Not happy with this namespace...
			return Collections.singletonMap("astralCrystal", out);
		}

		@Nonnull
		@Override
		public ItemStack getExample() {
			return ItemRockCrystalBase.createMaxBaseCrystal();
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

			//TODO Test this; Astral's code looks like it may actually switch these two fields...
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

	public static final IMetaProvider<EntityPlayer> META_PLAYER_PROGRESS = new BasicMetaProvider<EntityPlayer>(
		"Provides the player's progress in Astral Sorcery"
	) {

		@Nonnull
		@Override
		public Map<String, Object> getMeta(@Nonnull EntityPlayer context) {
			Map<String, Object> out = new HashMap<>();

			//TODO Should we use the overload that specifies the side?
			// Need to compare these calls...
			PlayerProgress clientProgress = ResearchManager.getProgress(context, Side.CLIENT);
			PlayerProgress serverProgress = ResearchManager.getProgress(context, Side.SERVER);
			PlayerProgress generalProgress = ResearchManager.getProgress(context);


			//FIXME Implement this...
			// Actually, what do we even want to expose here?

			return out;
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

			//TODO Check if we already handle colors (e.g. do I even need the `String.format` calls?)
			out.put("color", String.format("%1x", context.getConstellationColor().getRGB())); //Used for particles on rituals, collectors?
			out.put("colour", String.format("%1x", context.getConstellationColor().getRGB()));
			out.put("tierColor", String.format("%1x", context.getTierRenderColor().getRGB()));
			out.put("tierColour", String.format("%1x", context.getTierRenderColor().getRGB()));

			//Because there isn't a nice, clean `getTier` method...
			out.put("tier", context instanceof IMinorConstellation ? "minor"
				: context instanceof IMajorConstellation ? "major" : "weak");

			//Exposing the stars and the connections wouldn't help players much, unless they want to visualize
			// constellations on a computer...?

			//TODO Determine what objects directly expose this interface, and thereby what namespace is appropriate
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
		"FIXME Set the description"
	) {
		@Nonnull
		@Override
		public Map<String, Object> getMeta(@Nonnull IPartialContext<ItemStack> context, @Nonnull ItemJournal item) {
			ItemStack stack = context.getTarget();
			//FIXME Implement this
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
