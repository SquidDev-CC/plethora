package org.squiddev.plethora.core.modules;

import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;
import org.squiddev.plethora.api.module.BasicModuleHandler;

import javax.annotation.Nonnull;
import javax.vecmath.Matrix4f;

/**
 * A version of {@link BasicModuleHandler} which allows specifying a custom transformation matrix.
 */
public class BasicModuleHandlerTransform extends BasicModuleHandler {
	private final Matrix4f transform;

	public BasicModuleHandlerTransform(ResourceLocation id, Item item, Matrix4f transform) {
		super(id, item);
		this.transform = transform;
	}

	@Nonnull
	@Override
	public Pair<IBakedModel, Matrix4f> getModel(float delta) {
		return Pair.of(super.getModel(delta).getLeft(), transform);
	}
}
