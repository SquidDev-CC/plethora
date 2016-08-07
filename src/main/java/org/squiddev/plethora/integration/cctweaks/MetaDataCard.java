package org.squiddev.plethora.integration.cctweaks;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.squiddev.cctweaks.api.IDataCard;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

@IMetaProvider.Inject(value = ItemStack.class, namespace = "dataCard", modId = "CCTweaks")
public class MetaDataCard extends BasicMetaProvider<ItemStack> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull ItemStack stack) {
		Item item = stack.getItem();
		if (!(item instanceof IDataCard)) return Collections.emptyMap();
		IDataCard card = (IDataCard) item;

		return Collections.<Object, Object>singletonMap("type", card.getType(stack));
	}
}
