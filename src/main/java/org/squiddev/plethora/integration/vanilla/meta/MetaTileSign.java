package org.squiddev.plethora.integration.vanilla.meta;

import com.google.common.collect.Maps;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.text.ITextComponent;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;

import javax.annotation.Nonnull;
import java.util.Map;

@IMetaProvider.Inject(value = TileEntitySign.class, namespace = "sign")
public class MetaTileSign extends BasicMetaProvider<TileEntitySign> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull TileEntitySign object) {
		return getSignLines(object);
	}

	public static Map<Object, Object> getSignLines(TileEntitySign sign) {
		ITextComponent[] lines = sign.signText;

		Map<Object, Object> text = Maps.newHashMapWithExpectedSize(lines.length);
		for (int i = 0; i < lines.length; i++) {
			text.put(i + 1, lines[i].getUnformattedText());
		}

		return text;
	}
}
