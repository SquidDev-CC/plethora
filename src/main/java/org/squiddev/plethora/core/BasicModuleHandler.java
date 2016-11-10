package org.squiddev.plethora.core;

import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.apache.commons.lang3.tuple.Pair;
import org.squiddev.plethora.api.Constants;
import org.squiddev.plethora.api.module.IModuleHandler;
import org.squiddev.plethora.api.reference.IReference;
import org.squiddev.plethora.utils.Helpers;

import javax.annotation.Nonnull;
import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix4f;
import java.util.Collection;
import java.util.Collections;

/**
 * A basic module handler which serves as a capability provider too.
 * Used for {@link net.minecraftforge.event.AttachCapabilitiesEvent}.
 */
public class BasicModuleHandler implements IModuleHandler, ICapabilityProvider {
	private final ItemStack stack;
	private final ResourceLocation id;

	public BasicModuleHandler(String id, ItemStack stack) {
		this.stack = stack;
		this.id = new ResourceLocation(id);
	}

	@Nonnull
	@Override
	public ResourceLocation getModule() {
		return id;
	}

	@Nonnull
	@Override
	public Collection<IReference<?>> getAdditionalContext() {
		return Collections.emptySet();
	}

	@Nonnull
	@Override
	public Pair<IBakedModel, Matrix4f> getModel(float delta) {
		Matrix4f matrix = new Matrix4f(1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1);
		matrix.setRotation(new AxisAngle4f(0f, 1f, 0f, delta));

		return Pair.of(
			Helpers.getMesher().getItemModel(stack),
			matrix
		);
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing enumFacing) {
		return capability == Constants.MODULE_HANDLER_CAPABILITY;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getCapability(Capability<T> capability, EnumFacing enumFacing) {
		return capability == Constants.MODULE_HANDLER_CAPABILITY ? (T) this : null;
	}
}
