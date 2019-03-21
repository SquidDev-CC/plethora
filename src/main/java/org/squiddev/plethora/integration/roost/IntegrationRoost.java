package org.squiddev.plethora.integration.roost;

import com.google.common.collect.Maps;
import com.timwoodcreates.roost.Roost;
import com.timwoodcreates.roost.data.DataChicken;
import com.timwoodcreates.roost.data.DataChickenModded;
import com.timwoodcreates.roost.data.DataChickenVanilla;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.converter.ConstantConverter;
import org.squiddev.plethora.api.converter.DynamicConverter;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.api.meta.ItemStackContextMetaProvider;
import org.squiddev.plethora.api.meta.NamespacedMetaProvider;
import org.squiddev.plethora.api.method.IPartialContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Injects(Roost.MODID)
public final class IntegrationRoost {
	private IntegrationRoost() {
	}

	//TODO Define a meta-provider for Vanilla's EntityChicken

	public static final ConstantConverter<ItemStack, DataChicken> GET_DATA_CHICKEN_FROM_ITEM = DataChicken::getDataFromStack;

	//TODO Test whether this causes issues when Chickens integration is implemented, as that
	//  may _ALSO_ get a representation of the entity's stats...
	public static final DynamicConverter<Entity, DataChicken> GET_DATA_CHICKEN_FROM_ENTITY = DataChicken::getDataFromEntity;

	//REFINE Do we want to use 'roost' for the namespace, 'chicken', or something else?
	public static final IMetaProvider<DataChicken> META_DATA_CHICKEN = new NamespacedMetaProvider<>("roost", context -> {
		Map<Object, Object> out = Maps.newHashMap();
		DataChicken chicken = context.getTarget();
		out.put("name", chicken.getName());

		//TODO Unless we can get the ItemStackContextMetaProvider version to work,
		// we will have to cast to the actual subtype of DataChicken and use buildChickenStack
		// to get the full NBT.
		//TODO Submit a PR to Roost to add getters...
		// Once the PR is accepted, Plethora can default to the more annoying method,
		// with a version check for the 'cleaner' method.
		// The NBT parsing version can then be dropped when migrating to 1.13

		//Well, referencing DataChickenModded doesn't appear to cause issues in Roost
		// if Chickens isn't loaded, so...
		if (chicken instanceof DataChickenModded) {
			DataChickenModded moddedChicken = (DataChickenModded) chicken;

			NBTTagCompound nbt = moddedChicken.createTagCompound();

			//This _shouldn't_ throw errors; if it does, we have a malformed chicken!
			//That, or Roost changed their (private) NBT key IDs...
			out.put("growth", nbt.getInteger("Growth"));
			out.put("gain", nbt.getInteger("Gain"));
			out.put("strength", nbt.getInteger("Strength"));

			//REFINE Test whether this is needed/the same as chicken.getName()
			//TODO Determine if there is a way to pull this from the item stack
			// This field isn't included in createTagCompound... >:(
			// May need to convert to an ItemStackContextMetaProvider...?
			out.put("chicken", nbt.getString("Chicken"));
		}

		return out;

	});

	//FIXME This provider, and the variants for subclasses of DataChicken, don't appear to be registering?
	// The debug log records "Registering value of @Injects field ...", but in-game I do not get any
	// "roost2" meta records on either subclass
	//Technically, this specific meta provider shouldn't be needed, as all instances should be one of the subtypes, but...
	public static final IMetaProvider<ItemStack> META_DATA_CHICKEN_BASE = new ItemStackContextMetaProvider<DataChicken>(
			DataChicken.class
	) {

		@Nonnull
		@Override
		public Map<Object, Object> getMeta(@Nonnull IPartialContext<ItemStack> context, @Nonnull DataChicken item) {
			return getGeneralizedMeta(context, item);
		}

		@Nullable
		@Override
		public ItemStack getExample() {
			return null;
		}
	};

	public static final IMetaProvider<ItemStack> META_DATA_CHICKEN_VANILLA = new ItemStackContextMetaProvider<DataChickenVanilla>(
			DataChickenVanilla.class
	) {

		@Nonnull
		@Override
		public Map<Object, Object> getMeta(@Nonnull IPartialContext<ItemStack> context, @Nonnull DataChickenVanilla item) {
			return getGeneralizedMeta(context, item);
		}

		@Nullable
		@Override
		public ItemStack getExample() {
			return new DataChickenVanilla().buildChickenStack();
		}
	};

	//TODO Test if this will cause any issues if we only have Roost and not Chickens
	public static final IMetaProvider<ItemStack> META_DATA_CHICKEN_MODDED = new ItemStackContextMetaProvider<DataChickenModded>(
			DataChickenModded.class
	) {

		@Nonnull
		@Override
		public Map<Object, Object> getMeta(@Nonnull IPartialContext<ItemStack> context, @Nonnull DataChickenModded item) {
			return getGeneralizedMeta(context, item);
		}

		@Nullable
		@Override
		public ItemStack getExample() {
			List<DataChicken> chickenList = new ArrayList<>();
			DataChickenModded.addAllChickens(chickenList);
			return chickenList.isEmpty() ? null : chickenList.get(0).buildChickenStack();
		}
	};


	private static Map<Object, Object> getGeneralizedMeta(@Nonnull IPartialContext<ItemStack> context, @Nonnull DataChicken item) {
		//REFINE Implement checks against malformed NBT
		// Can be caused by corruption, NBT editors, improperly tweaked recipes...
		Map<Object, Object> details = Maps.newHashMap();
		NBTTagCompound nbt = context.getTarget().getTagCompound();

		details.put("name", item.getName());
		details.put("chicken", nbt.getString("Chicken"));

		if (item instanceof DataChickenModded) {
			//Unlike the original code, we already have the NBT
			//The type check merely tells us if certain tags should be present

			//This _shouldn't_ throw errors; if it does, we have a malformed chicken!
			//That, or Roost changed their (private) NBT key IDs...
			details.put("growth", nbt.getInteger("Growth"));
			details.put("gain", nbt.getInteger("Gain"));
			details.put("strength", nbt.getInteger("Strength"));
		}

		return Collections.singletonMap("roost2", details);
	}
}
