package org.squiddev.plethora.api.module;

import net.minecraft.nbt.NBTTagCompound;
import org.squiddev.plethora.api.IWorldLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A container for modules, allowing interaction with the outside world
 */
public interface IModuleAccess {
	/**
	 * The owner of this module container. This is probably a {@link net.minecraft.tileentity.TileEntity} or
	 * {@link net.minecraft.entity.EntityLivingBase}.
	 *
	 * @return The module's owner. This is constant for the lifetime of the module access.
	 */
	@Nonnull
	Object getOwner();

	/**
	 * Get the position of this owner
	 *
	 * @return The owners' position
	 */
	@Nonnull
	IWorldLocation getLocation();

	/**
	 * Get a view of all modules in this container
	 *
	 * @return All modules in this container
	 */
	@Nonnull
	IModuleContainer getContainer();

	/**
	 * Get data specific to this module. This can be written
	 * to and persisted and shared with the client. It is not bound
	 * to the item stack however and is discarded when the module is removed.
	 *
	 * Do note, this data may be shared across multiple accesses or instances of this module.
	 *
	 * If you change this data, you should mark it as dirty with {@link ]#markDataDirty()}.
	 *
	 * @return The module specific data
	 * @see #markDataDirty()
	 */
	@Nonnull
	NBTTagCompound getData();

	/**
	 * Mark the module specific data as dirty
	 *
	 * @see #getData()
	 */
	void markDataDirty();

	/**
	 * Queue an event on everything listening
	 *
	 * @param event The event name to queue
	 * @param args  The arguments to this event
	 * @see dan200.computercraft.api.peripheral.IComputerAccess#queueEvent(String, Object[])
	 */
	void queueEvent(@Nonnull String event, @Nullable Object... args);
}
