package org.squiddev.plethora.integration.roost;

import com.setycz.chickens.ChickensMod;
import com.timwoodcreates.roost.Roost;
import com.timwoodcreates.roost.data.DataChicken;
import com.timwoodcreates.roost.data.DataChickenModded;
import com.timwoodcreates.roost.data.DataChickenVanilla;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.Loader;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.converter.ConstantConverter;
import org.squiddev.plethora.api.converter.DynamicConverter;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

@Injects(Roost.MODID)
public final class IntegrationRoost {
	private IntegrationRoost() {
	}

	public static final ConstantConverter<ItemStack, DataChicken> GET_DATA_CHICKEN_FROM_ITEM = DataChicken::getDataFromStack;

	public static final DynamicConverter<Entity, DataChicken> GET_DATA_CHICKEN_FROM_ENTITY = DataChicken::getDataFromEntity;

	//REFINE Can we have multiple providers push into the same namespace
	// without code duplication or making a mess merging the maps at runtime?
	// In the meantime, the base `DataChicken` ignores its subtypes,
	// and the subtype providers also include the 'name' data.
	public static final IMetaProvider<DataChicken> META_DATA_CHICKEN = new BasicMetaProvider<DataChicken>() {

		@Nonnull
		@Override
		public Map<Object, Object> getMeta(@Nonnull DataChicken context) {
			//Hack?  Maybe.  Works?  For now...
			return context instanceof DataChickenVanilla || context instanceof DataChickenModded
				? Collections.emptyMap()
				: Collections.singletonMap("roost", Collections.singletonMap("name", context.getName()));
		}

		@Nonnull
		@Override
		public DataChicken getExample() {
			return new DataChicken("vanilla", "entity.Chicken.name");
		}
	};

	public static final IMetaProvider<DataChickenVanilla> META_DATA_CHICKEN_VANILLA = new BasicMetaProvider<DataChickenVanilla>() {
		@Nonnull
		@Override
		public Map<Object, Object> getMeta(@Nonnull DataChickenVanilla context) {
			Map<Object, Object> out = new HashMap<>(2);
			NBTTagCompound nbt = context.buildChickenStack().getTagCompound();

			out.put("name", context.getName());

			//Using key "type" for consistency with Chickens Mod
			//
			//Honestly, a valid `DataChickenVanilla` should _ALWAYS_ have the same 'type' of "minecraft:chicken",
			//if we would want to optimize this out...
			if (nbt != null) out.put("type", nbt.getString("Chicken"));

			return Collections.singletonMap("roost", out);
		}

		@Nonnull
		@Override
		public DataChickenVanilla getExample() {
			return new DataChickenVanilla();
		}
	};

	public static final IMetaProvider<DataChickenModded> META_DATA_CHICKEN_MODDED = new BasicMetaProvider<DataChickenModded>() {

		@Nonnull
		@Override
		public Map<Object, Object> getMeta(@Nonnull DataChickenModded context) {
			NBTTagCompound nbt = context.buildChickenStack().getTagCompound();
			if (nbt == null) {
				return Collections.singletonMap("roost", Collections.singletonMap("name", context.getName()));
			}

			Map<Object, Object> out = new HashMap<>(5);
			out.put("name", context.getName());

			out.put("growth", nbt.getInteger("Growth"));
			out.put("gain", nbt.getInteger("Gain"));
			out.put("strength", nbt.getInteger("Strength"));

			//Using key "type" for consistency with Chickens Mod
			out.put("type", nbt.getString("Chicken"));

			return Collections.singletonMap("roost", out);
		}

		@Nullable
		@Override
		public DataChickenModded getExample() {
			if (!Loader.isModLoaded(ChickensMod.MODID)) return null;

			//Messy, and replicates Roost internal code, but...
			List<DataChicken> chickenList = new LinkedList<>();
			DataChickenModded.addAllChickens(chickenList);
			if (chickenList.isEmpty()) return null;

			//The list should _only_ contain `DataChickenModded` entries, but...
			DataChicken chicken = chickenList.get(0);
			return chicken instanceof DataChickenModded ? (DataChickenModded) chicken : null;
		}
	};

}
