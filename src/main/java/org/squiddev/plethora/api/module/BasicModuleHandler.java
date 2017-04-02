package org.squiddev.plethora.api.module;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import org.squiddev.plethora.api.Constants;
import org.squiddev.plethora.gameplay.client.RenderHelpers;

import javax.annotation.Nonnull;
import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix4f;

/**
 * A basic module handler which serves as a capability provider too.
 * Used for {@link net.minecraftforge.event.AttachCapabilitiesEvent}.
 */
public class BasicModuleHandler extends AbstractModuleHandler implements ICapabilityProvider {
	private final ResourceLocation id;
	private final Item item;

	@SideOnly(Side.CLIENT)
	private IBakedModel model;

	public BasicModuleHandler(ResourceLocation id, Item item) {
		this.id = id;
		this.item = item;
	}

	@Nonnull
	@Override
	public ResourceLocation getModule() {
		return id;
	}

	@Nonnull
	@Override
	@SideOnly(Side.CLIENT)
	public Pair<IBakedModel, Matrix4f> getModel(float delta) {
		Matrix4f matrix = new Matrix4f();
		matrix.setIdentity();
		matrix.setRotation(new AxisAngle4f(0f, 1f, 0f, delta));

		IBakedModel model = this.model;
		if (model == null) {
			model = this.model = RenderHelpers.getMesher().getItemModel(new ItemStack(item));
		}

		return Pair.of(model, matrix);
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
