package org.squiddev.plethora.integration.vanilla.method;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import dan200.computercraft.api.lua.LuaException;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.method.MethodResult;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.api.module.TargetedModuleMethod;
import org.squiddev.plethora.gameplay.PlethoraFakePlayer;
import org.squiddev.plethora.gameplay.modules.PlethoraModules;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.Callable;

import static org.squiddev.plethora.api.method.ArgumentHelper.optInt;

public final class MethodsPlayerActions {
	@TargetedModuleMethod.Inject(
		module = PlethoraModules.KINETIC_S,
		target = EntityLivingBase.class,
		doc = "function([duration: integer]):boolean, string|nil -- Right click with this item"
	)
	public static MethodResult use(@Nonnull final IUnbakedContext<IModuleContainer> context, @Nonnull Object[] args) throws LuaException {
		final int duration = optInt(args, 0, 0);

		if (duration < 0) throw new LuaException("Duration out of range (must be >= 0)");

		return MethodResult.nextTick(new Callable<MethodResult>() {
			@Override
			@Nonnull
			public MethodResult call() throws Exception {
				EntityLivingBase entity = context.bake().getContext(EntityLivingBase.class);

				EntityPlayerMP player;
				PlethoraFakePlayer fakePlayer;
				if (entity instanceof EntityPlayerMP) {
					player = (EntityPlayerMP) entity;
					fakePlayer = null;
				} else if (entity instanceof EntityPlayer) {
					throw new LuaException("An unexpected player was used");
				} else {
					player = fakePlayer = PlethoraFakePlayer.getPlayer((WorldServer) entity.worldObj, entity);
				}

				if (fakePlayer != null) fakePlayer.load(entity);

				try {
					return use(player, entity, duration);
				} finally {
					if (fakePlayer != null) fakePlayer.unload(entity);
				}
			}
		});
	}

	//region Use
	private static MethodResult use(EntityPlayerMP player, EntityLivingBase original, int duration) {
		MovingObjectPosition hit = findHit(player, original);
		ItemStack stack = player.getCurrentEquippedItem();
		World world = player.worldObj;

		if (hit != null) {
			switch (hit.typeOfHit) {
				case ENTITY:
					if (stack != null && player.interactWith(hit.entityHit)) {
						return MethodResult.result(true, "entity", "interact");
					}
					break;
				case BLOCK: {
					// When right next to a block the hit direction gets inverted. Try both to see if one works.
					Object[] result = tryUseOnBlock(player, world, hit, stack, hit.sideHit);
					if (result != null) return MethodResult.result(result);

					result = tryUseOnBlock(player, world, hit, stack, hit.sideHit.getOpposite());
					if (result != null) return MethodResult.result(result);
				}
			}
		}

		if (stack != null && !ForgeEventFactory.onPlayerInteract(player, PlayerInteractEvent.Action.RIGHT_CLICK_AIR, world, null, null, null).isCanceled()) {
			duration = Math.min(duration, stack.getMaxItemUseDuration());
			ItemStack old = stack.copy();
			ItemStack result = stack.useItemRightClick(player.worldObj, player);

			boolean using = player.isUsingItem();
			if (using && !ForgeEventFactory.onUseItemStop(player, player.itemInUse, duration)) {
				player.itemInUse.onPlayerStoppedUsing(player.worldObj, player, player.itemInUse.getMaxItemUseDuration() - duration);
				player.clearItemInUse();
			}

			if (using || !ItemStack.areItemStacksEqual(old, result)) {
				if (result == null || result.stackSize <= 0) {
					player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
					MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(player, stack));
				} else {
					player.inventory.setInventorySlotContents(player.inventory.currentItem, result);
				}
				return MethodResult.delayedResult(duration, true, "item", "use");
			} else {
				return MethodResult.delayedResult(duration, false);
			}
		}

		return MethodResult.failure("Nothing to do here");
	}

	private static Object[] tryUseOnBlock(EntityPlayer player, World world, MovingObjectPosition hit, ItemStack stack, EnumFacing side) {
		if (!world.getBlockState(hit.getBlockPos()).getBlock().isAir(world, hit.getBlockPos())) {
			if (ForgeEventFactory.onPlayerInteract(player, PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK, world, hit.getBlockPos(), side, hit.hitVec).isCanceled()) {
				return new Object[]{true, "block", "interact"};
			}

			Object[] result = onPlayerRightClick(player, stack, hit.getBlockPos(), side, hit.hitVec);
			if (result != null) return result;
		}

		return null;
	}

	private static Object[] onPlayerRightClick(EntityPlayer player, ItemStack stack, BlockPos pos, EnumFacing side, Vec3 look) {
		float xCoord = (float) look.xCoord - (float) pos.getX();
		float yCoord = (float) look.yCoord - (float) pos.getY();
		float zCoord = (float) look.zCoord - (float) pos.getZ();
		World world = player.worldObj;

		if (stack != null && stack.getItem() != null && stack.getItem().onItemUseFirst(stack, player, world, pos, side, xCoord, yCoord, zCoord)) {
			return new Object[]{true, "item", "use"};
		}

		if (!player.isSneaking() || stack == null || stack.getItem().doesSneakBypassUse(world, pos, player)) {
			IBlockState state = world.getBlockState(pos);
			if (state.getBlock().onBlockActivated(world, pos, state, player, side, xCoord, yCoord, zCoord)) {
				return new Object[]{true, "block", "interact"};
			}
		}

		if (stack == null) return null;

		if (stack.getItem() instanceof ItemBlock) {
			ItemBlock itemBlock = (ItemBlock) stack.getItem();
			if (!itemBlock.canPlaceBlockOnSide(world, pos, side, player, stack)) {
				return null;
			}
		}

		int stackMetadata = stack.getMetadata();
		int stackSize = stack.stackSize;

		boolean flag = stack.onItemUse(player, world, pos, side, xCoord, yCoord, zCoord);

		if (player.capabilities.isCreativeMode) {
			stack.setItemDamage(stackMetadata);
			stack.stackSize = stackSize;
		} else if (stack.stackSize <= 0) {
			player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
			MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(player, stack));
		}

		if (flag) {

			return new Object[]{true, "place"};
		}

		return null;
	}

	private static final Predicate<Entity> collidablePredicate = Predicates.and(
		EntitySelectors.NOT_SPECTATING,
		new Predicate<Entity>() {
			public boolean apply(Entity entity) {
				return entity.canBeCollidedWith();
			}
		}
	);
	//endregion

	private static MovingObjectPosition findHit(EntityPlayerMP player, EntityLivingBase original) {
		double range = player.theItemInWorldManager.getBlockReachDistance();

		Vec3 origin = new Vec3(
			player.posX,
			player.posY + player.getEyeHeight(),
			player.posZ
		);

		Vec3 look = player.getLookVec();
		Vec3 target = new Vec3(
			origin.xCoord + look.xCoord * range,
			origin.yCoord + look.yCoord * range,
			origin.zCoord + look.zCoord * range
		);

		MovingObjectPosition hit = player.worldObj.rayTraceBlocks(origin, target);

		List<Entity> entityList = player.worldObj.getEntitiesInAABBexcluding(
			original,
			player.getEntityBoundingBox().addCoord(
				look.xCoord * range,
				look.yCoord * range,
				look.zCoord * range
			).expand(1, 1, 1), collidablePredicate);

		Entity closestEntity = null;
		Vec3 closestVec = null;
		double closestDistance = range;
		for (Entity entity : entityList) {
			float size = entity.getCollisionBorderSize();
			AxisAlignedBB box = entity.getEntityBoundingBox().expand((double) size, (double) size, (double) size);
			MovingObjectPosition intercept = box.calculateIntercept(origin, target);

			if (box.isVecInside(origin)) {
				if (closestDistance >= 0.0D) {
					closestEntity = entity;
					closestVec = intercept == null ? origin : intercept.hitVec;
					closestDistance = 0.0D;
				}
			} else if (intercept != null) {
				double distance = origin.distanceTo(intercept.hitVec);

				if (distance < closestDistance || closestDistance == 0.0D) {
					if (entity == player.ridingEntity && !player.canRiderInteract()) {
						if (closestDistance == 0.0D) {
							closestEntity = entity;
							closestVec = intercept.hitVec;
						}
					} else {
						closestEntity = entity;
						closestVec = intercept.hitVec;
						closestDistance = distance;
					}
				}
			}
		}

		if (closestEntity instanceof EntityLivingBase && closestDistance <= range && (hit == null || player.getDistanceSq(hit.getBlockPos()) > closestDistance * closestDistance)) {
			return new MovingObjectPosition(closestEntity, closestVec);
		} else {
			return hit;
		}
	}
}
