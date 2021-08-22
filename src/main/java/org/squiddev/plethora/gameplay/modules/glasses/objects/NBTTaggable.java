package org.squiddev.plethora.gameplay.modules.glasses.objects;

import dan200.computercraft.shared.util.NBTUtil;
import net.minecraft.nbt.NBTTagCompound;
import org.squiddev.plethora.api.method.MethodResult;
import org.squiddev.plethora.api.method.wrapper.FromTarget;
import org.squiddev.plethora.api.method.wrapper.PlethoraMethod;

public interface NBTTaggable {

	NBTTagCompound getNBTTagCompound();

	void setNBTTagCompound(NBTTagCompound nbt);

	@PlethoraMethod(doc = "function(): table -- Get all NBT tags for this object", worldThread = false)
	static MethodResult getNBTTags(@FromTarget NBTTaggable object) {
		return MethodResult.result(NBTUtil.toLua(object.getNBTTagCompound()));
	}


	@PlethoraMethod(doc = "function(nbt: table): nil -- Update a NBT Tag value for this object", worldThread = false)
	static void setNBTTags(@FromTarget NBTTaggable object, NBTTagCompound nbt) {
		object.setNBTTagCompound(nbt);
	}
}
