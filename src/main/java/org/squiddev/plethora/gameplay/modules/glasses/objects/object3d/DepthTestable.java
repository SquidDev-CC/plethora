package org.squiddev.plethora.gameplay.modules.glasses.objects.object3d;

import org.squiddev.plethora.api.method.gen.FromTarget;
import org.squiddev.plethora.api.method.gen.PlethoraMethod;

public interface DepthTestable {
	boolean hasDepthTest();

	void setDepthTest(boolean depthTest);

	@PlethoraMethod(doc = "-- Determine whether depth testing is enabled for this object.", worldThread = false)
	static boolean isDepthTested(@FromTarget DepthTestable object) {
		return object.hasDepthTest();
	}

	@PlethoraMethod(doc = "-- Set whether depth testing is enabled for this object.", worldThread = false)
	static void setDepthTested(@FromTarget DepthTestable object, boolean depthTest) {
		object.setDepthTest(depthTest);
	}
}
