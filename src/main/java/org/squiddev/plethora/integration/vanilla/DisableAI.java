package org.squiddev.plethora.integration.vanilla;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.ai.EntityLookHelper;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import org.squiddev.plethora.gameplay.Plethora;
import org.squiddev.plethora.integration.PlethoraIntegration;
import org.squiddev.plethora.utils.TypedField;

import javax.annotation.Nonnull;

public final class DisableAI {
	static final ResourceLocation DISABLE_AI = new ResourceLocation(Plethora.ID, "disableAI");

	@CapabilityInject(IDisableAIHandler.class)
	public static Capability<IDisableAIHandler> DISABLE_AI_CAPABILITY = null;

	private DisableAI() {
	}

	public static void register() {
		CapabilityManager.INSTANCE.register(IDisableAIHandler.class, new Capability.IStorage<IDisableAIHandler>() {
			@Override
			public NBTBase writeNBT(Capability<IDisableAIHandler> capability, IDisableAIHandler instance, EnumFacing side) {
				return new NBTTagByte((byte) (instance.isDisabled() ? 1 : 0));
			}

			@Override
			public void readNBT(Capability<IDisableAIHandler> capability, IDisableAIHandler instance, EnumFacing side, NBTBase nbt) {
				instance.setDisabled(nbt instanceof NBTPrimitive && ((NBTPrimitive) nbt).getByte() == 1);
			}
		}, DefaultDisableAI::new);
	}

	public interface IDisableAIHandler extends ITickable {
		boolean isDisabled();

		void setDisabled(boolean value);
	}

	static class DefaultDisableAI implements IDisableAIHandler, ICapabilitySerializable<NBTTagCompound> {
		private final EntityLiving entity;
		private boolean disabled;
		private EntityAITasks.EntityAITaskEntry task;

		DefaultDisableAI(EntityLiving entity) {
			this.entity = entity;
		}

		DefaultDisableAI() {
			entity = null;
		}

		@Override
		public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing) {
			return capability == DISABLE_AI_CAPABILITY;
		}

		@Override
		public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
			return capability == DISABLE_AI_CAPABILITY ? DISABLE_AI_CAPABILITY.cast(this) : null;
		}

		@Override
		public boolean isDisabled() {
			return disabled;
		}

		@Override
		public void setDisabled(boolean value) {
			if (value == disabled) return;

			disabled = value;
			if (entity != null) entity.getNavigator().clearPath();
			update();
		}

		@Override
		public void update() {
			if (!disabled || entity == null) return;

			// Check that our obedience AI task is added.
			// We don't remove this AI as it watches the Disable AI Capability for changes.
			if (task == null) {
				entity.tasks.addTask(Integer.MIN_VALUE, new AIPlethoraObedience(entity, this));
				task = entity.tasks.taskEntries.stream()
					.filter(it -> it.action instanceof AIPlethoraObedience).findFirst()
					.orElse(null);
			} else {
				entity.tasks.taskEntries.add(task);
			}
		}

		@Override
		public NBTTagCompound serializeNBT() {
			if (disabled) {
				NBTTagCompound tag = new NBTTagCompound();
				tag.setBoolean("disabled", true);
				return tag;
			} else {
				return new NBTTagCompound();
			}
		}

		@Override
		public void deserializeNBT(NBTTagCompound tag) {
			setDisabled(tag != null && tag.getBoolean("disabled"));
		}
	}

	private static class AIPlethoraObedience extends EntityAIBase {
		private static final TypedField<EntityLiving, EntityLookHelper> LOOK_HELPER = TypedField.of(
			EntityLiving.class, "lookHelper", "field_70749_g"
		);

		private final EntityLiving entity;
		private final IDisableAIHandler handler;

		private final PlethoraEntityLookHelper plethoraLook;
		private EntityLookHelper oldLook;

		AIPlethoraObedience(EntityLiving entity, IDisableAIHandler handler) {
			this.handler = handler;
			this.entity = entity;
			plethoraLook = new PlethoraEntityLookHelper(this.entity);
		}

		private boolean isAIDisabled() {
			return handler.isDisabled();
		}

		@Override
		public void startExecuting() {
			// This should not be already set when we start executing because it should have been reset.
			if (entity.getLookHelper() instanceof PlethoraEntityLookHelper) {
				PlethoraIntegration.LOG.error("Look helper is not a PlethoraEntityLookHelper ({} instead)", entity.getLookHelper());
				return;
			}

			oldLook = entity.getLookHelper();
			LOOK_HELPER.set(entity, plethoraLook);
		}

		@Override
		public void resetTask() {
			if (entity.getLookHelper() instanceof PlethoraEntityLookHelper) {
				LOOK_HELPER.set(entity, oldLook);
				oldLook = null;
			}
		}

		@Override
		public boolean shouldExecute() {
			return shouldContinueExecuting();
		}

		@Override
		public boolean shouldContinueExecuting() {
			// So long as Plethora wants this entity to not think for itself, we continue doing our work.
			return isAIDisabled();
		}

		@Override
		public boolean isInterruptible() {
			// We can be interrupted if the AI has been enabled
			return !isAIDisabled();
		}

		@Override
		public void updateTask() {
		}

		@Override
		public int getMutexBits() {
			// All the bits. This AI Task concurrency is incompatible with every other.
			return Integer.MAX_VALUE;
		}
	}

	private static class PlethoraEntityLookHelper extends EntityLookHelper {
		PlethoraEntityLookHelper(EntityLiving entity) {
			super(entity);
		}

		@Override
		public void onUpdateLook() {
		}
	}
}
