package org.squiddev.plethora.integration.roost;

import com.google.common.collect.Maps;
import com.timwoodcreates.roost.Roost;
import com.timwoodcreates.roost.data.DataChicken;
import com.timwoodcreates.roost.data.DataChickenModded;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.converter.ConstantConverter;
import org.squiddev.plethora.api.converter.DynamicConverter;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.api.meta.NamespacedMetaProvider;
import org.squiddev.plethora.gameplay.ConfigGameplay;
import org.squiddev.plethora.integration.PlethoraIntegration;

import java.util.Map;

@Injects(Roost.MODID)
public final class IntegrationRoost {
	private IntegrationRoost() {
	}

	public static final ConstantConverter<ItemStack, DataChicken> GET_DATA_CHICKEN_FROM_ITEM = DataChicken::getDataFromStack;

	//TODO Test whether this causes issues when Chickens integration is implemented, as that
	//  may _ALSO_ get a representation of the entity's stats...
	//REFINE Can/should this be tuned to use EntityReference?  getDataFromEntity should handle 'null' entities...
	public static final DynamicConverter<Entity, DataChicken> GET_DATA_CHICKEN_FROM_ENTITY = DataChicken::getDataFromEntity;

	//REFINE Do we want to use 'roost' for the namespace, 'chicken', or something else?
	public static final IMetaProvider<DataChicken> META_DATA_CHICKEN = new NamespacedMetaProvider<>("roost", object -> {
		Map<Object, Object> out = Maps.newHashMap();
		DataChicken chicken = object.getTarget();
		out.put("name", chicken.getName());

		//Well, referencing DataChickenModded doesn't appear to cause issues in Roost
		// if Chickens isn't loaded, so...
		if (chicken instanceof DataChickenModded) {
			DataChickenModded moddedChicken = (DataChickenModded) chicken;

			//TODO Submit a PR to Roost to add getters...
			NBTTagCompound nbt = moddedChicken.createTagCompound();

			//This _shouldn't_ throw errors; if it does, we have a malformed chicken!
			//That, or Roost changed their (private) NBT key IDs...
			out.put("growth", nbt.getInteger("Growth"));
			out.put("gain", nbt.getInteger("Gain"));
			out.put("strength", nbt.getInteger("Strength"));

			//REFINE Test whether this is needed/the same as chicken.getName()
			// Odd, this appears to be an empty string?
			out.put("chicken", nbt.getString("Chicken"));
		}

		return out;

	});
}
