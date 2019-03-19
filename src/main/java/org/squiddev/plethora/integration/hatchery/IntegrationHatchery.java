package org.squiddev.plethora.integration.hatchery;

import com.gendeathrow.hatchery.Hatchery;
import com.gendeathrow.hatchery.core.init.ModItems;
import com.gendeathrow.hatchery.item.AnimalNet;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.util.Constants;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.api.meta.ItemStackContextMetaProvider;
import org.squiddev.plethora.api.method.ContextKeys;
import org.squiddev.plethora.api.method.IPartialContext;
import org.squiddev.plethora.utils.Helpers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Injects(Hatchery.MODID)
public final class IntegrationHatchery {

	//TODO Determine what integrations we need
	/*Hatchery functionality:
	 * Lucky Egg Machine
	 * Feeder
	 * Fertilized dirt
	 * Fertilizer
	 * Fertilizer Mixer
	 * Generator
	 * Manure
	 * Nest
	 * Nest pen
	 * Nursery
	 * Shredder
	 * Chicken feed
	 * Chicken manure
	 * Fluid pump
	 * Hatchery egg
	 * ItemChickenMachine - ??
	 * Prize Egg
	 * Sprayer
	 * Upgrades
	 * ChickensHelper - may be a useful reference?
	 */

	public static final IMetaProvider<ItemStack> META_ANIMAL_NET = new ItemStackContextMetaProvider<AnimalNet>(
			AnimalNet.class,
			"Provides the entity captured inside this Animal Net."
	) {

		//TODO All three (so far) classes that expose captured entity data have many of the same checks;
		// Is there any way that we can abstract the complexity/deduplicate the code?
		@Nonnull
		@Override
		public Map<Object, Object> getMeta(@Nonnull IPartialContext<ItemStack> context, @Nonnull AnimalNet item) {
			//Check for a captured entity
			ItemStack netStack = context.getTarget();
			if (!AnimalNet.hasCapturedAnimal(netStack)) return Collections.emptyMap();

			//Check for an entity ID
			NBTTagCompound entityData = AnimalNet.getCapturedAnimalNBT(netStack);
			if (!entityData.hasKey("id", Constants.NBT.TAG_STRING)) return Collections.emptyMap();

			//Check if we have a location
			IWorldLocation location = context.getContext(ContextKeys.ORIGIN, IWorldLocation.class);
			if (location == null) return getBasicDetails(entityData);

			//Only the empty AnimalNet is registered, so we don't need to check for a generic
			Entity entity = EntityList.createEntityFromNBT(entityData, location.getWorld());
			if (entity == null) return getBasicDetails(entityData);

			Vec3d loc = location.getLoc();
			entity.setPositionAndRotation(loc.x, loc.y, loc.z, 0, 0);
			return Collections.singletonMap("capturedEntity", context.makePartialChild(entity).getMeta());
		}

		//Copied wholesale from IntegrationThermalExpansion.META_MORB
		private Map<Object, Object> getBasicDetails(NBTTagCompound entityData) {
			String translationKey = EntityList.getTranslationName(new ResourceLocation(entityData.getString("id")));
			if (translationKey == null) return Collections.emptyMap();

			String translated = Helpers.translateToLocal("entity." + translationKey + ".name");

			Map<Object, Object> details = new HashMap<>(2);
			details.put("name", translated);
			details.put("displayName",
					entityData.hasKey("CustomName", Constants.NBT.TAG_STRING)
							? entityData.getString("CustomName")
							: translated
			);
			return Collections.singletonMap("capturedEntity", details);
		}

		@Nullable
		@Override
		public ItemStack getExample() {
			//TODO Cannot cleanly generate an example, as the public methods on AnimalNet
			// require a parameter of type EntityPlayer, and manually constructing the
			// stack would be very brittle, as we would be relying entirely upon
			// implementation details not changing...
			//ModItems.animalNet.getDefaultInstance();
			return null;
		}
	};

}
