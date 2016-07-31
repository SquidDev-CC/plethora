package org.squiddev.plethora.core.capabilities;

import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import org.squiddev.plethora.api.module.IModule;
import org.squiddev.plethora.api.module.IModuleHandler;
import org.squiddev.plethora.api.reference.IReference;
import org.squiddev.plethora.gameplay.Plethora;

import javax.annotation.Nonnull;
import javax.vecmath.Matrix4f;
import java.util.Collection;
import java.util.Collections;

import static org.squiddev.plethora.utils.Helpers.getMesher;

/**
 * The default module handler: named "plethora:default_module". Uses a missing model.
 *
 * This is just a stub for the capability.
 */
public final class DefaultModuleHandler implements IModuleHandler, IModule {
	private static final Matrix4f identity = new Matrix4f();
	private static final ResourceLocation name = new ResourceLocation(Plethora.ID, "default_module");

	static {
		identity.setIdentity();
	}

	@Nonnull
	@Override
	public IModule getModule() {
		return this;
	}

	@Nonnull
	@Override
	public Collection<IReference<?>> getAdditionalContext() {
		return Collections.emptyList();
	}

	@Nonnull
	@Override
	@SideOnly(Side.CLIENT)
	public Pair<IBakedModel, Matrix4f> getModel(float delta) {
		return Pair.of(getMesher().getModelManager().getMissingModel(), new Matrix4f());
	}

	@Nonnull
	@Override
	public ResourceLocation getModuleId() {
		return name;
	}
}
