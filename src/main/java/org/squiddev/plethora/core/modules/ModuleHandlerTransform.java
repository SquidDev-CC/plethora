package org.squiddev.plethora.core.modules;

import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;
import org.squiddev.plethora.api.method.IContextBuilder;
import org.squiddev.plethora.api.module.IModuleAccess;
import org.squiddev.plethora.api.module.IModuleHandler;

import javax.annotation.Nonnull;
import javax.vecmath.Matrix4f;

/**
 * A module handler which proxies another module handler, applying a custom transformation.
 */
public class ModuleHandlerTransform implements IModuleHandler {
	private final IModuleHandler proxy;
	private final Matrix4f transform;

	public ModuleHandlerTransform(IModuleHandler proxy, Matrix4f transform) {
		this.proxy = proxy;
		this.transform = transform;
	}

	@Nonnull
	@Override
	public ResourceLocation getModule() {
		return proxy.getModule();
	}

	@Override
	public void getAdditionalContext(@Nonnull IModuleAccess access, @Nonnull IContextBuilder builder) {
		proxy.getAdditionalContext(access, builder);
	}

	@Nonnull
	@Override
	public Pair<IBakedModel, Matrix4f> getModel(float delta) {
		return Pair.of(proxy.getModel(delta).getLeft(), transform);
	}
}
