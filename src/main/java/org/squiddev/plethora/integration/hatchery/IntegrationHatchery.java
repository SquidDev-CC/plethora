package org.squiddev.plethora.integration.hatchery;

import com.gendeathrow.hatchery.Hatchery;
import com.gendeathrow.hatchery.block.nest.EggNestTileEntity;
import com.gendeathrow.hatchery.block.nestpen.NestPenTileEntity;
import com.gendeathrow.hatchery.core.init.ModBlocks;
import com.gendeathrow.hatchery.core.init.ModItems;
import com.gendeathrow.hatchery.entities.EntityRooster;
import com.gendeathrow.hatchery.item.AnimalNet;
import com.gendeathrow.hatchery.item.HatcheryEgg;
import com.gendeathrow.hatchery.util.ItemStackEntityNBTHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.BaseMetaProvider;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.api.method.IPartialContext;
import org.squiddev.plethora.integration.ItemEntityStorageMetaProvider;
import org.squiddev.plethora.utils.EntityPlayerDummy;
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

	public static final IMetaProvider<ItemStack> META_ANIMAL_NET = new ItemEntityStorageMetaProvider<AnimalNet>(
		"capturedEntity", AnimalNet.class,
		"Provides the entity captured inside this Animal Net."
	) {

		@Nullable
		@Override
		protected Entity spawn(@Nonnull ItemStack stack, @Nonnull AnimalNet item, @Nonnull IWorldLocation location) {
			//Check for a captured entity
			if (!AnimalNet.hasCapturedAnimal(stack)) return null;

			//Check for an entity ID
			NBTTagCompound entityData = AnimalNet.getCapturedAnimalNBT(stack);
			if (!entityData.hasKey("id", Constants.NBT.TAG_STRING)) return null;

			return EntityList.createEntityFromNBT(entityData, location.getWorld());
		}

		@Nonnull
		@Override
		protected Map<String, ?> getBasicDetails(@Nonnull ItemStack stack, @Nonnull AnimalNet item) {
			//Check for a captured entity
			if (!AnimalNet.hasCapturedAnimal(stack)) return Collections.emptyMap();
			return getBasicDetails(AnimalNet.getCapturedAnimalNBT(stack));
		}

		@Nullable
		@Override
		public ItemStack getExample() {
			return AnimalNet.addEntitytoNet(new EntityPlayerDummy(WorldDummy.INSTANCE), new ItemStack(ModItems.animalNet), new EntityCow(WorldDummy.INSTANCE));
		}
	};

	//Because of COURSE it stores a raw entity...
	public static final IMetaProvider<ItemStack> META_HATCHERY_EGG = new ItemEntityStorageMetaProvider<HatcheryEgg>(
		"storedEntity", HatcheryEgg.class,
		"Provides the entity that may spawn from this Egg"
	) {
		@Nullable
		@Override
		protected Entity spawn(@Nonnull ItemStack stack, @Nonnull HatcheryEgg item, @Nonnull IWorldLocation location) {
			//Check for a captured entity
			NBTTagCompound entityData = ItemStackEntityNBTHelper.getEntityTagFromStack(stack);
			if (entityData == null || !entityData.hasKey("id", Constants.NBT.TAG_STRING)) return null;

			//Due to Hatchery's `ItemStackEntityNBTHelper` having a `saveAll` param, we may need to
			// handle a generic egg.  Unfortunately, they don't provide a convenient analogue to TE's `ItemMorb.GENERIC`,
			// so this will have to suffice...
			Entity entity = EntityList.createEntityFromNBT(entityData, location.getWorld());
			if (entity == null) {
				entity = EntityList.createEntityByIDFromName(new ResourceLocation(entityData.getString("id")), location.getWorld());
			}
			return entity;
		}

		@Nonnull
		@Override
		protected Map<String, ?> getBasicDetails(@Nonnull ItemStack stack, @Nonnull HatcheryEgg item) {
			return getBasicDetails(ItemStackEntityNBTHelper.getEntityTagFromStack(stack));
		}

		@Nonnull
		@Override
		public ItemStack getExample() {
			return HatcheryEgg.createEggFromEntity(WorldDummy.INSTANCE, new EntityChicken(WorldDummy.INSTANCE));
		}
	};

	public static final IMetaProvider<EntityRooster> META_ROOSTER = new BasicMetaProvider<EntityRooster>() {

		@Nonnull
		@Override
		public Map<String, ?> getMeta(@Nonnull EntityRooster target) {
			//We are not exposing the actual inventory at this time;
			//if `seeds` drops below 98, then there are no items left in the inventory.

			//Somewhat of a misnomer; this is the 'energy' level, comparable to a furnace's remaining burn ticks
			//The actual 'temptation items' (Hatchery's term, not mine) is exposed via the Item Handler Capability,
			return Collections.singletonMap("seeds", target.getSeeds());
		}

		@Nonnull
		@Override
		public EntityRooster getExample() {
			return new EntityRooster(WorldDummy.INSTANCE);
		}
	};

	public static final IMetaProvider<NestPenTileEntity> META_NEST_PEN = new BaseMetaProvider<NestPenTileEntity>() {

		@Nonnull
		@Override
		public Map<String, ?> getMeta(@Nonnull IPartialContext<NestPenTileEntity> context) {
			NestPenTileEntity target = context.getTarget();

			//The stored entity and the inventory are straightforward
			//The only other data that we _may_ want to expose would be the time to next drop

			//Get the stored entity
			EntityAgeable storedEntity = target.storedEntity();
			return storedEntity != null
				? Collections.singletonMap("storedEntity", context.makePartialChild(storedEntity).getMeta())
				: Collections.emptyMap();
		}

		@Nullable
		@Override
		public NestPenTileEntity getExample() {
			//This mess is unfortunately necessary, as `trySetEntity` updates the block's state
			//If we don't first set the BlockState, we get an NPE.
			WorldDummy.INSTANCE.setBlockState(BlockPos.ORIGIN, ModBlocks.pen.getDefaultState());
			TileEntity te = WorldDummy.INSTANCE.getTileEntity(BlockPos.ORIGIN);
			if (!(te instanceof NestPenTileEntity)) return null;

			NestPenTileEntity pen = (NestPenTileEntity) te;
			pen.trySetEntity(new EntityChicken(WorldDummy.INSTANCE));
			return pen;
		}
	};

	public static final IMetaProvider<EggNestTileEntity> META_EGG_NEST = new BaseMetaProvider<EggNestTileEntity>() {

		@Nonnull
		@Override
		public Map<String, ?> getMeta(@Nonnull IPartialContext<EggNestTileEntity> context) {
			Map<String, Object> out = new HashMap<>(2);
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
			te.insertEgg(new ItemStack(Items.EGG));
			return te;
		}
	};
}
