package org.squiddev.plethora.integration.refinedstorage;

import com.google.common.collect.Maps;
import com.raoulvdberge.refinedstorage.RS;
import com.raoulvdberge.refinedstorage.api.autocrafting.task.ICraftingRequestInfo;
import com.raoulvdberge.refinedstorage.api.autocrafting.task.ICraftingTask;
import org.squiddev.plethora.api.meta.BaseMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.api.method.IPartialContext;

import javax.annotation.Nonnull;
import java.util.Map;

@IMetaProvider.Inject(modId = RS.ID, value = ICraftingTask.class)
public class MetaCraftingTask extends BaseMetaProvider<ICraftingTask> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull IPartialContext<ICraftingTask> context) {
		ICraftingTask task = context.getTarget();
		Map<Object, Object> out = Maps.newHashMap();

		ICraftingRequestInfo requested = task.getRequested();
		if (requested != null) out.put("requested", context.makePartialChild(requested));
		out.put("quantity", task.getQuantity());
		out.put("valid", !task.hasMissing());

		return out;
	}
}
