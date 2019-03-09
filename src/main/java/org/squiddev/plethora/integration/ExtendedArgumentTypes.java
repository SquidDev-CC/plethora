package org.squiddev.plethora.integration;

import dan200.computercraft.api.lua.LuaException;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.method.gen.ArgumentType;

import javax.annotation.Nonnull;

@Injects
public final class ExtendedArgumentTypes {
	public static final ArgumentType<ItemFingerprint> FINGERPRINT = new ArgumentType<ItemFingerprint>() {
		@Override
		public String name() {
			return "string|table";
		}

		@Nonnull
		@Override
		public ItemFingerprint get(@Nonnull Object[] args, int index) throws LuaException {
			return ItemFingerprint.fromLua(args, index);
		}
	};
}
