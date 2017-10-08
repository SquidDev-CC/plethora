package org.squiddev.plethora.gameplay.modules.glasses;

import com.google.common.collect.Sets;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.squiddev.plethora.gameplay.Plethora;
import org.squiddev.plethora.gameplay.modules.PlethoraModules;
import org.squiddev.plethora.gameplay.neural.NeuralHelpers;

import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

import static net.minecraftforge.common.util.Constants.NBT;
import static org.squiddev.plethora.gameplay.neural.ItemComputerHandler.MODULE_DATA;

public class CanvasHandler {
	public static final double WIDTH = 512;
	public static final double HEIGHT = 512 / 16 * 9;

	private static AtomicInteger id = new AtomicInteger(0);
	private static final HashSet<CanvasServer> server = Sets.newHashSet();

	private static final TIntObjectHashMap<CanvasClient> client = new TIntObjectHashMap<CanvasClient>();

	public static int nextId() {
		return id.getAndIncrement();
	}

	public static void addServer(CanvasServer canvas) {
		synchronized (server) {
			server.add(canvas);
			Plethora.network.sendTo(canvas.getAddMessage(), canvas.getPlayer());
		}
	}

	public static void removeServer(CanvasServer canvas) {
		synchronized (server) {
			server.remove(canvas);
			Plethora.network.sendTo(canvas.getRemoveMessage(), canvas.getPlayer());
		}
	}

	public static void addClient(CanvasClient canvas) {
		synchronized (client) {
			client.put(canvas.id, canvas);
		}
	}

	public static void removeClient(CanvasClient canvas) {
		synchronized (client) {
			client.remove(canvas.id);
		}
	}

	public static CanvasClient getClient(int id) {
		synchronized (client) {
			return client.get(id);
		}
	}

	public static void clear() {
		synchronized (server) {
			server.clear();
		}
		synchronized (client) {
			client.clear();
		}
	}

	@SubscribeEvent
	public void update(TickEvent.ServerTickEvent event) {
		if (event.phase != TickEvent.Phase.START) return;

		synchronized (server) {
			for (CanvasServer canvas : server) {
				MessageCanvasUpdate update = canvas.getUpdateMessage();
				if (update != null) {
					Plethora.network.sendTo(update, canvas.getPlayer());
				}
			}
		}
	}

	@SideOnly(Side.CLIENT)
	private static CanvasClient getCanvas() {
		EntityPlayer playerMP = Minecraft.getMinecraft().player;
		ItemStack stack = NeuralHelpers.getStack(playerMP);

		if (stack == null) return null;

		NBTTagCompound tag = stack.getTagCompound();
		if (tag == null || !tag.hasKey(MODULE_DATA, NBT.TAG_COMPOUND)) return null;

		NBTTagCompound modules = tag.getCompoundTag(MODULE_DATA);
		if (!modules.hasKey(PlethoraModules.GLASSES_S, NBT.TAG_COMPOUND)) {
			return null;
		}

		NBTTagCompound data = modules.getCompoundTag(PlethoraModules.GLASSES_S);
		if (!data.hasKey("id", NBT.TAG_ANY_NUMERIC)) return null;

		int id = data.getInteger("id");
		return getClient(id);
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void render2DOverlay(RenderGameOverlayEvent.Post event) {
		if (event.getType() != RenderGameOverlayEvent.ElementType.HELMET) return;

		CanvasClient canvas = getCanvas();
		if (canvas == null) return;

		GlStateManager.pushMatrix();

		GlStateManager.enableBlend();
		GlStateManager.disableAlpha();
		GlStateManager.disableTexture2D();
		GlStateManager.disableLighting();

		// The hotbar renders at -90 (See GuiIngame#renderTooltip)
		GlStateManager.translate(0, 0, -100);

		ScaledResolution resolution = event.getResolution();
		GlStateManager.scale(resolution.getScaledWidth_double() / WIDTH, resolution.getScaledHeight_double() / HEIGHT, 0);

		synchronized (canvas.objects) {
			for (BaseObject object : canvas.objects.valueCollection()) {
				object.draw2D();
			}
		}

		GlStateManager.color(1.0f, 1.0f, 1.0f);
		GlStateManager.enableTexture2D();

		GlStateManager.popMatrix();
	}
}
