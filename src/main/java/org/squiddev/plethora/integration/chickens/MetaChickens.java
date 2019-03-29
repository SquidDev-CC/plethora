package org.squiddev.plethora.integration.chickens;

import com.setycz.chickens.ChickensMod;
import com.setycz.chickens.config.ConfigHandler;
import com.setycz.chickens.entity.EntityChickensChicken;
import com.setycz.chickens.registry.ChickensRegistry;
import com.setycz.chickens.registry.ChickensRegistryItem;
import dan200.computercraft.api.lua.ILuaObject;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.BaseMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.api.meta.NamespacedMetaProvider;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.IPartialContext;
import org.squiddev.plethora.core.ContextFactory;
import org.squiddev.plethora.core.executor.BasicExecutor;
import org.squiddev.plethora.integration.MetaWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

@Injects(ChickensMod.MODID)
public final class MetaChickens {
	private MetaChickens() {
	}

	public static final IMetaProvider<EntityChickensChicken> META_ENTITY_CHICKEN = new NamespacedMetaProvider<>("chickens", context -> {
		Map<Object, Object> out = new HashMap<>();
		EntityChickensChicken chicken = context.getTarget();

		// Growth, gain, strength, layProgress, analyzed, tier
		out.put("analyzed", chicken.getStatsAnalyzed());
		out.put("tier", chicken.getTier());

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
			out.put("layItem", wrapStack(context, chickenDesc.createLayItem()));
			out.put("dropItem", wrapStack(context, chickenDesc.createDropItem()));
		}

		return out;
	});

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
			out.put("layItem", wrapStack(context, chicken.createLayItem()));
			out.put("dropItem", wrapStack(context, chicken.createDropItem()));

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

	//Copied from integration.vanilla.meta.MetaEntityLiving
	@Nullable
	private static ILuaObject wrapStack(IPartialContext<?> context, @Nullable ItemStack object) {
		if (object == null || object.isEmpty()) return null;

		MetaWrapper<ItemStack> wrapper = MetaWrapper.of(object.copy());
		if (context instanceof IContext) {
			return ((IContext<?>) context).makeChildId(wrapper).getObject();
		} else {
			return ContextFactory.of(wrapper).withExecutor(BasicExecutor.INSTANCE).getObject();
		}
	}

}
