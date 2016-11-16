package org.squiddev.plethora.integration.vanilla;

import com.google.common.collect.Lists;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAITasks;
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
import org.squiddev.plethora.gameplay.Plethora;

import java.util.List;

public class DisableAI {
	public static final ResourceLocation DISABLE_AI = new ResourceLocation(Plethora.ID, "disableAI");

	@CapabilityInject(IDisableAIHandler.class)
	public static Capability<IDisableAIHandler> DISABLE_AI_CAPABILITY = null;

	public static void register() {
		CapabilityManager.INSTANCE.register(IDisableAIHandler.class, new Capability.IStorage<IDisableAIHandler>() {
			@Override
			public NBTBase writeNBT(Capability<IDisableAIHandler> capability, IDisableAIHandler instance, EnumFacing side) {
				return new NBTTagByte(instance.isDisabled() ? (byte) 1 : (byte) 0);
			}

			@Override
			public void readNBT(Capability<IDisableAIHandler> capability, IDisableAIHandler instance, EnumFacing side, NBTBase nbt) {
				instance.setDisabled(nbt != null && nbt instanceof NBTPrimitive && ((NBTPrimitive) nbt).getByte() == 1);
			}
		}, DefaultDisableAI.class);
	}

	public static void maybeClear(EntityLiving entity) {
		IDisableAIHandler disableAI = entity.getCapability(DISABLE_AI_CAPABILITY, null);
		if (disableAI != null && disableAI.isDisabled()) {
			clearTasks(entity.tasks);
			clearTasks(entity.targetTasks);
		}
	}

	private static void clearTasks(EntityAITasks tasks) {
		if (tasks.taskEntries.isEmpty()) return;

		List<EntityAITasks.EntityAITaskEntry> backup = Lists.newArrayList(tasks.taskEntries);
		for (EntityAITasks.EntityAITaskEntry entry : backup) {
			tasks.removeTask(entry.action);
		}
	}

	public interface IDisableAIHandler {
		boolean isDisabled();

		void setDisabled(boolean value);
	}

	public static class DefaultDisableAI implements IDisableAIHandler, ICapabilitySerializable<NBTTagCompound> {
		private boolean disabled;

		@Override
		public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
			return capability == DISABLE_AI_CAPABILITY;
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
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
			if (tag == null) {
				disabled = false;
			} else {
				disabled = tag.getBoolean("disabled");
			}
		}
	}
}
