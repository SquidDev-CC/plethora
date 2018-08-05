package org.squiddev.plethora.core.docdump;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;
import org.squiddev.plethora.api.method.IMethod;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;

public class JSONWriter implements IDocWriter {
	private static final Gson gson = new GsonBuilder().create();

	private final JsonWriter writer;

	public JSONWriter(OutputStream stream) {
		this.writer = new JsonWriter(new OutputStreamWriter(stream));
	}

	@Override
	public void writeHeader() throws IOException {
		writer.beginObject();
	}

	@Override
	public void writeFooter() throws IOException {
		writer.endObject();
	}

	@Override
	public void write(Multimap<Class<?>, IMethod<?>> methodLookup) throws IOException {
		List<MethodData> data = Lists.newArrayListWithExpectedSize(methodLookup.size());

		for (Map.Entry<Class<?>, IMethod<?>> entry : methodLookup.entries()) {
			data.add(new MethodData(entry.getKey(), entry.getValue()));
		}

		writer.name("methods");
		gson.toJson(data, List.class, writer);
	}

	@Override
	public void close() throws IOException {
		writer.close();
	}
}
