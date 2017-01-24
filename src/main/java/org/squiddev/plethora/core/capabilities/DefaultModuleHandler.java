package org.squiddev.plethora.core.capabilities;

import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import org.squiddev.plethora.api.module.AbstractModuleHandler;
import org.squiddev.plethora.gameplay.Plethora;

import javax.annotation.Nonnull;
import javax.vecmath.Matrix4f;

import static org.squiddev.plethora.utils.Helpers.getMesher;

/**
 * The default module handler: named "plethora:default_module". Uses a missing model.
 *
 * This is just a stub for the capability.
 */
public final class DefaultModuleHandler extends AbstractModuleHandler {
	private static final Matrix4f identity = new Matrix4f();
	private static final ResourceLocation name = new ResourceLocation(Plethora.ID, "default_module");

	static {
		identity.setIdentity();
	}

	@Nonnull
	@Override
	public ResourceLocation getModule() {
		return name;
	}

	@Nonnull
	@Override
	@SideOnly(Side.CLIENT)
	public Pair<IBakedModel, Matrix4f> getModel(float delta) {
		return Pair.of(getMesher().getModelManager().getMissingModel(), identity);
	}
}
