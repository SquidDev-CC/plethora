package org.squiddev.plethora.integration.tconstruct;

import com.google.common.collect.Maps;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.tools.IToolPart;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

/**
 * A meta provider for tools parts
 */
@IMetaProvider.Inject(value = ItemStack.class, namespace = "toolPart", modId = TConstruct.modID)
public class MetaToolPart extends BasicMetaProvider<ItemStack> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull ItemStack stack) {
		Item item = stack.getItem();
		if (!(item instanceof IToolPart)) return Collections.emptyMap();

		IToolPart toolPart = (IToolPart) item;

		Map<Object, Object> out = Maps.newHashMap();
		out.put("cost", toolPart.getCost());
		return out;
	}
}
