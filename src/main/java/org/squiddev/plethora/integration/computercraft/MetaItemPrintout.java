package org.squiddev.plethora.integration.computercraft;

import com.google.common.collect.Maps;
import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.media.items.ItemPrintout;
import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

/**
 * Meta provider for printed outs: printed page(s) and books.
 */
@IMetaProvider.Inject(value = ItemStack.class, modId = "ComputerCraft", namespace = "printout")
public class MetaItemPrintout extends BasicMetaProvider<ItemStack> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull ItemStack object) {
		if (object.getItem() != ComputerCraft.Items.printout) return Collections.emptyMap();

		Map<Object, Object> out = Maps.newHashMap();
		out.put("type", ItemPrintout.getType(object).toString());
		out.put("title", ItemPrintout.getTitle(object));
		out.put("pages", ItemPrintout.getPageCount(object));

		Map<Integer, String> lines = Maps.newHashMap();
		String[] lineArray = ItemPrintout.getText(object);
		for (int i = 0; i < lineArray.length; i++) {
			lines.put(i + 1, lineArray[i]);
		}
		out.put("lines", lines);

		return out;
	}
}
