package org.squiddev.plethora.modules.methods;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
import org.squiddev.plethora.api.PlethoraAPI;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.method.Method;
import org.squiddev.plethora.api.module.IModule;
import org.squiddev.plethora.api.module.ModuleMethod;
import org.squiddev.plethora.modules.ItemModule;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.squiddev.plethora.api.reference.Reference.id;

public final class IntrospectionModule {
	private static final ResourceLocation MODULE = ItemModule.toResource(ItemModule.INTROSPECTION);

	@Method(IModule.class)
	public static final class GetInventoryMethod extends ModuleMethod {
		public GetInventoryMethod() {
			super("getInventory", true, MODULE);
		}

		@Nullable
		@Override
		public Object[] apply(@Nonnull IContext<IModule> context, @Nonnull Object[] args) throws LuaException {
			EntityPlayer player = context.getContext(EntityPlayer.class);
			if (player == null) throw new LuaException("Player not found");

			IInventory inventory = player.inventory;
			IUnbakedContext<IInventory> newContext = context.makeChild(id(inventory));
			return new Object[]{PlethoraAPI.instance().methodRegistry().getObject(newContext)};
		}
	}

	@Method(IModule.class)
	public static final class GetEnderMethod extends ModuleMethod {
		public GetEnderMethod() {
			super("getEnder", true, MODULE);
		}

		@Nullable
		@Override
		public Object[] apply(@Nonnull IContext<IModule> context, @Nonnull Object[] args) throws LuaException {
			EntityPlayer player = context.getContext(EntityPlayer.class);
			if (player == null) throw new LuaException("Player not found");

			IInventory inventory = player.getInventoryEnderChest();
			IUnbakedContext<IInventory> newContext = context.makeChild(id(inventory));
			return new Object[]{PlethoraAPI.instance().methodRegistry().getObject(newContext)};
		}
	}

	@Method(IModule.class)
	public static final class GetIDMethod extends ModuleMethod {
		public GetIDMethod() {
			super("getID", true, MODULE);
		}

		@Nullable
		@Override
		public Object[] apply(@Nonnull IContext<IModule> context, @Nonnull Object[] args) throws LuaException {
			EntityPlayer player = context.getContext(EntityPlayer.class);
			if (player == null) throw new LuaException("Player not found");

			return new Object[]{player.getUniqueID().toString()};
		}
	}

	@Method(IModule.class)
	public static final class GetNameMethod extends ModuleMethod {
		public GetNameMethod() {
			super("getName", true, MODULE);
		}

		@Nullable
		@Override
		public Object[] apply(@Nonnull IContext<IModule> context, @Nonnull Object[] args) throws LuaException {
			EntityPlayer player = context.getContext(EntityPlayer.class);
			if (player == null) throw new LuaException("Player not found");

			return new Object[]{player.getName()};
		}
	}
}
