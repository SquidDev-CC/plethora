package org.squiddev.plethora.gameplay.modules.glasses;

/**
 * Represents a holder for {@link BaseObject}s.
 */
public interface ObjectGroup {
	/**
	 * @return The ID for this group.
	 */
	int id();

	/**
	 * A group for 2D objects
	 */
	interface Group2D extends ObjectGroup {
	}
}
