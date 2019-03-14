package org.squiddev.plethora.integration.roost;

import com.setycz.chickens.ChickensMod;
import com.setycz.chickens.registry.ChickensRegistry;
import com.timwoodcreates.roost.Roost;
import com.timwoodcreates.roost.data.DataChicken;
import com.timwoodcreates.roost.data.DataChickenModded;
import com.timwoodcreates.roost.data.DataChickenVanilla;
import com.timwoodcreates.roost.tileentity.TileEntityBreeder;
import net.minecraftforge.fml.common.Loader;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.wrapper.PlethoraMethod;
import org.squiddev.plethora.utils.LuaList;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

public class MethodsRoost {

	@PlethoraMethod(
			modId = Roost.MODID,
			doc = "-- Get a list of chickens species"
	)
	public static Map<Integer, Object> getSpeciesList(@Nonnull IContext<TileEntityBreeder> context) {
		if (Loader.isModLoaded(ChickensMod.MODID)) {
			LuaList<Object> species = ChickensRegistry.getItems().stream()
					.map(m -> context.makePartialChild(m).getMeta())
					.collect(LuaList.toLuaList());

			return species.asMap();
		}

		//FIXME Determine a proper return value
		// If Chickens isn't loaded, then Roost only registers DataChickenVanilla
		// This, in turn, doesn't have any breeding pairs,
		// but it will expose different methods than a ChickensRegistryItem
		return Collections.emptyMap();
	}

}
