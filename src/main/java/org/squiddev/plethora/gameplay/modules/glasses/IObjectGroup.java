package org.squiddev.plethora.gameplay.modules.glasses;

import javax.annotation.Nonnull;

/**
 * Represents a holder for {@link BaseObject}s.
 */
public interface IObjectGroup {
	/**
	 * Get the root canvas for this object.
	 *
	 * @return The root canvas
	 */
	CanvasServer root();

	/**
	 * @return The ID for this group.
	 */
	int id();

	/**
	 * Add a child to this group
	 *
	 * @param object The object to add. Its parent ID must be this one's
	 */
	void add(@Nonnull BaseObject object);

	/**
	 * Remove a child from this group
	 *
	 * @param object The object to from. Its parent ID must be this one's
	 */
	void remove(BaseObject object);

	/**
	 * Remove all child objects from this group.
	 */
	void clear();

	/**
	 * Determine whether this canvas still contains a given object.
	 *
	 * @param object The object to check
	 * @return Whether this canvas contains this object
	 */
	boolean contains(@Nonnull BaseObject object);
}
