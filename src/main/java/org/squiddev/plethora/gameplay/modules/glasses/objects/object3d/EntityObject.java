package org.squiddev.plethora.gameplay.modules.glasses.objects.object3d;


import net.minecraftforge.fml.common.registry.EntityEntry;
import org.squiddev.plethora.api.method.MethodResult;
import org.squiddev.plethora.api.method.wrapper.FromTarget;
import org.squiddev.plethora.api.method.wrapper.PlethoraMethod;

import javax.annotation.Nonnull;

/**
 * An object which contains an entity.
 */
public interface EntityObject {

	@Nonnull
	EntityEntry getEntityEntry();

	void setEntityEntry(@Nonnull EntityEntry entityEntry);

	@PlethoraMethod(doc = "function(): string -- Get the entity name for this object.", worldThread = false)
	static MethodResult getEntity(@FromTarget EntityObject object) {
		return MethodResult.result(object.getEntityEntry().getRegistryName().toString());
	}

	@PlethoraMethod(doc = "function(string name): -- Set the entity value for this object.", worldThread = false)
	static void setEntity(@FromTarget EntityObject object, EntityEntry entityEntry) {
		object.setEntityEntry(entityEntry);
	}
}
