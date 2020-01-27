package org.squiddev.plethora.core.docdump;

import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.google.gson.stream.JsonWriter;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.core.RegisteredMethod;
import org.squiddev.plethora.core.collections.SortedMultimap;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class JSONWriter implements IDocWriter {
	private static final Gson gson = new GsonBuilder()
		.registerTypeAdapter(Class.class, (JsonSerializer<Class<?>>) (src, typeOfSrc, context) -> new JsonPrimitive(src.getName()))
		.create();

	private final JsonWriter writer;
	private final List<DocumentedMethod> methodData;
	private final List<DocumentedMetaProvider> metaData;

	public JSONWriter(
		OutputStream stream,
		Multimap<Class<?>, RegisteredMethod<?>> methods,
		SortedMultimap<Class<?>, IMetaProvider<?>> metaProviders
	) {
		writer = new JsonWriter(new OutputStreamWriter(stream, StandardCharsets.UTF_8));

		List<DocumentedMethod> methodData = this.methodData = new ArrayList<>(methods.size());
		for (RegisteredMethod<?> method : methods.values()) {
			methodData.add(new DocumentedMethod(method));
		}

		List<DocumentedMetaProvider> data = metaData = new ArrayList<>();
		for (Map.Entry<Class<?>, Collection<IMetaProvider<?>>> entry : metaProviders.items().entrySet()) {
			for (IMetaProvider<?> provider : entry.getValue()) {
				data.add(new DocumentedMetaProvider(entry.getKey(), provider));
			}
		}
	}

	@Override
	public void write() throws IOException {
		writer.beginObject();

		writer.name("methods");
		gson.toJson(methodData, List.class, writer);
		writer.name("meta");
		gson.toJson(metaData, List.class, writer);

		writer.endObject();
	}

	@Override
	public void close() throws IOException {
		writer.close();
	}
}
