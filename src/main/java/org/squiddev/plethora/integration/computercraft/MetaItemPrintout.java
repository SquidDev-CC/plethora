package org.squiddev.plethora.integration.computercraft;

import com.google.common.collect.Maps;
import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.media.items.ItemPrintout;
import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.ItemStackMetaProvider;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Meta provider for printed outs: printed page(s) and books.
 */
@Injects(ComputerCraft.MOD_ID)
public final class MetaItemPrintout extends ItemStackMetaProvider<ItemPrintout> {
	public MetaItemPrintout() {
		super("printout", ItemPrintout.class);
	}

	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull ItemStack object, @Nonnull ItemPrintout item) {
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

	@Nonnull
	@Override
	public ItemStack getExample() {
		return ItemPrintout.createSingleFromTitleAndText("My page",
			new String[]{"This is the first line"},
			new String[]{"FFFFFFFFFFFFFFFFFFFFFF"}
		);
	}
}
