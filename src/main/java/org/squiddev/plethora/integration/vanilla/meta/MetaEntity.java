package org.squiddev.plethora.integration.vanilla.meta;

import com.google.common.collect.Maps;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.MetaProvider;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

@MetaProvider(Entity.class)
public class MetaEntity extends BasicMetaProvider<Entity> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull Entity object) {
		return getBasicProperties(object);
	}

	public static HashMap<Object, Object> getBasicProperties(@Nonnull Entity entity) {
		HashMap<Object, Object> result = Maps.newHashMap();
		result.put("id", entity.getUniqueID().toString());

		result.put("name", getName(entity));
		result.put("displayName", entity.getName());

		result.put("motionX", entity.motionX);
		result.put("motionY", entity.motionY);
		result.put("motionZ", entity.motionZ);

		result.put("pitch", Math.toRadians(entity.rotationPitch));
		result.put("yaw", Math.toRadians(entity.rotationYaw));

		return result;
	}

	public static String getName(Entity entity) {
		String name = EntityList.getEntityString(entity);
		if (name == null) {
			if (entity instanceof EntityPlayer) {
				return entity.getName();
			} else if (entity.hasCustomName()) {
				return entity.getCustomNameTag();
			} else {
				return "unknown";
			}
		} else {
			return name;
		}
	}
}
