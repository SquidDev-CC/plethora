package org.squiddev.plethora.core;

import org.squiddev.plethora.utils.Helpers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * A node which can be registered and enabled and disabled at runtime.
 */
public class RegisteredValue {
	@Nonnull
	private final String name;

	@Nullable
	private final String mod;

	RegisteredValue(@Nonnull String name, @Nullable String mod) {
		this.name = Objects.requireNonNull(name);
		this.mod = mod;
	}

	@Nonnull
	public final String name() {
		return name;
	}

	@Nullable
	public final String mod() {
		return mod;
	}

	public final boolean enabled() {
		return !Helpers.blacklisted(ConfigCore.Blacklist.blacklistProviders, name)
			&& (mod == null || !Helpers.modBlacklisted(mod));
	}
}
