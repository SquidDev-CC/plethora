package org.squiddev.plethora.api.module;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import org.squiddev.plethora.api.reference.IReference;

import javax.annotation.Nonnull;
import javax.vecmath.Matrix4f;
import java.util.Collection;

/**
 * A capability which provides a module
 */
public interface IModuleHandler {
	/**
	 * Get the module from this item
	 *
	 * @return The module.
	 */
	@Nonnull
	ResourceLocation getModule();

	/**
	 * Used to get additional context from a stack
	 *
	 * @return The additional context items.
	 */
	@Nonnull
	Collection<IReference<?>> getAdditionalContext();

	/**
	 * Get a model from this stack
	 *
	 * @param delta A tick based offset. Can used to animate the model.
	 * @return A baked model and its transformation
	 * @see net.minecraft.client.renderer.ItemModelMesher#getItemModel(ItemStack)
	 */
	@Nonnull
	@SideOnly(Side.CLIENT)
	Pair<IBakedModel, Matrix4f> getModel(float delta);
}
