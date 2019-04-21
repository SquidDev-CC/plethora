package org.squiddev.plethora.api.method;

import dan200.computercraft.api.lua.ILuaObject;

/**
 * A {@link ILuaObject} which targets a specific type.
 *
 * @param <T> The type this object targets
 * @see IContext#getObject()
 */
public interface TypedLuaObject<T> extends ILuaObject {
}
