package org.squiddev.plethora.gameplay.client;

import baubles.common.Baubles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.*;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.*;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.squiddev.plethora.gameplay.registry.IClientModule;
import org.squiddev.plethora.gameplay.registry.Module;
import org.squiddev.plethora.utils.DebugLogger;

import static org.squiddev.plethora.gameplay.client.ModelInterface.getMonocle;
import static org.squiddev.plethora.gameplay.client.ModelInterface.getNormal;

public class RenderInterfaceLiving extends Module implements IClientModule {
	@Override
	@SideOnly(Side.CLIENT)
	public void clientInit() {
		/**
		 * Anything small looks stupid. We don't allow attaching to baby animals.
		 *
		 * See {@link RenderManager} for entity to model mappings
		 *
		 * @see net.minecraft.client.model.
		 */

		/**
		 * @see net.minecraft.entity.monster
		 */
		inject(EntityBlaze.class, 0, 3, 0);
		inject(EntityCaveSpider.class, 0, 2, -4);
		inject(EntityCreeper.class, 0, -1, 0);
		injectMonocle(EntityElderGuardian.class, -1, 20.5f, 4.2f, 0, 0, 0);
		inject(EntityEnderman.class, 0, 0, 0);
		injectMonocle(EntityEndermite.class, -1.5f, 5, 3.1f, 0, 0, 0);
		inject(EntityEvoker.class, 0, 0, 0);
		inject(EntityGhast.class, 1, 12, -4);
		// EntityGiantZombie renders armor
		injectMonocle(EntityGuardian.class, -1, 20.5f, 4.2f, 0, 0, 0);
		inject(EntityIronGolem.class, 0, -2, -1);
		inject(EntityMagmaCube.class, 0, 23, 0);
		// EntityPigZombie renders armor
		inject(EntityPolarBear.class, 0, 3.5f, 1);
		injectMonocle(EntityShulker.class, 0.1f, 7, 1, 0, 0, 0);
		injectMonocle(EntitySilverfish.class, -0.5f, 4f, 3.1f, 0, 0, 0);
		// EntitySkeleton renders armor
		inject(EntitySlime.class, 0, 23, 0); // This scales on size. I love it.
		inject(EntitySnowman.class, 1, -3, -1);
		inject(EntitySpider.class, 0, 2, -4);
		// EntityStray renders armour
		inject(EntityVex.class, 0, 0, 0);
		inject(EntityVindicator.class, 0, 0, 0);
		// EntityWitherSkeleton renders armour
		inject(EntityWitch.class, 0, 0, 0);
		// EntityZombie renders armor
		// EntityZombieVillage renders armor

		/**
		 * @see net.minecraft.entity.passive
		 */
		injectMonocle(EntityBat.class, 0, 4, 1, 0, 0, 0);
		injectMonocle(EntityChicken.class, 0, -0.5f, 2, 0, 0, 0);
		inject(EntityCow.class, 0, 4, -2);
		injectMonocle(EntityDonkey.class, -1.5f, -4.5f, 0, 0, -90, 0);
		injectMonocle(EntityHorse.class, -1.5f, -4.5f, 0, 0, -90, 0);
		inject(EntityLlama.class, 0, -10, -2);
		inject(EntityMooshroom.class, 0, 4, -2);
		injectMonocle(EntityMule.class, -1.5f, -4.5f, 0, 0, -90, 0);
		inject(EntityOcelot.class, -1, 3, 0);
		injectMonocle(EntityParrot.class, -3, 2, -2, 0, -90, 0);
		inject(EntityPig.class, 0, 4, -4);
		injectMonocle(EntityRabbit.class, 0, 5, 1.5f, 0, 0, 0);
		inject(EntitySheep.class, 0, 2, -2);
		injectMonocle(EntitySkeletonHorse.class, -1.5f, -4.5f, 0, 0, -90, 0);
		inject(EntitySquid.class, 2, 3, -2);
		inject(EntityVillager.class, 0, 0, 0);
		inject(EntityWolf.class, 0, 3, 2);
		injectMonocle(EntityZombieHorse.class, -1.5f, -4.5f, 0, 0, -90, 0);

		// Inject the baubles renderer
		if (Loader.isModLoaded(Baubles.MODID)) {
			for (RenderPlayer render : Minecraft.getMinecraft().getRenderManager().getSkinMap().values()) {
				render.addLayer(new LayerBaublesInterface(render.getMainModel()));
			}
		}
	}

	@SideOnly(Side.CLIENT)
	private void inject(Class<? extends EntityLivingBase> klass, float dx, float dy, float dz) {
		RenderManager manager = Minecraft.getMinecraft().getRenderManager();
		inject(manager.getEntityClassRenderObject(klass), getNormal(), dx, dy, dz, 0, 0, 0);
	}

	@SideOnly(Side.CLIENT)
	private void injectMonocle(Class<? extends EntityLivingBase> klass, float dx, float dy, float dz, float rx, float ry, float rz) {
		RenderManager manager = Minecraft.getMinecraft().getRenderManager();
		inject(manager.getEntityClassRenderObject(klass), getMonocle(), dx, dy, dz, rx, ry, rz);
	}

	@SideOnly(Side.CLIENT)
	private void inject(Render<?> render, ModelInterface iface, float dx, float dy, float dz, float rx, float ry, float rz) {
		if (render instanceof RenderLiving<?>) {
			RenderLiving<?> living = (RenderLiving) render;
			ModelRenderer head = getHead(living.getMainModel());
			if (head != null) {
				living.addLayer(new LayerInterface(head, iface, dx, dy, dz, rx, ry, rz));
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
			// squidBody
			return ObfuscationReflectionHelper.getPrivateValue(ModelSquid.class, (ModelSquid) model, "field_78202_a");
		} else if (model instanceof ModelBlaze) {
			// blazeHead
			return ObfuscationReflectionHelper.getPrivateValue(ModelBlaze.class, (ModelBlaze) model, "field_78105_b");
		} else if (model instanceof ModelOcelot) {
			// ocelotHead
			return ObfuscationReflectionHelper.getPrivateValue(ModelOcelot.class, (ModelOcelot) model, "field_78156_g");
		} else if (model instanceof ModelEnderman) {
			return ((ModelEnderman) model).bipedHeadwear;
		} else if (model instanceof ModelGuardian) {
			// guardianEye
			return ObfuscationReflectionHelper.getPrivateValue(ModelGuardian.class, (ModelGuardian) model, "field_178708_b");
		} else if (model instanceof ModelRabbit) {
			// rabbitHead
			return ObfuscationReflectionHelper.getPrivateValue(ModelRabbit.class, (ModelRabbit) model, "field_178704_h");
		} else if (model instanceof ModelSlime) {
			// slimeBodies
			return ObfuscationReflectionHelper.getPrivateValue(ModelSlime.class, (ModelSlime) model, "field_78200_a");
		} else if (model instanceof ModelMagmaCube) {
			// core
			return ObfuscationReflectionHelper.getPrivateValue(ModelMagmaCube.class, (ModelMagmaCube) model, "field_78108_b");
		} else if (model instanceof ModelSpider) {
			return ((ModelSpider) model).spiderHead;
		} else if (model instanceof ModelGhast) {
			// body
			return ObfuscationReflectionHelper.getPrivateValue(ModelGhast.class, (ModelGhast) model, "field_78128_a");
		} else if (model instanceof ModelHorse) {
			// head
			return ObfuscationReflectionHelper.getPrivateValue(ModelHorse.class, (ModelHorse) model, "field_110709_a");
		} else if (model instanceof ModelBat) {
			// batHead,
			return ObfuscationReflectionHelper.getPrivateValue(ModelBat.class, (ModelBat) model, "field_82895_a");
		} else if (model instanceof ModelSilverfish) {
			ModelRenderer[] renderers = ObfuscationReflectionHelper.getPrivateValue(ModelSilverfish.class, (ModelSilverfish) model, "field_78171_a");
			return renderers[0];
		} else if (model instanceof ModelEnderMite) {
			ModelRenderer[] renderers = ObfuscationReflectionHelper.getPrivateValue(ModelEnderMite.class, (ModelEnderMite) model, "field_178713_d");
			return renderers[0];
		} else if (model instanceof ModelShulker) {
			return ((ModelShulker) model).head;
		} else if (model instanceof ModelIllager) {
			return ((ModelIllager) model).head;
		} else if (model instanceof ModelParrot) {
			return ObfuscationReflectionHelper.getPrivateValue(ModelParrot.class, (ModelParrot) model, "field_192768_e");
		} else if (model instanceof ModelBiped) {
			return ((ModelBiped) model).bipedHead;
		} else {
			return null;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void clientPreInit() {
	}
}
