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
import org.squiddev.plethora.api.meta.NamespacedMetaProvider;

import java.util.Map;

@Injects(Roost.MODID)
public final class IntegrationRoost {
	private IntegrationRoost() {
	}

	public static final ConstantConverter<ItemStack, DataChicken> GET_DATA_CHICKEN_FROM_ITEM = DataChicken::getDataFromStack;

	public static final DynamicConverter<Entity, DataChicken> GET_DATA_CHICKEN_FROM_ENTITY = DataChicken::getDataFromEntity;

	public static final IMetaProvider<DataChicken> META_DATA_CHICKEN = new NamespacedMetaProvider<>("roost", context -> {
		Map<Object, Object> out = Maps.newHashMap();
		DataChicken chicken = context.getTarget();
		out.put("name", chicken.getName());

		//TODO Submit a PR to Roost to add getters...
		// Once the PR is accepted, Plethora can default to the more annoying method,
		// with a version check for the 'cleaner' method.
		// The NBT parsing version can then be dropped when migrating to 1.13

		if (chicken instanceof DataChickenModded) {
			DataChickenModded moddedChicken = (DataChickenModded) chicken;
			ItemStack chickenStack = moddedChicken.buildChickenStack();
			NBTTagCompound nbt = chickenStack.getTagCompound();

			out.put("growth", nbt.getInteger("Growth"));
			out.put("gain", nbt.getInteger("Gain"));
			out.put("strength", nbt.getInteger("Strength"));

			//Using key "type" for consistency with Chickens Mod
			out.put("type", nbt.getString("Chicken"));
		}
		else if (chicken instanceof DataChickenVanilla) {
			DataChickenVanilla vanillaChicken = (DataChickenVanilla) chicken;
			ItemStack chickenStack = vanillaChicken.buildChickenStack();
			NBTTagCompound nbt = chickenStack.getTagCompound();

			//Using key "type" for consistency with Chickens Mod
			out.put("type", nbt.getString("Chicken"));
		}

		return out;

	});

}
