package org.squiddev.plethora.core;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.turtle.*;
import dan200.computercraft.shared.turtle.blocks.ITurtleTile;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import org.apache.commons.lang3.tuple.Pair;
import org.squiddev.plethora.api.TurtleWorldLocation;
import org.squiddev.plethora.api.method.ICostHandler;
import org.squiddev.plethora.api.method.IMethod;
import org.squiddev.plethora.api.method.IPartialContext;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.api.module.IModuleHandler;
import org.squiddev.plethora.api.module.SingletonModuleContainer;
import org.squiddev.plethora.api.reference.IReference;
import org.squiddev.plethora.api.reference.Reference;
import org.squiddev.plethora.core.executor.DelayedExecutor;
import org.squiddev.plethora.core.executor.IExecutorFactory;
import org.squiddev.plethora.utils.DebugLogger;

import javax.annotation.Nonnull;
import javax.vecmath.Matrix4f;
import java.util.Collection;
import java.util.List;

/**
 * Wraps a module item as a turtle upgrade.
 */
class TurtleUpgradeModule implements ITurtleUpgrade {
	private final IModuleHandler handler;
	private final ItemStack stack;
	private final String adjective;

	TurtleUpgradeModule(@Nonnull ItemStack stack, @Nonnull IModuleHandler handler, @Nonnull String adjective) {
		this.handler = handler;
		this.stack = stack;
		this.adjective = adjective;
	}

	@Override
	public ResourceLocation getUpgradeID() {
		return handler.getModule();
	}

	@Override
	public int getLegacyUpgradeID() {
		return -1;
	}

	@Override
	public String getUnlocalisedAdjective() {
		return adjective;
	}

	@Override
	public TurtleUpgradeType getType() {
		return TurtleUpgradeType.Peripheral;
	}

	@Override
	public ItemStack getCraftingItem() {
		return stack;
	}

	@Override
	public IPeripheral createPeripheral(final ITurtleAccess turtle, final TurtleSide side) {
		final ResourceLocation thisModule = handler.getModule();

		String moduleName = thisModule.toString();
		if (ConfigCore.Blacklist.blacklistModulesTurtle.contains(moduleName) || ConfigCore.Blacklist.blacklistModules.contains(moduleName)) {
			return null;
		}

		final IModuleContainer container = new SingletonModuleContainer(thisModule);
		IReference<IModuleContainer> containerRef = new IReference<IModuleContainer>() {
			@Nonnull
			@Override
			public IModuleContainer get() throws LuaException {
				if (turtle.getUpgrade(side) != TurtleUpgradeModule.this) throw new LuaException("The upgrade is gone");
				return container;
			}
		};

		MethodRegistry registry = MethodRegistry.instance;

		TileEntity te = turtle.getWorld().getTileEntity(turtle.getPosition());
		ICostHandler cost;
		if (te != null && te instanceof ITurtleTile) {
			cost = registry.getCostHandler(te, null);
		} else {
			DebugLogger.warn("Cannot find turtle where access says it should be");
			return null;
		}

		Collection<IReference<?>> additionalContext = handler.getAdditionalContext();
		IReference<?>[] contextData = new IReference[additionalContext.size() + 2];
		additionalContext.toArray(contextData);
		contextData[contextData.length - 2] = new TurtleWorldLocation(turtle);
		contextData[contextData.length - 1] = Reference.id(turtle);

		IUnbakedContext<IModuleContainer> context = registry.makeContext(
			containerRef,
			cost,
			containerRef,
			contextData
		);

		IPartialContext<IModuleContainer> baked = new PartialContext<IModuleContainer>(
			container,
			cost,
			new Object[]{new TurtleWorldLocation(turtle), turtle},
			container
		);

		Tuple<List<IMethod<?>>, List<IUnbakedContext<?>>> paired = registry.getMethodsPaired(context, baked);
		if (paired.getFirst().size() > 0) {
			return new MethodWrapperPeripheral(moduleName, this, paired.getFirst(), paired.getSecond(), new DelayedExecutor());
		} else {
			return null;
		}
	}

	@Override
	public TurtleCommandResult useTool(ITurtleAccess turtle, TurtleSide side, TurtleVerb verb, EnumFacing direction) {
		return null;
	}

	@Override
	public Pair<IBakedModel, Matrix4f> getModel(ITurtleAccess turtle, TurtleSide side) {
		float xOffset = side == TurtleSide.Left ? -0.40625f : 0.40625f;
		Matrix4f transform = new Matrix4f(
			0.0F, 0.0F, -0.8F, 0.9f + xOffset,
			0.0F, 0.8F, 0.0F, 0.1F,
			0.8F, 0.0F, 0.0F, 0.075F,
			0.0F, 0.0F, 0.0F, 1.0F
		);

		// Translate -0.5 0 -0.5
		// Rotate -PI/2
		// Scale 0.8
		// Translate 0.5 0 0.5
		// Translate xOffset 0.1 -0.025

		Pair<IBakedModel, Matrix4f> pair = handler.getModel(0);
		transform.mul(transform, pair.getRight());
		return Pair.of(pair.getLeft(), transform);
	}

	@Override
	public void update(ITurtleAccess turtle, TurtleSide side) {
		IPeripheral peripheral = turtle.getPeripheral(side);
		if (peripheral instanceof MethodWrapperPeripheral) {
			IExecutorFactory executor = ((MethodWrapperPeripheral) peripheral).getExecutorFactory();

			if (executor instanceof DelayedExecutor) {
				((DelayedExecutor) executor).update();
			}
		}
	}
}
