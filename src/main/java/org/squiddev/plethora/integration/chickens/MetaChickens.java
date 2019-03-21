package org.squiddev.plethora.integration.chickens;

import com.google.common.collect.Maps;
import com.setycz.chickens.ChickensMod;
import com.setycz.chickens.config.ConfigHandler;
import com.setycz.chickens.entity.EntityChickensChicken;
import com.setycz.chickens.registry.ChickensRegistry;
import com.setycz.chickens.registry.ChickensRegistryItem;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.converter.DynamicConverter;
import org.squiddev.plethora.api.meta.BaseMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.api.meta.NamespacedMetaProvider;
import org.squiddev.plethora.api.method.IPartialContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

@Injects(ChickensMod.MODID)
public final class MetaChickens {
	private MetaChickens() {
	}

	@Nullable
	public static final DynamicConverter<Entity, EntityChickensChicken> GET_CHICKEN_FROM_ENTITY =
		entity -> entity instanceof EntityChickensChicken ? (EntityChickensChicken) entity : null;

	public static final IMetaProvider<EntityChickensChicken> META_ENTITY_CHICKEN = new NamespacedMetaProvider<>("chickens", context -> {
		Map<Object, Object> out = Maps.newHashMap();
		EntityChickensChicken chicken = context.getTarget();

		// Growth, gain, strength, layProgress, analyzed, tier
		out.put("analyzed", chicken.getStatsAnalyzed());
		out.put("tier", chicken.getTier());

		if (ConfigHandler.alwaysShowStats || chicken.getStatsAnalyzed() || context.getModules().hasModule(IntegrationChickens.ANALYZER_MOD)) {
			out.put("growth", chicken.getGrowth());
			out.put("gain", chicken.getGain());
			out.put("strength", chicken.getStrength());
		}

		//TODO Submit a PR for Chickens to expose this via getter
		NBTTagCompound nbt = new NBTTagCompound();
		chicken.writeEntityToNBT(nbt);
		String chickenType = nbt.getString("Type");
		out.put("type", chickenType);

		//Replicating Chickens internal code...
		//REFINE Should this be directly associated, or require a lookup against the registry?
		ChickensRegistryItem chickenDesc = ChickensRegistry.getByRegistryName(chickenType);
		if (chickenDesc != null) {
			out.put("layItem", context.makePartialChild(chickenDesc.createLayItem()).getMeta());
			out.put("dropItem", context.makePartialChild(chickenDesc.createDropItem()).getMeta());
		}

		return out;
	});

	//When modifying this provider, ensure that MethodsRoost.getVanillaChicken is also updated
	public static final IMetaProvider<ChickensRegistryItem> META_CHICKENS_REGISTRY_ITEM = new BaseMetaProvider<ChickensRegistryItem>() {
		@Nonnull
		@Override
		public Map<Object, Object> getMeta(@Nonnull IPartialContext<ChickensRegistryItem> context) {
			Map<Object, Object> out = Maps.newHashMap();
			ChickensRegistryItem chicken = context.getTarget();

			//Using key "type" for consistency
			out.put("type", chicken.getRegistryName().toString());
			//out.put("entityName", chicken.getEntityName());

			//REFINE This is a bit TOO verbose; determine the proper data to expose (e.g. display name or resource location)
			out.put("layItem", context.makePartialChild(chicken.createLayItem()).getMeta());
			out.put("dropItem", context.makePartialChild(chicken.createDropItem()).getMeta());

			out.put("tier", chicken.getTier());

			//REFINE Check if this is the value that we want here
			// We could put the full parent reference, but that could get a bit messy
			// in terms of reference management/GC, and data structure size
			ChickensRegistryItem parent1 = chicken.getParent1();
			out.put("parent1", parent1 == null ? null : parent1.getRegistryName().toString());

			ChickensRegistryItem parent2 = chicken.getParent2();
			out.put("parent2", parent2 == null ? null : parent2.getRegistryName().toString());

			//REFINE Do we want any more fields exposed?
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
