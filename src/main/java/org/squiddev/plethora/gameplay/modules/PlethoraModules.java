package org.squiddev.plethora.gameplay.modules;

import net.minecraft.util.ResourceLocation;
import org.squiddev.plethora.gameplay.Plethora;

/**
 * List of modules built in to plethora
 */
public class PlethoraModules {
	public static final String INTROSPECTION = "introspection";
	public static final String LASER = "laser";
	public static final String SCANNER = "scanner";
	public static final String SENSOR = "sensor";
	public static final String KINETIC = "kinetic";
	public static final String CHAT = "chat";
	public static final String GLASSES = "glasses";
	public static final String CHAT_CREATIVE = "chat_creative";

	public static final int INTROSPECTION_ID = 0;
	public static final int LASER_ID = 1;
	public static final int SCANNER_ID = 2;
	public static final int SENSOR_ID = 3;
	public static final int KINETIC_ID = 4;
	public static final int CHAT_ID = 5;
	public static final int GLASSES_ID = 6;
	public static final int CHAT_CREATIVE_ID = 7;

	public static final int MODULES = 8;

	private static final String[] NAMES = new String[]{
		INTROSPECTION, LASER, SCANNER, SENSOR, KINETIC, CHAT, GLASSES, CHAT_CREATIVE
	};

	public static final int[] TURTLE_MODULES = new int[]{
		INTROSPECTION_ID,
		LASER_ID,
		SCANNER_ID,
		SENSOR_ID,
		CHAT_CREATIVE_ID,
	};

	public static final int[] POCKET_MODULES = new int[]{
		LASER_ID,
		SCANNER_ID,
		SENSOR_ID,
		INTROSPECTION_ID,
		KINETIC_ID,
		CHAT_ID,
		CHAT_CREATIVE_ID,
	};

	public static final int[] VEHICLE_MODULES = new int[]{
		LASER_ID,
		SCANNER_ID,
		SENSOR_ID,
		INTROSPECTION_ID,
		KINETIC_ID,
		CHAT_CREATIVE_ID,
	};

	public static final String INTROSPECTION_S = Plethora.RESOURCE_DOMAIN + ":" + INTROSPECTION;
	public static final String KINETIC_S = Plethora.RESOURCE_DOMAIN + ":" + KINETIC;
	public static final String LASER_S = Plethora.RESOURCE_DOMAIN + ":" + LASER;
	public static final String SCANNER_S = Plethora.RESOURCE_DOMAIN + ":" + SCANNER;
	public static final String SENSOR_S = Plethora.RESOURCE_DOMAIN + ":" + SENSOR;
	public static final String CHAT_S = Plethora.RESOURCE_DOMAIN + ":" + CHAT;
	public static final String GLASSES_S = Plethora.RESOURCE_DOMAIN + ":" + GLASSES;
	public static final String CHAT_CREATIVE_S = Plethora.RESOURCE_DOMAIN + ":" + CHAT_CREATIVE;

	public static final ResourceLocation INTROSPECTION_M = new ResourceLocation(Plethora.RESOURCE_DOMAIN, INTROSPECTION);
	public static final ResourceLocation KINETIC_M = new ResourceLocation(Plethora.RESOURCE_DOMAIN, KINETIC);
	public static final ResourceLocation LASER_M = new ResourceLocation(Plethora.RESOURCE_DOMAIN, LASER);
	public static final ResourceLocation SCANNER_M = new ResourceLocation(Plethora.RESOURCE_DOMAIN, SCANNER);
	public static final ResourceLocation SENSOR_M = new ResourceLocation(Plethora.RESOURCE_DOMAIN, SENSOR);
	public static final ResourceLocation CHAT_M = new ResourceLocation(Plethora.RESOURCE_DOMAIN, CHAT);
	public static final ResourceLocation GLASSES_M = new ResourceLocation(Plethora.RESOURCE_DOMAIN, GLASSES);
	public static final ResourceLocation CHAT_CREATIVE_M = new ResourceLocation(Plethora.RESOURCE_DOMAIN, CHAT_CREATIVE);

	public static String getName(int id) {
		return id >= 0 && id < MODULES ? NAMES[id] : "unknown";
	}
}
