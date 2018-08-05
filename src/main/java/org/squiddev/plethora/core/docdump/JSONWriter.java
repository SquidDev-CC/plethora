package org.squiddev.plethora.core.docdump;

import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.api.method.IMethod;
import org.squiddev.plethora.core.collections.SortedMultimap;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class JSONWriter implements IDocWriter {
	private static final Gson gson = new GsonBuilder().create();

	private final JsonWriter writer;
	private final List<DocumentedMethod> methodData;
	private final List<DocumentedMetaProvider> metaData;

	public JSONWriter(
		OutputStream stream,
		Multimap<Class<?>, IMethod<?>> methods,
		SortedMultimap<Class<?>, IMetaProvider<?>> metaProviders
	) {
		this.writer = new JsonWriter(new OutputStreamWriter(stream));

		List<DocumentedMethod> methodData = this.methodData = new ArrayList<>(methods.size());
		for (Map.Entry<Class<?>, IMethod<?>> entry : methods.entries()) {
			methodData.add(new DocumentedMethod(entry.getKey(), entry.getValue()));
		}

		List<DocumentedMetaProvider> data = this.metaData = new ArrayList<>();
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
