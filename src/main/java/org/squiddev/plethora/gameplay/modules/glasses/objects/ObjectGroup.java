package org.squiddev.plethora.gameplay.modules.glasses.objects;

import org.squiddev.plethora.gameplay.modules.glasses.BaseObject;
import org.squiddev.plethora.gameplay.modules.glasses.CanvasHandler;

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

	/**
	 * A group for 2D objects with a fixed side
	 */
	interface Frame2D extends Group2D {
		default int getWidth() {
			return CanvasHandler.WIDTH;
		}

		default int getHeight() {
			return CanvasHandler.HEIGHT;
		}
	}

	/**
	 * A group for 3D objects
	 */
	interface Group3D extends ObjectGroup {
	}

	/**
	 * The "origin" for all 3D objects
	 */
	interface Origin3D extends ObjectGroup {
	}
}
