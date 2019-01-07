package org.squiddev.plethora.utils;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HelpersTest {
	@Test
	public void blacklisted() {
		// Packages
		assertBlocked("pkg", "pkg.Class");
		assertBlocked("pkg.", "pkg.Class");

		assertAllowed("pkg", "pkgs.Class");
		assertAllowed("pkg.", "pkgs.Class");

		// Classes
		assertBlocked("pkg.Class", "pkg.Class");
		assertBlocked("pkg.Class", "pkg.Class$Child");
		assertBlocked("pkg.Class$", "pkg.Class$Child");
		assertBlocked("pkg.Class$Child", "pkg.Class$Child");

		assertAllowed("pkg.Class", "pkg.ClassX");

		// Methods
		assertBlocked("pkg.Class", "pkg.Class#method(L;args)V");
		assertBlocked("pkg.Class#", "pkg.Class#method(L;args)V");
		assertBlocked("pkg.Class#method", "pkg.Class#method(L;args)V");
		assertBlocked("pkg.Class#method(L;args)V", "pkg.Class#method(L;args)V");

		assertAllowed("pkg.Class#method", "pkg.Class#methods");
	}

	private void assertBlocked(String prefix, String name) {
		assertTrue(
			String.format("Expected '%s' to match prefix '%s'", name, prefix),
			Helpers.blacklisted(Collections.singleton(prefix), name)
		);
	}

	private void assertAllowed(String prefix, String name) {
		assertFalse(
			String.format("Expected '%s' to not match prefix '%s'", name, prefix),
			Helpers.blacklisted(Collections.singleton(prefix), name)
		);
	}
}
