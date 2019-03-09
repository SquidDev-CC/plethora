package org.squiddev.plethora.core.gen;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.ILuaObject;
import dan200.computercraft.api.lua.ILuaTask;
import dan200.computercraft.api.lua.LuaException;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraftforge.common.config.Configuration;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.squiddev.plethora.core.ConfigCore;
import org.squiddev.plethora.core.ContextFactory;
import org.squiddev.plethora.core.executor.BasicExecutor;
import org.squiddev.plethora.integration.vanilla.method.MethodsVanillaTileEntities;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.squiddev.plethora.api.reference.Reference.id;

public class PlethoraMethodRegistryTest {
	@Before
	public void before() {
		ConfigCore.Testing.bytecodeVerify = true;
		ConfigCore.Testing.strict = true;
		ConfigCore.configuration = new Configuration();
		ConfigCore.baseCosts = ConfigCore.configuration.getCategory("baseCosts");
	}

	@Test
	public void testAddNoSync() throws NoSuchMethodException, LuaException, InterruptedException {
		Method method = MethodsVanillaTileEntities.class.getMethod("getRemainingBurnTime", TileEntityFurnace.class);
		assertTrue(PlethoraMethodRegistry.add(method));

		TileEntityFurnace furnace = new TileEntityFurnace();
		ILuaObject object = ContextFactory
			.of(furnace, id(furnace))
			.withExecutor(BasicExecutor.INSTANCE)
			.getObject();

		List<String> methods = Arrays.asList(object.getMethodNames());
		assertThat(methods, CoreMatchers.hasItem("getRemainingBurnTime"));

		assertArrayEquals(new Object[]{0}, object.callMethod(new BasicObject(), methods.indexOf("getRemainingBurnTime"), new Object[0]));
	}

	@Test
	public void testAddSync() throws NoSuchMethodException, LuaException, InterruptedException {
		Method method = MethodsVanillaTileEntities.class.getMethod("getSignText", TileEntitySign.class);
		assertTrue(PlethoraMethodRegistry.add(method));

		TileEntitySign furnace = new TileEntitySign();
		ILuaObject object = ContextFactory
			.of(furnace, id(furnace))
			.withExecutor(BasicExecutor.INSTANCE)
			.getObject();

		List<String> methods = Arrays.asList(object.getMethodNames());
		assertThat(methods, CoreMatchers.hasItem("getSignText"));

		Map<Object, Object> result = new HashMap<>();
		for (int i = 1; i <= 4; i++) result.put(i, "");
		assertArrayEquals(new Object[]{result}, object.callMethod(new BasicObject(), methods.indexOf("getSignText"), new Object[0]));
	}

	private static class BasicObject implements ILuaContext {

		@Nonnull
		@Override
		public Object[] pullEvent(@Nullable String s) throws InterruptedException {
			throw new InterruptedException("Cannot yield");
		}

		@Nonnull
		@Override
		public Object[] pullEventRaw(@Nullable String s) throws InterruptedException {
			throw new InterruptedException("Cannot yield");
		}

		@Nonnull
		@Override
		public Object[] yield(@Nullable Object[] objects) throws InterruptedException {
			throw new InterruptedException("Cannot yield");
		}

		@Nullable
		@Override
		public Object[] executeMainThreadTask(@Nonnull ILuaTask task) throws LuaException {
			return task.execute();
		}

		@Override
		public long issueMainThreadTask(@Nonnull ILuaTask task) throws LuaException {
			throw new LuaException("Cannot issue task");
		}
	}
}
