package org.squiddev.plethora.integration.roost;

import com.setycz.chickens.ChickensMod;
import com.setycz.chickens.registry.ChickensRegistry;
import com.timwoodcreates.roost.Roost;
import com.timwoodcreates.roost.data.DataChicken;
import com.timwoodcreates.roost.data.DataChickenModded;
import com.timwoodcreates.roost.data.DataChickenVanilla;
import com.timwoodcreates.roost.tileentity.TileEntityBreeder;
import net.minecraftforge.fml.common.Loader;
import org.squiddev.plethora.api.method.BasicObjectMethod;
import org.squiddev.plethora.api.method.IContext;

import javax.annotation.Nonnull;

public class MethodsRoost {

	@BasicObjectMethod.Inject(
			value = TileEntityBreeder.class, modId = Roost.MODID,
			doc = "function():table -- Get the chicken breeding data"
	)
	public static Object[] getBreedingData(IContext<TileEntityBreeder> context, Object[] arg) {
		if (Loader.isModLoaded(ChickensMod.MODID)) {
			//TODO Test if this contains an equivalent to Roosts' "DataChickenVanilla"
			//FIXME Appears to be returning an empty array, assuming this path is traversed?
			return ChickensRegistry.getItems().toArray();
		}

		//FIXME Determine a proper return value
		// If Chickens isn't loaded, then Roost only registers DataChickenVanilla
		// This, in turn, doesn't have any breeding pairs,
		// but it will expose different methods than a ChickensRegistryItem
		return new Object[0];
	}

	//TODO Right, if I go this route then I need to define another converter...
	public class ChickenMetaWrapper {
		public ChickenMetaWrapper(@Nonnull DataChicken chikkin) {

		}

		public ChickenMetaWrapper(@Nonnull DataChickenModded chikkin) {

		}

		public ChickenMetaWrapper(@Nonnull DataChickenVanilla chikkin) {

		}

	}

}
