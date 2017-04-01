package org.squiddev.plethora.core.modules;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import org.squiddev.plethora.api.PlethoraAPI;
import org.squiddev.plethora.api.minecart.IMinecartUpgradeHandler;
import org.squiddev.plethora.api.minecart.MinecartModuleHandler;

import javax.vecmath.Matrix4f;

/**
 * A version of {@link MinecartModuleHandler} which allows specifying a minecart specific matrix transformation.
 */
public class MinecartModuleHandlerTransform extends MinecartModuleHandler {
	private final Matrix4f transform;

	public MinecartModuleHandlerTransform(ResourceLocation id, Item item, Matrix4f transform) {
		super(id, item);
		this.transform = transform;
	}

	@Override
	protected IMinecartUpgradeHandler createMinecart() {
		return PlethoraAPI.instance().moduleRegistry().toMinecartUpgrade(new ModuleHandlerTransform(this, transform));
	}
}
