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
		/**
		 * Anything small looks stupid. We don't allow attaching to baby animals.
		 * We don't render endermites, silverfish, bats or chickens.
		 *
		 * See {@link RenderManager} for entity to model mappings
		 */

		/**
		 * @see net.minecraft.entity.monster
		 */
		inject(EntityBlaze.class, 0, 3, 0);
		inject(EntityCaveSpider.class, 0, 2, -3); // Same as normal spider
		inject(EntityCreeper.class, 0, -1, 0);
		inject(EntityEnderman.class, 0, 0, 0);
		// IGNORE: Endermite
		inject(EntityGhast.class, 1, 12, -4);
		inject(EntityGuardian.class, -1, 20.5f, -4); // Move to eye?
		inject(EntityIronGolem.class, 0, -2, -1);
		inject(EntityMagmaCube.class, 0, 23, 0);
		// PigZombie renders armor
		// IGNORE: Silverfish
		// Skeleton renders armor
		inject(EntitySlime.class, 0, 23, 0); // This scales on size. I love it.
		inject(EntitySnowman.class, 1, -3, -1);
		inject(EntitySpider.class, 0, 2, -3);
		inject(EntityWitch.class, 0, 0, 0);
		// Zombie renders armor

		/**
		 * @see net.minecraft.entity.passive
		 */
		// IGNORE: Bat
		// IGNORE: Chicken
		inject(EntityCow.class, 0, 4, -2);
		// IGNORE: Horse: the eyes are on the side of the face
		inject(EntityMooshroom.class, 0, 4, -2);
		inject(EntityOcelot.class, -1, 3, 0);
		inject(EntityPig.class, 0, 4, -4);
		inject(EntityRabbit.class, 0, 1, -1);
		inject(EntitySheep.class, 0, 2, -2);
		inject(EntitySquid.class, 2, 3, -2);
		inject(EntityVillager.class, 0, 0, 0);
		inject(EntityWolf.class, 0, 4, 3);
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
		} else if (model instanceof ModelGuardian) {
			return ((ModelGuardian) model).guardianBody;
		} else if (model instanceof ModelRabbit) {
			return ((ModelRabbit) model).rabbitHead;
		} else if (model instanceof ModelSlime) {
			return ((ModelSlime) model).slimeBodies;
		} else if (model instanceof ModelMagmaCube) {
			return ((ModelMagmaCube) model).core;
		} else if (model instanceof ModelSpider) {
			return ((ModelSpider) model).spiderHead;
		} else if (model instanceof ModelGhast) {
			return ((ModelGhast) model).body;
		} else {
			return null;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void clientPreInit() {
	}
}
