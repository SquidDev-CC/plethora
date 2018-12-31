package org.squiddev.plethora.core.docdump;

import java.io.Closeable;
import java.io.IOException;

/**
 * An output format for documentation
 */
public interface IDocWriter extends Closeable {
	default void writeHeader() throws IOException {
	}

	default void writeFooter() {
	}

	void write() throws IOException;
}
