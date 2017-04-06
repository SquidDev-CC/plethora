package org.squiddev.plethora.gameplay.modules.glasses;

import com.google.common.collect.Sets;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
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
		EntityPlayer playerMP = Minecraft.getMinecraft().thePlayer;
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
		if (event.type != RenderGameOverlayEvent.ElementType.HELMET) return;

		CanvasClient canvas = getCanvas();
		if (canvas == null) return;

		GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_LINE_BIT | GL11.GL_POINT_BIT | GL11.GL_LIGHTING_BIT);
		GlStateManager.pushMatrix();

		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_LIGHTING);

		// The hotbar renders at -90 (See GuiIngame#renderTooltip)
		GL11.glTranslatef(0, 0, -100);

		ScaledResolution resolution = event.resolution;
		GL11.glScaled(resolution.getScaledWidth_double() / WIDTH, resolution.getScaledHeight_double() / HEIGHT, 0);

		synchronized (canvas.objects) {
			for (BaseObject object : canvas.objects.valueCollection()) {
				object.draw2D();
			}
		}

		GL11.glColor3f(1.0f, 1.0f, 1.0f);

		GlStateManager.popMatrix();
		GL11.glPopAttrib();
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void render3DOverlay(RenderWorldLastEvent event) {
		CanvasClient canvas = getCanvas();
		if (canvas == null) return;

		GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_LINE_BIT | GL11.GL_POINT_BIT | GL11.GL_LIGHTING_BIT | GL11.GL_COLOR_BUFFER_BIT);
		GlStateManager.pushMatrix();

		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDepthMask(false);

		Minecraft minecraft = Minecraft.getMinecraft();
		Entity renderEntity = minecraft.getRenderViewEntity();
		RenderManager renderManager = minecraft.getRenderManager();
		GlStateManager.translate(-renderManager.viewerPosX, -renderManager.viewerPosY, -renderManager.viewerPosZ);

		synchronized (canvas.objects) {
			for (BaseObject object : canvas.objects.valueCollection()) {
				object.draw3D(renderEntity);
			}
		}

		GL11.glColor3f(1.0f, 1.0f, 1.0f);
		GL11.glDepthMask(true);

		GlStateManager.popMatrix();
		GL11.glPopAttrib();
	}
}
