package org.squiddev.plethora.integration.chickens;

import com.google.common.collect.Maps;
import com.setycz.chickens.ChickensMod;
import com.setycz.chickens.entity.EntityChickensChicken;
import com.setycz.chickens.registry.ChickensRegistry;
import com.setycz.chickens.registry.ChickensRegistryItem;
import net.minecraft.nbt.NBTTagCompound;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.api.meta.NamespacedMetaProvider;

import java.util.Map;

import static org.squiddev.plethora.integration.PlethoraIntegration.LOG;

@Injects(ChickensMod.MODID)
public final class MetaChickens {
	private MetaChickens() {
	}

	//TODO Define a converter for Entity to EntityChickensChicken

	public static final IMetaProvider<EntityChickensChicken> META_ENTITY_CHICKEN = new NamespacedMetaProvider<>("chickens", object -> {
		Map<Object, Object> out = Maps.newHashMap();
		EntityChickensChicken chicken = object.getTarget();

		LOG.info("Chicken found as: {}", chicken);

		// Growth, gain, strength, layProgress, analyzed, tier
		out.put("analyzed", chicken.getStatsAnalyzed());
		out.put("tier", chicken.getTier());

		if (chicken.getStatsAnalyzed()) {
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
			out.put("layItem", object.makePartialChild(chickenDesc.createLayItem()).getMeta());
			out.put("dropItem", object.makePartialChild(chickenDesc.createDropItem()).getMeta());
		}

		LOG.info("EntityChickensChicken decoded as: {}", out);

		return out;
	});

	public static final IMetaProvider<ChickensRegistryItem> META_CHICKENS_REGISTRY_ITEM = new NamespacedMetaProvider<>("chickens", object -> {
		Map<Object, Object> out = Maps.newHashMap();
		ChickensRegistryItem chicken = object.getTarget();

		out.put("name", chicken.getRegistryName().toString());
		out.put("entityName", chicken.getEntityName());

		out.put("layItem", object.makePartialChild(chicken.createLayItem()).getMeta());
		out.put("dropItem", object.makePartialChild(chicken.createDropItem()).getMeta());

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
	});

}
