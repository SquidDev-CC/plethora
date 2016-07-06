package org.squiddev.plethora.integration.vanilla.method;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;
import org.squiddev.plethora.EquipmentInvWrapper;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.method.Method;
import org.squiddev.plethora.api.module.IModule;
import org.squiddev.plethora.api.module.ModuleMethod;
import org.squiddev.plethora.gameplay.modules.PlethoraModules;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.squiddev.plethora.api.reference.Reference.id;

public final class MethodsIntrospection {
	@Method(IModule.class)
	public static final class MethodEntityPlayerInventory extends ModuleMethod {
		public MethodEntityPlayerInventory() {
			super("getInventory", true, PlethoraModules.INTROSPECTION);
		}

		@Nullable
		@Override
		public Object[] apply(@Nonnull IContext<IModule> context, @Nonnull Object[] args) throws LuaException {
			EntityPlayer player = context.getContext(EntityPlayer.class);
			if (player == null) throw new LuaException("Player not found");

			IItemHandler inventory = new PlayerMainInvWrapper(player.inventory);
			IUnbakedContext<IItemHandler> newContext = context.makeChild(id(inventory));
			return new Object[]{newContext.getObject()};
		}
	}

	@Method(IModule.class)
	public static final class MethodEntityEquipment extends ModuleMethod {
		public MethodEntityEquipment() {
			super("getEquipment", true, PlethoraModules.INTROSPECTION);
		}

		@Nullable
		@Override
		public Object[] apply(@Nonnull IContext<IModule> context, @Nonnull Object[] args) throws LuaException {
			EntityLivingBase entity = context.getContext(EntityLivingBase.class);
			if (entity == null) throw new LuaException("Entity not found");

			IItemHandler inventory = new EquipmentInvWrapper(entity);
			IUnbakedContext<IItemHandler> newContext = context.makeChild(id(inventory));
			return new Object[]{newContext.getObject()};
		}
	}

	@Method(IModule.class)
	public static final class MethodEntityPlayerGetEnder extends ModuleMethod {
		public MethodEntityPlayerGetEnder() {
			super("getEnder", true, PlethoraModules.INTROSPECTION);
		}

		@Nullable
		@Override
		public Object[] apply(@Nonnull IContext<IModule> context, @Nonnull Object[] args) throws LuaException {
			EntityPlayer player = context.getContext(EntityPlayer.class);
			if (player == null) throw new LuaException("Player not found");

			IInventory inventory = player.getInventoryEnderChest();
			IUnbakedContext<IInventory> newContext = context.makeChild(id(inventory));
			return new Object[]{newContext.getObject()};
		}
	}

	@Method(IModule.class)
	public static final class MethodEntityGetID extends ModuleMethod {
		public MethodEntityGetID() {
			super("getID", true, PlethoraModules.INTROSPECTION);
		}

		@Nullable
		@Override
		public Object[] apply(@Nonnull IContext<IModule> context, @Nonnull Object[] args) throws LuaException {
			EntityLivingBase entity = context.getContext(EntityLivingBase.class);
			if (entity == null) throw new LuaException("Entity not found");

			return new Object[]{entity.getUniqueID().toString()};
		}
	}

	@Method(IModule.class)
	public static final class MethodEntityGetName extends ModuleMethod {
		public MethodEntityGetName() {
			super("getName", true, PlethoraModules.INTROSPECTION);
		}

		@Nullable
		@Override
		public Object[] apply(@Nonnull IContext<IModule> context, @Nonnull Object[] args) throws LuaException {
			EntityLivingBase entity = context.getContext(EntityLivingBase.class);
			if (entity == null) throw new LuaException("Entity not found");

			return new Object[]{entity.getName()};
		}
	}
}
