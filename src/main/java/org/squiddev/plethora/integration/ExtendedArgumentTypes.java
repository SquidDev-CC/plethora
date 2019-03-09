package org.squiddev.plethora.integration;

import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.method.gen.ArgumentType;

@Injects
public final class ExtendedArgumentTypes {
	public static final ArgumentType<ItemFingerprint> FINGERPRINT = ItemFingerprint::fromLua;
}
