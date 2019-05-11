package org.squiddev.plethora.core.docdump;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.io.ByteStreams;
import joptsimple.internal.Strings;
import net.minecraft.util.math.BlockPos;
import org.squiddev.plethora.api.WorldLocation;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.api.meta.TypedMeta;
import org.squiddev.plethora.api.method.ContextKeys;
import org.squiddev.plethora.api.method.IMethod;
import org.squiddev.plethora.api.module.BasicModuleContainer;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.core.PartialContext;
import org.squiddev.plethora.core.capabilities.DefaultCostHandler;
import org.squiddev.plethora.core.collections.ClassIteratorIterable;
import org.squiddev.plethora.core.collections.SortedMultimap;
import org.squiddev.plethora.utils.WorldDummy;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class HTMLWriter implements IDocWriter {
	private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("#.#######");
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss yyyy-MM-dd");

	private final PrintStream writer;
	private final ObjectWriter objectWriter;

	private final Map<String, Object> escapeMap = new HashMap<>();

	private final ListMultimap<String, DocumentedMethod> methodLookup = MultimapBuilder.treeKeys().arrayListValues().build();
	private final ListMultimap<String, DocumentedMethod> moduleMethodLookup = MultimapBuilder.treeKeys().arrayListValues().build();
	private final ListMultimap<String, DocumentedMetaProvider> metaLookup = MultimapBuilder.treeKeys().arrayListValues().build();

	public HTMLWriter(
		OutputStream stream,
		Multimap<Class<?>, IMethod<?>> methodLookup,
		SortedMultimap<Class<?>, IMetaProvider<?>> metaProviders
	) {
		PrintStream writer;
		try {
			writer = new PrintStream(stream, false, StandardCharsets.UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			writer = new PrintStream(stream);
		}
		this.writer = writer;
		objectWriter = new HtmlObjectWriter(writer);

		for (Map.Entry<Class<?>, IMethod<?>> entry : methodLookup.entries()) {
			IMethod<?> method = entry.getValue();
			DocumentedMethod data = new DocumentedMethod(entry.getKey(), method);

			if (data.getModules().isEmpty() || !data.getTarget().isAssignableFrom(IModuleContainer.class)) {
				this.methodLookup.put(data.getTarget().getName(), data);
			} else {
				moduleMethodLookup.put(Strings.join(data.getModules(), ", "), data);
			}
		}


		for (Map.Entry<Class<?>, Collection<IMetaProvider<?>>> entries : metaProviders.items().entrySet()) {
			String klass = entries.getKey().getName();
			for (IMetaProvider<?> provider : entries.getValue()) {
				metaLookup.put(klass, new DocumentedMetaProvider(entries.getKey(), provider));
			}
		}

	}

	@Override
	public void writeHeader() throws IOException {
		try (InputStream header = getClass().getClassLoader().getResourceAsStream("assets/plethora/header.html")) {
			if (header == null) {
				writer.println("<html><body>");
			} else {
				ByteStreams.copy(header, writer);
			}
		}
	}

	@Override
	public void writeFooter() throws IOException {
		writer.printf("<footer>Generated on %s</footer>\n", DATE_FORMAT.format(new Date()));
		try (InputStream footer = getClass().getClassLoader().getResourceAsStream("assets/plethora/footer.html")) {
			if (footer == null) {
				writer.println("</body></html>");
			} else {
				ByteStreams.copy(footer, writer);
			}
		}
	}

	@Override
	public void write() throws IOException {
		if (!moduleMethodLookup.isEmpty()) writeGroupHeader("Module methods", moduleMethodLookup);
		if (!methodLookup.isEmpty()) writeGroupHeader("Targeted methods", methodLookup);
		if (!metaLookup.isEmpty()) writeGroupHeader("Metadata providers", metaLookup);

		if (!moduleMethodLookup.isEmpty()) {
			writeGroupElements("Module methods", moduleMethodLookup, this::writeMethodTarget);
		}
		if (!methodLookup.isEmpty()) writeGroupElements("Targeted methods", methodLookup, this::writeMethodTarget);
		if (!metaLookup.isEmpty()) writeGroupElements("Metadata providers", metaLookup, this::writeMetaTarget);
	}

	private <T extends DocumentedItem<?>> void writeGroupHeader(String title, ListMultimap<String, T> lookup) {
		String groupId = ident(title.toLowerCase());
		writer.printf("<h2 class=\"group-name\">%s</h2>\n", title);

		// Emit a link to all children
		writer.println("<ul>");
		for (String target : lookup.keySet()) writer.printf("<li>%s</li>\n", linkToTarget(groupId, target));
		writer.println("</ul>");
	}

	private <T extends DocumentedItem<?>> void writeGroupElements(String title, ListMultimap<String, T> lookup, DocEmitter<List<T>> emitter) throws IOException {
		String groupId = ident(title.toLowerCase());
		writer.printf("<h2 id=\"%s\" class=\"group-name\">%s</h2>\n", groupId, title);
		writer.println("<div class=\"group-data\">");

		// Emit each child
		for (String target : lookup.keySet()) emitter.emit(groupId, target, lookup.get(target));

		writer.println("</div>");
	}

	private void writeMethodTarget(String group, String target, List<DocumentedMethod> docs) {
		writer.printf("<h3 id=\"%s-%s\" class=\"target-name\" >%s</h3>\n", group, ident(target), target);
		writer.println("<div class=\"target-data\">");

		// Write a summary table
		writer.println("<table class=\"doc-list\">");
		writer.println("<tr><th>Function</th><th>Synopsis</th></tr>");
		Collections.sort(docs);
		for (DocumentedMethod doc : docs) {
			writer.printf("<tr><td>%s<td>%s</td></tr>\n", linkToItem(doc), empty(doc.getSynopsis()));
		}
		writer.println("</table>");

		// Emit the methods themselves
		for (DocumentedMethod data : docs) writeMethod(data);

		writer.println("</div>");
	}

	private void writeMethod(DocumentedMethod doc) {
		writer.println("<div class=\"doc-item\">");
		writer.printf("<h4 id=\"%s\" class=\"doc-name\">%s</h4>\n", uniqueIdent(doc), doc.getFriendlyName() + doc.getArgs());

		if (doc.getSynopsis() != null) writer.printf("<p class=\"synopsis\">%s</p>\n", doc.getSynopsis());
		if (doc.getDetail() != null) writer.printf("<p>%s</p>\n", doc.getDetail().replace("\n", "</p>\n<p>"));

		writer.println("<table class=\"doc-details\">");
		writer.printf("<tr><td>Class</td><td><code>%s</code></td></tr>\n", doc.getId());
		writer.printf("<tr><td>Target</td><td><code>%s</code></td></tr>\n", doc.getTarget().getName());

		if (doc.getSubtarget() != null) {
			writer.printf("<tr><td>Sub-target</td><td><code>%s</code></td></tr>\n", doc.getSubtarget().getName());
		}

		if (!doc.getModules().isEmpty()) {
			writer.print("<tr><td>Modules</td><td>");
			for (String module : doc.getModules()) {
				writer.printf("<code>%s</code> ", module);
			}
			writer.print("</td></tr>\n");
		}

		writer.println("</table>");
		writer.println("</div>");
	}

	private void writeMetaTarget(String group, String target, List<DocumentedMetaProvider> docs) throws IOException {
		writer.printf("<h3 id=\"%s-%s\" class=\"target-name\" >%s</h3>\n", group, ident(target), target);
		writer.println("<div class=\"target-data\">");

		{
			// Find "parent" classes and link to their meta providers
			Class<?> targetClass = docs.get(0).getTarget();
			List<String> parents = new ArrayList<>();
			for (Class<?> superclass : new ClassIteratorIterable(targetClass)) {
				if (superclass != targetClass && !metaLookup.get(superclass.getName()).isEmpty()) {
					parents.add(superclass.getName());
				}
			}

			if (!parents.isEmpty()) {
				parents.sort(Comparator.comparing(HTMLWriter::shortName));
				writer.print("<p>Also includes metadata from ");
				for (int i = 0; i < parents.size(); i++) {
					if (i > 0) writer.print(", ");
					writer.print(linkToShortTarget(group, parents.get(i)));
				}
				writer.print("</p>");
			}
		}

		for (DocumentedMetaProvider doc : docs) writeMeta(doc);

		writer.println("</div>");
	}

	@SuppressWarnings("unchecked")
	private void writeMeta(DocumentedMetaProvider doc) throws IOException {
		writer.println("<div class=\"doc-item\">");
		writer.printf("<h4 id=\"%s\" class=\"doc-name\">%s</h4>\n", uniqueIdent(doc), doc.getFriendlyName());

		if (doc.getSynopsis() != null) writer.printf("<p class=\"synopsis\">%s</p>\n", doc.getSynopsis());
		if (doc.getDetail() != null) writer.printf("<p>%s</p>\n", doc.getDetail().replace("\n", "</p>\n<p>"));

		IMetaProvider<?> provider = doc.getObject();
		Object example = provider.getExample();
		if (example != null) {
			Map<?, ?> meta = provider.getMeta(new PartialContext(1,
				new String[]{ContextKeys.ORIGIN, ContextKeys.TARGET},
				new Object[]{new WorldLocation(WorldDummy.INSTANCE, BlockPos.ORIGIN), example},
				DefaultCostHandler.EMPTY,
				BasicModuleContainer.EMPTY
			));

			if (!meta.isEmpty()) {
				writer.println("<pre class=\"highlight\">");
				objectWriter.write(meta);
				writer.println("</pre>");
			}
		}

		writer.println("</div>");
	}

	/**
	 * Get a unique identifier for this doc data
	 *
	 * @param data The doc data to identify
	 * @return The unique identifier
	 */
	private String uniqueIdent(DocumentedItem<?> data) {
		String name = ident(data.getId());

		// We start at 0 and increment a counter until we find the object or find a name which hasn't been
		// used
		for (int id = 0; ; id++) {
			String fullName = id == 0 ? name : name + "-" + id;
			Object existing = escapeMap.get(fullName);

			if (existing == data.getObject()) return fullName;

			if (existing == null) {
				escapeMap.put(fullName, data.getObject());
				return fullName;
			}
		}
	}

	private String linkToItem(DocumentedItem<?> data) {
		return String.format("<a href=\"#%s\" class=\"doc-name\">%s</a>", uniqueIdent(data), data.getFriendlyName());
	}

	private static String linkToTarget(String group, String name) {
		return String.format("<a href=\"#%s-%s\" class=\"target-name\">%s</a>", group, ident(name), name);
	}

	private static String linkToShortTarget(String group, String name) {
		return String.format("<a href=\"#%s-%s\" class=\"target-name\">%s</a>", group, ident(name), shortName(name));
	}

	private static String ident(String value) {
		return value.replaceAll("[^0-9a-zA-Z$-_.+!*'()]+", "-");
	}

	private static String empty(String x) {
		return x == null ? "" : x;
	}

	private static String shortName(String name) {
		int index = name.lastIndexOf('.');
		return index >= 0 ? name.substring(index + 1) : name;
	}

	@Override
	public void close() {
		writer.close();
	}

	@FunctionalInterface
	private interface DocEmitter<T> {
		void emit(String groupId, String target, T object) throws IOException;
	}

	private static final class HtmlObjectWriter extends ObjectWriter {
		HtmlObjectWriter(Appendable stream) {
			super(stream);
		}

		@Override
		public void writeValue(boolean value) throws IOException {
			classed("kc", value ? "true" : "false");
		}

		@Override
		public void writeValue(Void value) throws IOException {
			classed("kc", "nil");
		}

		@Override
		public void writeValue(String value) throws IOException {
			StringBuilder builder = new StringBuilder(2 + value.length());
			builder.append('"');
			for (int i = 0; i < value.length(); i++) {
				char c = value.charAt(i);
				if (c == '\n') {
					builder.append("\\n");
				} else if (c == '\t') {
					builder.append("\\t");
				} else if (c < ' ' || c > '~') {
					builder.append("\\").append((int) c);
				} else {
					builder.append(c);
				}
			}

			classed("s2", builder.append('"').toString());
		}

		@Override
		public void writeSpecial(String value) throws IOException {
			classed("o", "&laquo;" + value + "&raquo;");
		}

		@Override
		protected void writeValue(Map<?, ?> value, String indent) throws IOException {
			if (value.isEmpty()) {
				output.append("{}");
				return;
			}

			output
				.append("<span class=\"meta-map\">")
				.append("<span class=\"meta-brace\">{</span>");

			writeMapBody(value, indent);

			output
				.append("<span class=\"meta-brace\">}</span>")
				.append("</span>");
		}

		@Override
		protected void writeMeta(TypedMeta<?, ?> meta, String indent) throws IOException {
			if (meta.isEmpty()) {
				output.append("{}");
				return;
			}

			// Generally not needed, as the top level won't be a meta object. But useful none-the-less.
			if (indent.isEmpty()) {
				writeValue(meta, indent);
				return;
			}

			output
				.append("<span class=\"nested-meta meta-map\">")
				.append("<span class=\"meta-brace\">{</span>");

			output.append("<span class=\"nested-meta-short\"> ");
			writeSpecial("nested metadata");
			output.append(" </span>");

			output.append("<span class=\"nested-meta-long\">");
			writeMapBody(meta, indent);
			output.append("</span>");

			output
				.append("<span class=\"meta-brace\">}</span>")
				.append("</span>");
		}

		private void classed(String css, String value) throws IOException {
			output.append(String.format("<span class=\"%s\">%s</span>", css, value));
		}
	}
}
