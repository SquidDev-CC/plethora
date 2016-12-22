package org.squiddev.plethora.core.docdump;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.squiddev.plethora.api.method.IMethod;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;

public class JSONWriter implements IDocWriter {
	private static final Gson gson = new GsonBuilder().create();

	private final PrintStream writer;

	public JSONWriter(OutputStream stream) {
		this.writer = new PrintStream(stream);
	}

	@Override
	public void writeHeader() throws IOException {

	}

	@Override
	public void writeFooter() throws IOException {

	}

	@Override
	public void write(Multimap<Class<?>, IMethod<?>> methodLookup) {
		List<DocData> data = Lists.newArrayListWithExpectedSize(methodLookup.size());

		for (Map.Entry<Class<?>, IMethod<?>> entry : methodLookup.entries()) {
			data.add(new DocData(entry.getKey(), entry.getValue()));
		}

		gson.toJson(data, writer);
	}
}
