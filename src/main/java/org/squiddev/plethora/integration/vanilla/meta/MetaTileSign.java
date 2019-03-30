package org.squiddev.plethora.integration.vanilla.meta;

import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
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

		Map<Object, Object> text = new HashMap<>(lines.length);
		for (int i = 0; i < lines.length; i++) {
			text.put(i + 1, lines[i].getUnformattedText());
		}

		return text;
	}

	@Nullable
	@Override
	public TileEntitySign getExample() {
		TileEntitySign sign = new TileEntitySign();
		sign.signText[0] = new TextComponentString("This is");
		sign.signText[1] = new TextComponentString("my rather fancy");
		sign.signText[2] = new TextComponentString("sign.");
		return sign;
	}
}
