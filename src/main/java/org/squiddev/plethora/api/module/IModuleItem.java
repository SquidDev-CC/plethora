package org.squiddev.plethora.api.module;

import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import org.squiddev.plethora.api.reference.IReference;

import javax.annotation.Nonnull;
import javax.vecmath.Matrix4f;
import java.util.Collection;

/**
 * An item which holds a module
 */
public interface IModuleItem {
	/**
	 * Get the module from this item
	 *
	 * @param stack The stack to extract from
	 * @return The module.
	 */
	@Nonnull
	IModule getModule(@Nonnull ItemStack stack);

	/**
	 * Used to get additional context from a stack
	 *
	 * @param stack The stack to extract from
	 * @return The additional context items.
	 */
	@Nonnull
	Collection<IReference<?>> getAdditionalContext(@Nonnull ItemStack stack);

	/**
	 * Get a model from this stack
	 *
	 * @param stack The stack to get a model from
	 * @param delta A tick based offset. Generally used to rotate the model.
	 * @return A baked model and its transformation
	 * @see net.minecraft.client.renderer.ItemModelMesher#getItemModel(ItemStack)
	 */
	@SideOnly(Side.CLIENT)
	@Nonnull
	Pair<IBakedModel, Matrix4f> getModel(@Nonnull ItemStack stack, float delta);
}
