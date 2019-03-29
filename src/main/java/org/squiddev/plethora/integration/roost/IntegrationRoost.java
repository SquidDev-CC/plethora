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

import java.util.HashMap;
import java.util.Map;

@Injects(Roost.MODID)
public final class IntegrationRoost {
	private IntegrationRoost() {
	}

	public static final ConstantConverter<ItemStack, DataChicken> GET_DATA_CHICKEN_FROM_ITEM = DataChicken::getDataFromStack;

	public static final DynamicConverter<Entity, DataChicken> GET_DATA_CHICKEN_FROM_ENTITY = DataChicken::getDataFromEntity;

	public static final IMetaProvider<DataChicken> META_DATA_CHICKEN = new NamespacedMetaProvider<>("roost", context -> {
		Map<Object, Object> out = new HashMap<>();
		DataChicken chicken = context.getTarget();
		out.put("name", chicken.getName());

		if (chicken instanceof DataChickenModded) {
			DataChickenModded moddedChicken = (DataChickenModded) chicken;
			NBTTagCompound nbt = moddedChicken.buildChickenStack().getTagCompound();

			if (nbt != null) {
				out.put("growth", nbt.getInteger("Growth"));

				out.put("gain", nbt.getInteger("Gain"));
				out.put("strength", nbt.getInteger("Strength"));

				//Using key "type" for consistency with Chickens Mod
				out.put("type", nbt.getString("Chicken"));
			}
		}
		else if (chicken instanceof DataChickenVanilla) {
			DataChickenVanilla vanillaChicken = (DataChickenVanilla) chicken;
			NBTTagCompound nbt = vanillaChicken.buildChickenStack().getTagCompound();

			//Using key "type" for consistency with Chickens Mod
			if (nbt != null) {
				out.put("type", nbt.getString("Chicken"));
			}
		}

		return out;

	});

}
