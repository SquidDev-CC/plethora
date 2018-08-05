package org.squiddev.plethora.core.docdump;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.io.ByteStreams;
import joptsimple.internal.Strings;
import org.squiddev.plethora.api.method.IMethod;
import org.squiddev.plethora.api.method.ISubTargetedMethod;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class HTMLWriter implements IDocWriter {
	private static final DateFormat format = new SimpleDateFormat("HH:mm:ss yyyy-MM-dd");

	private final boolean template;
	private final PrintStream writer;

	private Map<String, Object> escapeMap = new HashMap<>();

	public HTMLWriter(boolean template, OutputStream stream) {
		this.template = template;
		this.writer = new PrintStream(stream);
	}

	@Override
	public void writeHeader() throws IOException {
		if (template) {
			InputStream header = getClass().getClassLoader().getResourceAsStream("assets/plethora/header.html");
			if (header == null) {
				writer.println("<html><body>");
			} else {
				try {
					ByteStreams.copy(header, writer);
				} finally {
					header.close();
				}
			}
		}
	}

	@Override
	public void writeFooter() {
		if (template) {
			writer.printf("<footer>Generated on %s</footer>\n", format.format(new Date()));
			writer.println("</body></html>");
		}
	}

	@Override
	public void write(Multimap<Class<?>, IMethod<?>> methodLookup) {
		ListMultimap<String, MethodData> classLookup = MultimapBuilder.treeKeys().arrayListValues().build();
		ListMultimap<String, MethodData> moduleLookup = MultimapBuilder.treeKeys().arrayListValues().build();

		for (Map.Entry<Class<?>, IMethod<?>> entry : methodLookup.entries()) {
			IMethod<?> method = entry.getValue();
			Class<?> klass = entry.getKey();
			MethodData data = new MethodData(klass, method);

			if (method instanceof ISubTargetedMethod<?, ?>) {
				klass = ((ISubTargetedMethod<?, ?>) method).getSubTarget();
			}

			if (data.modules != null) {
				moduleLookup.put(Strings.join(data.modules, ", "), data);
			} else {
				classLookup.put(klass.getName(), data);
			}
		}

		if (moduleLookup.size() > 0) writeGroupHeader("Module methods", moduleLookup);
		if (classLookup.size() > 0) writeGroupHeader("Targeted methods", classLookup);

		if (moduleLookup.size() > 0) writeGroupElements("Module methods", moduleLookup, this::writeMethodTarget);
		if (classLookup.size() > 0) writeGroupElements("Targeted methods", classLookup, this::writeMethodTarget);
	}

	private <T extends DocData<?>> void writeGroupHeader(String title, ListMultimap<String, T> lookup) {
		String groupId = ident(title.toLowerCase());
		writer.printf("<h2 id=\"%s\" class=\"group-name\">%s</h2>\n", groupId, title);

		// Emit a link to all children
		writer.println("<ul>");
		for (String target : lookup.keySet()) {
			writer.printf("<li><a href=\"#%s-%s\" class=\"target-name\">%s</a></li>\n", groupId, ident(target), target);
		}
		writer.println("</ul>");
	}

	private <T extends DocData<?>> void writeGroupElements(String title, ListMultimap<String, T> lookup, DocEmitter<List<T>> emitter) {
		String groupId = ident(title.toLowerCase());
		writer.println("<div class=\"group-data\">");

		// Emit each child
		for (String target : lookup.keySet()) emitter.emit(groupId, target, lookup.get(target));

		writer.println("</div>");
	}

	private void writeMethodTarget(String groupId, String target, List<MethodData> methodData) {
		writer.printf("<h3 id=\"%s-%s\" class=\"target-name\" >%s</h3>\n", groupId, ident(target), target);

		writer.println("<div class=\"target-data\">");
		writer.println("<table class=\"doc-list\">");
		writer.println("<tr><th>Function</th><th>Synopsis</th></tr>");

		Collections.sort(methodData);
		for (MethodData data : methodData) {
			writer.printf("<tr><td><a href=\"#%s\" class=\"doc-name\">%s</a></td><td>%s</td></tr>\n",
				uniqueIdent(data), data.name, empty(data.synopsis));
		}
		writer.println("</table>");

		for (MethodData data : methodData) writeMethod(data);

		writer.println("</div>");
	}

	private void writeMethod(MethodData data) {
		writer.println("<div class=\"doc-item\">");

		writer.printf("<h4 id=\"%s\" class=\"doc-name\">%s</h4>\n", uniqueIdent(data), data.name + empty(data.args));

		if (data.synopsis != null) writer.printf("<p class=\"synopsis\">%s</p>\n", data.synopsis);
		if (data.detail != null) writer.printf("<p>%s</p>", data.detail.replace("\n", "</p>\n<p>"));

		writer.println("<table class=\"doc-details\">");
		writer.printf("<tr><td>Class</td><td><code>%s</code></td></tr>\n", data.value.getClass().getName());
		writer.printf("<tr><td>Target</td><td><code>%s</code></td></tr>\n", data.target);

		if (data.subtarget != null) {
			writer.printf("<tr><td>Sub-target</td><td><code>%s</code></td></tr>\n", data.subtarget);
		}

		if (data.modules != null) {
			writer.print("<tr><td>Modules</td><td>");
			for (String module : data.modules) {
				writer.printf("<code>%s</code> ", module);
			}
			writer.print("</td></tr>\n");
		}

		writer.println("</table>");
		writer.println("</div>");
	}

	/**
	 * Get a unique identifier for this doc data
	 *
	 * @param data The doc data to identify
	 * @return The unique identifier
	 */
	private String uniqueIdent(DocData<?> data) {
		String name = ident(data.id);

		// We start at 0 and increment a counter until we find the object or find a name which hasn't been
		// used
		for (int id = 0; ; id++) {
			String fullName = id == 0 ? name : name + "-" + id;
			Object existing = escapeMap.get(fullName);

			if (existing == data.value) return fullName;

			if (existing == null) {
				escapeMap.put(fullName, data.value);
				return fullName;
			}
		}
	}

	private static String ident(String value) {
		return value.replaceAll("[^0-9a-zA-Z$-_.+!*'()]+", "-");
	}

	private static String empty(String x) {
		return x == null ? "" : x;
	}

	@Override
	public void close() {
		writer.close();
	}

	private interface DocEmitter<T> {
		void emit(String groupId, String target, T object);
	}
}
