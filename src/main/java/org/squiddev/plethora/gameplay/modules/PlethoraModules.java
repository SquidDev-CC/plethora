package org.squiddev.plethora.gameplay.modules;

import net.minecraft.util.ResourceLocation;
import org.squiddev.plethora.gameplay.Plethora;

/**
 * List of modules built in to plethora
 */
public class PlethoraModules {
	public static final ResourceLocation INTROSPECTION = new ResourceLocation(Plethora.RESOURCE_DOMAIN, ItemModule.INTROSPECTION);
	public static final ResourceLocation KINETIC = new ResourceLocation(Plethora.RESOURCE_DOMAIN, ItemModule.KINETIC);
	public static final ResourceLocation LASER = new ResourceLocation(Plethora.RESOURCE_DOMAIN, ItemModule.LASER);
	public static final ResourceLocation SCANNER = new ResourceLocation(Plethora.RESOURCE_DOMAIN, ItemModule.SCANNER);
	public static final ResourceLocation SENSOR = new ResourceLocation(Plethora.RESOURCE_DOMAIN, ItemModule.SENSOR);

	public static final String INTROSPECTION_S = Plethora.RESOURCE_DOMAIN + ":" + ItemModule.INTROSPECTION;
	public static final String KINETIC_S = Plethora.RESOURCE_DOMAIN + ":" + ItemModule.KINETIC;
	public static final String LASER_S = Plethora.RESOURCE_DOMAIN + ":" + ItemModule.LASER;
	public static final String SCANNER_S = Plethora.RESOURCE_DOMAIN + ":" + ItemModule.SCANNER;
	public static final String SENSOR_S = Plethora.RESOURCE_DOMAIN + ":" + ItemModule.SENSOR;
}
