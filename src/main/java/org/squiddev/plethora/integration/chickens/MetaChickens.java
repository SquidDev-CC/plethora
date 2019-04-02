package org.squiddev.plethora.integration.chickens;

import com.setycz.chickens.ChickensMod;
import com.setycz.chickens.config.ConfigHandler;
import com.setycz.chickens.entity.EntityChickensChicken;
import com.setycz.chickens.registry.ChickensRegistry;
import com.setycz.chickens.registry.ChickensRegistryItem;
import net.minecraft.nbt.NBTTagCompound;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.BaseMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.api.method.ContextHelpers;
import org.squiddev.plethora.api.method.IPartialContext;
import org.squiddev.plethora.utils.WorldDummy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Injects(ChickensMod.MODID)
public final class MetaChickens {
	private MetaChickens() {
	}

	public static final IMetaProvider<EntityChickensChicken> META_ENTITY_CHICKEN = new BaseMetaProvider<EntityChickensChicken>() {

		@Nonnull
		@Override
		public Map<Object, Object> getMeta(@Nonnull IPartialContext<EntityChickensChicken> context) {
			Map<Object, Object> out = new HashMap<>();
			EntityChickensChicken chicken = context.getTarget();

			// Growth, gain, strength, layProgress, analyzed, tier
			out.put("analyzed", chicken.getStatsAnalyzed());
			out.put("tier", chicken.getTier());

			//Unfortunately, captured chickens will only show their stats if already analyzed;
			// doubtful that we could show the Analyzer as being present from an attached
			// manipulator, at least without very messy hacks... calling it an intended challenge.
			if (ConfigHandler.alwaysShowStats || chicken.getStatsAnalyzed() || context.getModules().hasModule(IntegrationChickens.ANALYZER_MOD)) {
				out.put("growth", chicken.getGrowth());
				out.put("gain", chicken.getGain());
				out.put("strength", chicken.getStrength());
			}

			NBTTagCompound nbt = new NBTTagCompound();
			chicken.writeEntityToNBT(nbt);
			String chickenType = nbt.getString("Type");
			out.put("type", chickenType);

			//Replicating Chickens internal code...
			//Exposing these two fields directly for now; hardly warrants the player having to call `getSpecies`
			ChickensRegistryItem chickenDesc = ChickensRegistry.getByRegistryName(chickenType);
			if (chickenDesc != null) {
				out.put("layItem", ContextHelpers.wrapStack(context, chickenDesc.createLayItem()));
				out.put("dropItem", ContextHelpers.wrapStack(context, chickenDesc.createDropItem()));
			}

			return Collections.singletonMap("chickens", out);
		}

		@Nonnull
		@Override
		public EntityChickensChicken getExample() {
			EntityChickensChicken chicken = new EntityChickensChicken(WorldDummy.INSTANCE);
			chicken.setChickenType(ChickensRegistry.SMART_CHICKEN_ID.toString());
			return chicken;
		}
	};

	//When modifying this provider, ensure that MethodsRoost.getVanillaChicken is also updated
	public static final IMetaProvider<ChickensRegistryItem> META_CHICKENS_REGISTRY_ITEM = new BaseMetaProvider<ChickensRegistryItem>() {
		@Nonnull
		@Override
		public Map<Object, Object> getMeta(@Nonnull IPartialContext<ChickensRegistryItem> context) {
			Map<Object, Object> out = new HashMap<>();
			ChickensRegistryItem chicken = context.getTarget();

			//Using key "type" for consistency
			out.put("type", chicken.getRegistryName().toString());
			//out.put("entityName", chicken.getEntityName());

			//While a mix of verbosity and indirection, this avoids issues with
			//items identified by values such as "thermalfoundation:material:132" or similar,
			//and allows a user to potentially filter based on OreDict entries
			out.put("layItem", ContextHelpers.wrapStack(context, chicken.createLayItem()));
			out.put("dropItem", ContextHelpers.wrapStack(context, chicken.createDropItem()));

			out.put("tier", chicken.getTier());

			ChickensRegistryItem parent1 = chicken.getParent1();
			out.put("parent1", parent1 == null ? null : parent1.getRegistryName().toString());

			ChickensRegistryItem parent2 = chicken.getParent2();
			out.put("parent2", parent2 == null ? null : parent2.getRegistryName().toString());

			return out;
		}

		@Nullable
		@Override
		public ChickensRegistryItem getExample() {
			//While this example won't fully demonstrate all of the meta fields, it gives us a consistent example (if not disabled)
			return ChickensRegistry.getSmartChicken();
		}
	};

}
