package org.squiddev.plethora.gameplay.modules;

import com.google.common.base.Ascii;
import net.minecraft.util.ResourceLocation;
import org.squiddev.plethora.gameplay.Plethora;

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
		// Normalise module name (remove module, lowercase first letter)
		name = name.replace("module", "");
		if (name.length() > 0) {
			name = Ascii.toLowerCase(name.charAt(0)) + name.substring(1);
		}

		return new ResourceLocation(Plethora.RESOURCE_DOMAIN, name);
	}
}
