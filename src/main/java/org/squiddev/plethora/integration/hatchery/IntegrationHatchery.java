package org.squiddev.plethora.integration.hatchery;

import com.gendeathrow.hatchery.Hatchery;
import com.gendeathrow.hatchery.block.nest.EggNestTileEntity;
import com.gendeathrow.hatchery.block.nestpen.NestPenTileEntity;
import com.gendeathrow.hatchery.core.init.ModItems;
import com.gendeathrow.hatchery.entities.EntityRooster;
import com.gendeathrow.hatchery.item.AnimalNet;
import com.gendeathrow.hatchery.item.HatcheryEgg;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.util.Constants;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.BaseMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.api.meta.ItemStackContextMetaProvider;
import org.squiddev.plethora.api.meta.NamespacedMetaProvider;
import org.squiddev.plethora.api.method.ContextKeys;
import org.squiddev.plethora.api.method.IPartialContext;
import org.squiddev.plethora.utils.EntityPlayerDummy;
import org.squiddev.plethora.utils.Helpers;
import org.squiddev.plethora.utils.WorldDummy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Injects(Hatchery.MODID)
public final class IntegrationHatchery {
	private IntegrationHatchery() {
	}

	/*Hatchery functionality:
	 * Lucky Egg Machine
	 * Feeder
	 * Fertilized dirt
	 * Fertilizer
	 * Fertilizer Mixer
	 * Generator
	 * Manure
	 * Nursery
	 * Shredder
	 * Chicken feed
	 * Chicken manure
	 * Fluid pump
	 * ItemChickenMachine - ??
	 * Prize Egg
	 * Sprayer
	 * Upgrades
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
			EntityPlayer dummyPlayer = new EntityPlayerDummy(WorldDummy.INSTANCE);
			ItemStack netStack = ModItems.animalNet.getDefaultInstance();
			ResourceLocation cowLocation = EntityList.getKey(EntityCow.class);
			if (cowLocation == null) return null;

			Entity cow = EntityList.createEntityByIDFromName(cowLocation, WorldDummy.INSTANCE);
			return cow == null ? null : AnimalNet.addEntitytoNet(dummyPlayer, netStack, cow);
		}
	};

	//Because of COURSE it stores a raw entity...
	public static final IMetaProvider<ItemStack> META_HATCHERY_EGG = new ItemStackContextMetaProvider<HatcheryEgg>(
		HatcheryEgg.class,
		"Provides the entity that may spawn from this Egg"
	) {
		@Nonnull
		@Override
		public Map<Object, Object> getMeta(@Nonnull IPartialContext<ItemStack> context, @Nonnull HatcheryEgg item) {
			//Check for a captured entity
			NBTTagCompound nbt = context.getTarget().getTagCompound();
			if (nbt == null || !nbt.hasKey("storedEntity")) return Collections.emptyMap();

			//Check for an entity ID
			NBTTagCompound entityData = nbt.getCompoundTag("storedEntity");
			if (!entityData.hasKey("id", Constants.NBT.TAG_STRING)) return Collections.emptyMap();

			//Check if we have a location
			IWorldLocation location = context.getContext(ContextKeys.ORIGIN, IWorldLocation.class);
			if (location == null) return getBasicDetails(entityData);

			//Due to Hatchery's `ItemStackEntityNBTHelper` having a `saveAll` param, we may need to
			// handle a generic egg.  Unfortunately, they don't provide a convenient analogue to TE's `ItemMorb.GENERIC`,
			// so this will have to suffice...
			Entity entity = EntityList.createEntityFromNBT(entityData, location.getWorld());
			if (entity == null)
				entity = EntityList.createEntityByIDFromName(new ResourceLocation(entityData.getString("id")), location.getWorld());
			if (entity == null) return getBasicDetails(entityData);

			Vec3d loc = location.getLoc();
			entity.setPositionAndRotation(loc.x, loc.y, loc.z, 0, 0);
			return Collections.singletonMap("spawnEntity", context.makePartialChild(entity).getMeta());
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
			return Collections.singletonMap("spawnEntity", details);
		}

		@Nullable
		@Override
		public ItemStack getExample() {
			//REFINE What are the trade-offs between this technique and `new ResourceLocation("minecraft:chicken")`?
			ResourceLocation chickenLocation = EntityList.getKey(EntityChicken.class);
			if (chickenLocation == null) return null;

			//REFINE Similarly, how does this compare with `new EntityChicken(WorldDummy.INSTANCE)`
			Entity chicken = EntityList.createEntityByIDFromName(chickenLocation, WorldDummy.INSTANCE);
			if (!(chicken instanceof EntityAgeable)) return null;

			return HatcheryEgg.createEggFromEntity(WorldDummy.INSTANCE, (EntityAgeable) chicken);
		}
	};

	public static final IMetaProvider<EntityRooster> META_ROOSTER = new BaseMetaProvider<EntityRooster>() {

		@Nonnull
		@Override
		public Map<Object, Object> getMeta(@Nonnull IPartialContext<EntityRooster> context) {
			Map<Object, Object> out = new HashMap<>();
			EntityRooster rooster = context.getTarget();

			//Somewhat of a misnomer; this is the 'energy' level, comparable to a furnace's remaining burn ticks
			//The actual 'temptation items' (Hatchery's term, not mine) is exposed via the Item Handler Capability,
			//TODO How can I expose the Rooster's inventory in a manner consistent with the rest of the code?
			// I can't bind an Introspection Module to it...
			out.put("seeds", rooster.getSeeds());

			return out;
		}

		@Nullable
		@Override
		public EntityRooster getExample() {
			//REFINE See META_HATCHERY_EGG#getExample's task
			ResourceLocation roosterLocation = EntityList.getKey(EntityRooster.class);
			if (roosterLocation == null) return null;

			Entity rooster = EntityList.createEntityByIDFromName(roosterLocation, WorldDummy.INSTANCE);
			if (!(rooster instanceof EntityRooster)) return null;

			return (EntityRooster) rooster;
		}
	};

	public static final IMetaProvider<NestPenTileEntity> META_NESTPEN = new BaseMetaProvider<NestPenTileEntity>() {

		@Nonnull
		@Override
		public Map<Object, Object> getMeta(@Nonnull IPartialContext<NestPenTileEntity> context) {
			Map<Object, Object> out = new HashMap<>();
			NestPenTileEntity target = context.getTarget();

			//Get the stored entity
			EntityAgeable storedEntity = target.storedEntity();
			if (storedEntity != null) out.put("storedEntity", context.makePartialChild(storedEntity).getMeta());

			//The stored entity and the inventory are straightforward
			//The only other data that we _may_ want to expose would be the time to next drop

			return out;

		}

		@Nonnull
		@Override
		public NestPenTileEntity getExample() {
			NestPenTileEntity te = new NestPenTileEntity();
			Entity chicken = EntityList.createEntityByIDFromName(new ResourceLocation("minecraft:chicken"), WorldDummy.INSTANCE);

			te.trySetEntity(chicken);
			return te;
		}
	};

	public static final IMetaProvider<EggNestTileEntity> META_EGG_NEST = new BaseMetaProvider<EggNestTileEntity>() {

		@Nonnull
		@Override
		public Map<Object, Object> getMeta(@Nonnull IPartialContext<EggNestTileEntity> context) {
			Map<Object, Object> out = new HashMap<>();
			EggNestTileEntity target = context.getTarget();

			//The 'hasEgg' property is exposed as part of the BlockState

			ItemStack egg = target.getEgg();
			if (egg != null) {
				out.put("egg", context.makePartialChild(egg).getMeta());
				out.put("hatchPercent", target.getPercentage());
			}

			//The nest also obtains hatching bonuses from nearby entities, (especially players?),
			// as well as from 'heat lamps' (powered RS lamps above the nest).
			// However, these checks are in private code, and I'd rather not replicate something
			// that may be trivially changed in an update.

			return out;

		}

		@Nonnull
		@Override
		public EggNestTileEntity getExample() {
			EggNestTileEntity te = new EggNestTileEntity();
			ItemStack egg = new ItemStack(Items.EGG);

			te.insertEgg(egg);
			return te;
		}
	};
}
