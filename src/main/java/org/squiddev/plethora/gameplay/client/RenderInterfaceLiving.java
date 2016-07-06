package org.squiddev.plethora.gameplay.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.*;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.*;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.squiddev.plethora.gameplay.registry.IClientModule;
import org.squiddev.plethora.gameplay.registry.Module;
import org.squiddev.plethora.utils.DebugLogger;

public class RenderInterfaceLiving extends Module implements IClientModule {
	@Override
	@SideOnly(Side.CLIENT)
	public void clientInit() {
		// Chickens look stupid :(
		// So do baby animals

		inject(EntityBlaze.class, 0, 3, 0);
		inject(EntityCow.class, 0, 4, -2);
		inject(EntityCreeper.class, 0, -1, 0);
		inject(EntityIronGolem.class, 0, -2, -1);
		inject(EntityMooshroom.class, 0, 4, -2);
		inject(EntityOcelot.class, -1, 3, 0);
		inject(EntityPig.class, 0, 4, -4);
		inject(EntitySheep.class, 0, 2, -2);
		inject(EntitySnowman.class, 1, -3, -1);
		inject(EntitySquid.class, 2, 3, -2);
		inject(EntityVillager.class, 0, 0, 0);
		inject(EntityWitch.class, 0, 0, 0);
		inject(EntityWolf.class, 0, 4, 3);

		// TODO: Rabbit, Magma Cube, Slime, Guardian, Spider, Enderman
	}

	@SideOnly(Side.CLIENT)
	private void inject(Class<? extends EntityLivingBase> klass, float dx, float dy, float dz) {
		RenderManager manager = Minecraft.getMinecraft().getRenderManager();
		inject(manager.getEntityClassRenderObject(klass), dx, dy, dz);
	}

	@SideOnly(Side.CLIENT)
	private void inject(Render<?> render, float dx, float dy, float dz) {
		if (render instanceof RenderLiving<?>) {
			RenderLiving<?> living = (RenderLiving) render;
			ModelRenderer head = getHead(living.getMainModel());
			if (head != null) {
				living.addLayer(new LayerInterface(head, dx, dy, dz));
			} else {
				DebugLogger.warn("Cannot inject neural renderer for " + render);
			}
		} else {
			DebugLogger.warn("Cannot inject neural renderer for " + render);
		}
	}

	@SideOnly(Side.CLIENT)
	private ModelRenderer getHead(ModelBase model) {
		if (model instanceof ModelQuadruped) {
			return ((ModelQuadruped) model).head;
		} else if (model instanceof ModelChicken) {
			return ((ModelChicken) model).head;
		} else if (model instanceof ModelVillager) {
			return ((ModelVillager) model).villagerHead;
		} else if (model instanceof ModelWolf) {
			return ((ModelWolf) model).wolfHeadMain;
		} else if (model instanceof ModelCreeper) {
			return ((ModelCreeper) model).head;
		} else if (model instanceof ModelSnowMan) {
			return ((ModelSnowMan) model).head;
		} else if (model instanceof ModelIronGolem) {
			return ((ModelIronGolem) model).ironGolemHead;
		} else if (model instanceof ModelSquid) {
			return ((ModelSquid) model).squidBody;
		} else if (model instanceof ModelBlaze) {
			return ((ModelBlaze) model).blazeHead;
		} else if (model instanceof ModelOcelot) {
			return ((ModelOcelot) model).ocelotHead;
		} else if (model instanceof ModelEnderman) {
			return ((ModelEnderman) model).bipedHeadwear;
		} else {
			return null;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void clientPreInit() {
	}
}
