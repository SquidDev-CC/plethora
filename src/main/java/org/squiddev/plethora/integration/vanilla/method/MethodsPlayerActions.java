package org.squiddev.plethora.integration.vanilla.method;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import dan200.computercraft.api.lua.LuaException;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.apache.commons.lang3.tuple.Pair;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.method.MethodResult;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.api.module.SubtargetedModuleMethod;
import org.squiddev.plethora.api.module.SubtargetedModuleObjectMethod;
import org.squiddev.plethora.gameplay.PlethoraFakePlayer;
import org.squiddev.plethora.gameplay.modules.PlethoraModules;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;

import static org.squiddev.plethora.api.method.ArgumentHelper.optInt;
import static org.squiddev.plethora.api.method.ArgumentHelper.optString;

public final class MethodsPlayerActions {
	@SubtargetedModuleMethod.Inject(
		module = PlethoraModules.KINETIC_S, target = EntityLivingBase.class,
		doc = "function([duration:integer], [hand:string]):boolean, string|nil -- Right click with this item using a particular hand."
	)
	public static MethodResult use(@Nonnull final IUnbakedContext<IModuleContainer> context, @Nonnull Object[] args) throws LuaException {
		final int duration = optInt(args, 0, 0);
		if (duration < 0) throw new LuaException("Duration out of range (must be >= 0)");

		String handStr = optString(args, 1, "main").toLowerCase(Locale.ENGLISH);
		final EnumHand hand;
		if (handStr.equals("main") || handStr.equals("mainhand")) {
			hand = EnumHand.MAIN_HAND;
		} else if (handStr.equals("off") || handStr.equals("offhand")) {
			hand = EnumHand.OFF_HAND;
		} else {
			throw new LuaException("Unknown hand '" + handStr + "', expected 'main' or 'off'");
		}

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
					return use(player, entity, hand, duration);
				} finally {
					if (fakePlayer != null) fakePlayer.unload(entity);
				}
			}
		});
	}

	@SubtargetedModuleObjectMethod.Inject(
		module = PlethoraModules.KINETIC_S, target = EntityLiving.class, worldThread = true,
		doc = "function():boolean, string|nil -- Left click with this item. Returns the action taken."
	)
	public static Object[] swing(EntityLiving entity, IContext<IModuleContainer> context, Object[] args) {
		PlethoraFakePlayer fakePlayer = PlethoraFakePlayer.getPlayer((WorldServer) entity.worldObj, entity);

		fakePlayer.load(entity);
		try {
			RayTraceResult hit = findHit(fakePlayer, entity);

			if (hit != null) {
				switch (hit.typeOfHit) {
					case ENTITY: {
						Pair<Boolean, String> result = fakePlayer.attack(entity, hit.entityHit);
						return new Object[]{result.getLeft(), result.getRight()};
					}
					case BLOCK: {
						Pair<Boolean, String> result = fakePlayer.dig(hit.getBlockPos(), hit.sideHit);
						return new Object[]{result.getLeft(), result.getRight()};
					}
				}
			}

			return new Object[]{false, "Nothing to do here"};
		} finally {
			fakePlayer.unload(entity);
			fakePlayer.resetActiveHand();
		}
	}

	//region Use
	private static MethodResult use(EntityPlayerMP player, EntityLivingBase original, EnumHand hand, int duration) {
		RayTraceResult hit = findHit(player, original);
		ItemStack stack = player.getHeldItemMainhand();
		World world = player.worldObj;

		if (hit != null) {
			switch (hit.typeOfHit) {
				case ENTITY:
					if (stack != null) {
						EnumActionResult result = player.interact(hit.entityHit, stack, EnumHand.MAIN_HAND);
						if (result != EnumActionResult.PASS) {
							return MethodResult.result(result == EnumActionResult.SUCCESS, "entity", "interact");
						}
					}
					break;
				case BLOCK: {
					// When right next to a block the hit direction gets inverted. Try both to see if one works.
					Object[] result = tryUseOnBlock(player, world, hit, stack, hand, hit.sideHit);
					if (result != null) return MethodResult.result(result);

					result = tryUseOnBlock(player, world, hit, stack, hand, hit.sideHit.getOpposite());
					if (result != null) return MethodResult.result(result);
				}
			}
		}

		if (stack != null) {
			if (MinecraftForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickEmpty(player, hand))) {
				return MethodResult.result(true, "item", "use");
			}

			duration = Math.min(duration, stack.getMaxItemUseDuration());
			ActionResult<ItemStack> result = stack.useItemRightClick(player.worldObj, player, hand);

			ItemStack resultStack = result.getResult();
			player.setHeldItem(hand, resultStack);
			if (resultStack == null || resultStack.stackSize <= 0) {
				player.setHeldItem(hand, null);
				MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(player, stack, hand));
			}

			switch (result.getType()) {
				case FAIL:
					return MethodResult.delayedResult(duration, false, "item", "use");
				case SUCCESS:
					ItemStack active = player.getActiveItemStack();
					if (active != null && !ForgeEventFactory.onUseItemStop(player, active, duration)) {
						active.onPlayerStoppedUsing(player.worldObj, player, active.getMaxItemUseDuration() - duration);
						player.resetActiveHand();
						return MethodResult.delayedResult(duration, true, "item", "use");
					}
					break;
			}
		}

		return MethodResult.failure("Nothing to do here");
	}

	private static Object[] tryUseOnBlock(EntityPlayer player, World world, RayTraceResult hit, ItemStack stack, EnumHand hand, EnumFacing side) {
		IBlockState state = world.getBlockState(hit.getBlockPos());
		if (!state.getBlock().isAir(state, world, hit.getBlockPos())) {
			if (MinecraftForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickBlock(player, EnumHand.MAIN_HAND, stack, hit.getBlockPos(), side, hit.hitVec))) {
				return new Object[]{true, "block", "interact"};
			}

			Object[] result = onPlayerRightClick(player, stack, hand, hit.getBlockPos(), side, hit.hitVec);
			if (result != null) return result;
		}

		return null;
	}

	private static Object[] onPlayerRightClick(EntityPlayer player, ItemStack stack, EnumHand hand, BlockPos pos, EnumFacing side, Vec3d look) {
		float xCoord = (float) look.xCoord - (float) pos.getX();
		float yCoord = (float) look.yCoord - (float) pos.getY();
		float zCoord = (float) look.zCoord - (float) pos.getZ();
		World world = player.worldObj;

		if (stack != null && stack.getItem().onItemUseFirst(stack, player, world, pos, side, xCoord, yCoord, zCoord, hand) == EnumActionResult.SUCCESS) {
			return new Object[]{true, "item", "use"};
		}

		if (!player.isSneaking() || stack == null || stack.getItem().doesSneakBypassUse(stack, world, pos, player)) {
			IBlockState state = world.getBlockState(pos);
			if (state.getBlock().onBlockActivated(world, pos, state, player, hand, stack, side, xCoord, yCoord, zCoord)) {
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

		boolean flag = stack.onItemUse(player, world, pos, hand, side, xCoord, yCoord, zCoord) == EnumActionResult.SUCCESS;

		if (player.capabilities.isCreativeMode) {
			stack.setItemDamage(stackMetadata);
			stack.stackSize = stackSize;
		} else if (stack.stackSize <= 0) {
			player.setHeldItem(hand, null);
			MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(player, stack, hand));
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

	private static RayTraceResult findHit(EntityPlayerMP player, EntityLivingBase original) {
		double range = player.interactionManager.getBlockReachDistance();

		Vec3d origin = new Vec3d(
			player.posX,
			player.posY + original.getEyeHeight(),
			player.posZ
		);

		Vec3d look = player.getLookVec();
		Vec3d target = new Vec3d(
			origin.xCoord + look.xCoord * range,
			origin.yCoord + look.yCoord * range,
			origin.zCoord + look.zCoord * range
		);

		RayTraceResult hit = player.worldObj.rayTraceBlocks(origin, target);

		List<Entity> entityList = player.worldObj.getEntitiesInAABBexcluding(
			original,
			player.getEntityBoundingBox().addCoord(
				look.xCoord * range,
				look.yCoord * range,
				look.zCoord * range
			).expand(1, 1, 1), collidablePredicate);

		Entity closestEntity = null;
		Vec3d closestVec = null;
		double closestDistance = range;
		for (Entity entity : entityList) {
			float size = entity.getCollisionBorderSize();
			AxisAlignedBB box = entity.getEntityBoundingBox().expand((double) size, (double) size, (double) size);
			RayTraceResult intercept = box.calculateIntercept(origin, target);

			if (box.isVecInside(origin)) {
				if (closestDistance >= 0.0D) {
					closestEntity = entity;
					closestVec = intercept == null ? origin : intercept.hitVec;
					closestDistance = 0.0D;
				}
			} else if (intercept != null) {
				double distance = origin.distanceTo(intercept.hitVec);

				if (distance < closestDistance || closestDistance == 0.0D) {
					if (entity == player.getRidingEntity() && !player.canRiderInteract()) {
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
			return new RayTraceResult(closestEntity, closestVec);
		} else {
			return hit;
		}
	}
}
