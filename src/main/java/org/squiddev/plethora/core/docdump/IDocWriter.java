package org.squiddev.plethora.core.docdump;

import com.google.common.collect.Multimap;
import org.squiddev.plethora.api.method.IMethod;

import java.io.IOException;

/**
 * An output format for documentation
 */
public interface IDocWriter {
	void writeHeader() throws IOException;

	void writeFooter() throws IOException;

	void write(Multimap<Class<?>, IMethod<?>> methodLookup);
}
