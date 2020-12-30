package org.squiddev.plethora.integration.refinedstorage;

import com.raoulvdberge.refinedstorage.RS;
import com.raoulvdberge.refinedstorage.api.autocrafting.preview.ICraftingPreviewElement;
import com.raoulvdberge.refinedstorage.apiimpl.autocrafting.preview.CraftingPreviewElementItemStack;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.BaseMetaProvider;
import org.squiddev.plethora.api.method.IPartialContext;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

@Injects(RS.ID)
public final class MetaCraftingPreviewElement extends BaseMetaProvider<ICraftingPreviewElement<?>> {
	@Nonnull
	@Override
	public Map<String, ?> getMeta(@Nonnull IPartialContext<ICraftingPreviewElement<?>> context) {
		ICraftingPreviewElement<?> preview = context.getTarget();

		Map<String, Object> out = new HashMap<>();
		out.put("id", preview.getId());
		out.put("available", preview.getAvailable());
		out.put("toCraft", preview.getToCraft());
		out.put("component", context.makePartialChild(preview.getElement()).getMeta());

		return out;
	}

	@Nonnull
	@Override
	public ICraftingPreviewElement<?> getExample() {
		return new CraftingPreviewElementItemStack(new ItemStack(Items.STICK, 4));
	}
}
