package org.squiddev.plethora.modules;

import net.minecraft.util.ResourceLocation;
import org.squiddev.plethora.Plethora;

/**
 * List of modules built in to plethora
 */
public class PlethoraModules {
	public static final ResourceLocation INTROSPECTION = toResource(ItemModule.INTROSPECTION);
	public static final ResourceLocation KINETIC = toResource(ItemModule.KINETIC);
	public static final ResourceLocation LASER = toResource(ItemModule.LASER);
	public static final ResourceLocation SCANNER = toResource(ItemModule.SCANNER);
	public static final ResourceLocation SENSOR = toResource(ItemModule.SENSOR);

	public static ResourceLocation toResource(String name) {
		return new ResourceLocation(Plethora.RESOURCE_DOMAIN, name);
	}
}
