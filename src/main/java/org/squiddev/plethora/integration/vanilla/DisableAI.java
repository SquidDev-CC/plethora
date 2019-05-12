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
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.squiddev.plethora.gameplay.Plethora;

import javax.annotation.Nonnull;
import java.util.Optional;

public final class DisableAI {
	public static final ResourceLocation DISABLE_AI = new ResourceLocation(Plethora.ID, "disableAI");

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

	/**
	 * Check if this entity should have its AI disabled and if so, ensure that our obedience AI is inserted.
	 * @param entity
	 */
	public static void maybePossess(EntityLiving entity) {
		IDisableAIHandler disableAI = entity.getCapability(DISABLE_AI_CAPABILITY, null);
		if (disableAI != null && disableAI.isDisabled()) {
			// Check that our obedience AI task is added.
			// We don't remove this AI as it watches the Disable AI Capability for changes.
			Optional<EntityAITasks.EntityAITaskEntry> obedient = entity.tasks.taskEntries.stream().filter(it -> it.action instanceof AIPlethoraObedience).findFirst();
			if (!obedient.isPresent()) {
				entity.tasks.addTask(Integer.MIN_VALUE, new AIPlethoraObedience(entity));
			}
		}
	}

	public interface IDisableAIHandler {
		boolean isDisabled();

		void setDisabled(boolean value);
	}

	public static class DefaultDisableAI implements IDisableAIHandler, ICapabilitySerializable<NBTTagCompound> {
		private boolean disabled;

		@Override
		public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing) {
			return capability == DISABLE_AI_CAPABILITY;
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
			return capability == DISABLE_AI_CAPABILITY ? (T) this : null;
		}

		@Override
		public boolean isDisabled() {
			return disabled;
		}

		@Override
		public void setDisabled(boolean value) {
			disabled = value;
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
			disabled = tag != null && tag.getBoolean("disabled");
		}
	}

	public static class AIPlethoraObedience extends EntityAIBase {

		private EntityLiving entity;
		private EntityLookHelper oldInstance;

		AIPlethoraObedience(EntityLiving entityIn) {
			this.entity = entityIn;
		}

		private boolean isAIDisabled() {
			DisableAI.IDisableAIHandler disable = this.entity.getCapability(DisableAI.DISABLE_AI_CAPABILITY, null);
			if (disable == null) return false;
			return disable.isDisabled();
		}

		@Override
		public void startExecuting() {
			// This should not be already set when we start executing because it should have been reset.
			assert (!( this.entity.getLookHelper() instanceof PlethoraEntityLookHelper));
			this.oldInstance = this.entity.getLookHelper();
			ReflectionHelper.setPrivateValue(EntityLiving.class, this.entity, new PlethoraEntityLookHelper(), "lookHelper");
		}

		@Override
		public void resetTask() {
			if (this.entity.getLookHelper() instanceof PlethoraEntityLookHelper) {
				ReflectionHelper.setPrivateValue(EntityLiving.class, this.entity, this.oldInstance, "lookHelper");
				this.oldInstance = null;
			}
		}

		@Override
		public boolean shouldExecute() {
			return this.shouldContinueExecuting();
		}

		@Override
		public boolean shouldContinueExecuting() {
			// So long as Plethora wants this entity to not think for itself, we continue doing our work.
			return this.isAIDisabled();
		}

		@Override
		public boolean isInterruptible() {
			// We can be interrupted if the AI has been enabled
			return !this.isAIDisabled();
		}

		@Override
		public void updateTask() {
			// Noop
		}

		@Override
		public int getMutexBits() {
			return Integer.MAX_VALUE; // All the bits. This AI Task concurrency is incompatible with every other.
		}

		private class PlethoraEntityLookHelper extends EntityLookHelper {

			PlethoraEntityLookHelper() {
				super(AIPlethoraObedience.this.entity);
			}

			@Override
			public void onUpdateLook() {
				// Noop
			}
		}
	}
}
