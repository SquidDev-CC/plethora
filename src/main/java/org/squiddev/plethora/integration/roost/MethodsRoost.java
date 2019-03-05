package org.squiddev.plethora.integration.roost;

import com.timwoodcreates.roost.Roost;
import com.timwoodcreates.roost.data.DataChicken;
import com.timwoodcreates.roost.data.DataChickenModded;
import com.timwoodcreates.roost.data.DataChickenVanilla;
import com.timwoodcreates.roost.tileentity.TileEntityBreeder;
import org.squiddev.plethora.api.method.BasicObjectMethod;
import org.squiddev.plethora.api.method.IContext;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

public class MethodsRoost {

	@BasicObjectMethod.Inject(
			value = TileEntityBreeder.class, modId = Roost.MODID,
			doc = "function():table -- Get the chicken breeding data"
	)
	public static Object[] getBreedingData(IContext<TileEntityBreeder> context, Object[] arg) {
		List<DataChicken> chickenList = DataChicken.getAllChickens();
		for (DataChicken chicken : chickenList){
			//Unlike Forestry, we don't have a separate genetic registry in Roost
			//This means either we hand-build the breeding data/mutation list,
			//or we expose extra data on the items themselves...

			//REFINE Wish I could recall how generics behaved well enough to know if I even need the
			// instanceof checks...

		}

		//FIXME Not an actual return!
		return new Object[]{};
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
