package org.squiddev.plethora.gameplay.modules.glasses.objects;

import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import org.squiddev.plethora.api.method.MethodResult;
import org.squiddev.plethora.api.method.wrapper.FromTarget;
import org.squiddev.plethora.api.method.wrapper.Optional;
import org.squiddev.plethora.api.method.wrapper.PlethoraMethod;

import javax.annotation.Nonnull;

/**
 * An object which contains an item.
 */
public interface ItemObject {
	@Nonnull
	Item getItem();

	void setItem(@Nonnull Item item);

	int getDamage();

	void setDamage(int damage);

	@PlethoraMethod(doc = "function(): string, number -- Get the item and damage value for this object.", worldThread = false)
	static MethodResult getItem(@FromTarget ItemObject object) {
		return MethodResult.result(object.getItem().getRegistryName().toString(), object.getDamage());
	}

	@PlethoraMethod(doc = "-- Set the item and damage value for this object.", worldThread = false)
	static void setItem(@FromTarget ItemObject object, Item item, @Optional(defInt = 0) int damage) {
		object.setItem(item);
		object.setDamage(damage);
	}
}
