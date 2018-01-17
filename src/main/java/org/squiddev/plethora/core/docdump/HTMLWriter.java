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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class HTMLWriter implements IDocWriter {
	private static final DateFormat format = new SimpleDateFormat("HH:mm:ss yyyy-MM-dd");
	private final PrintStream writer;

	private int id = 0;

	public HTMLWriter(OutputStream stream) {
		this.writer = new PrintStream(stream);
	}

	@Override
	public void writeHeader() throws IOException {
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

	@Override
	public void writeFooter() {
		writer.printf("<footer>Generated on %s</footer>\n", format.format(new Date()));
		writer.println("</body></html>");
	}

	@Override
	public void write(Multimap<Class<?>, IMethod<?>> methodLookup) {
		ListMultimap<String, DocData> classLookup = MultimapBuilder.treeKeys().arrayListValues().build();
		ListMultimap<String, DocData> moduleLookup = MultimapBuilder.treeKeys().arrayListValues().build();

		for (Map.Entry<Class<?>, IMethod<?>> entry : methodLookup.entries()) {
			IMethod<?> method = entry.getValue();
			Class<?> klass = entry.getKey();
			DocData data = new DocData(klass, method);

			if (method instanceof ISubTargetedMethod<?, ?>) {
				klass = ((ISubTargetedMethod<?, ?>) method).getSubTarget();
			}

			if (data.modules != null) {
				moduleLookup.put(Strings.join(data.modules, ", "), data);
			} else {
				classLookup.put(klass.getName(), data);
			}
		}

		if (classLookup.size() > 0) {
			writeData("Targeted", classLookup);
		}

		if (moduleLookup.size() > 0) {
			writeData("Modules", moduleLookup);
		}
	}

	private void writeData(String title, ListMultimap<String, DocData> lookup) {
		writer.println("<div class=\"target\">");
		writer.printf("<h1>%s</h1>\n", title);

		{
			writer.println("<ul>");

			int id = this.id;
			for (String target : lookup.keySet()) {
				writer.printf("<li><a href=\"#%d_%s\">%s</a></li>\n", id++, target.replace('.', '_').replace('$', '_'), target);
			}

			writer.println("</ul>");
		}

		{
			int id = this.id;
			for (String target : lookup.keySet()) {
				writer.printf("<h3 id=\"%d_%s\">%s</h3>\n", id++, target.replace('.', '_').replace('$', '_'), target);

				writeMethods(id, lookup.get(target));
			}
		}

		writer.println("</div>");

		this.id += lookup.keySet().size();
	}

	private void writeMethod(int group, int index, DocData data) {
		writer.println("<div class=\"method\">");

		writer.printf("<h4 id=\"%d_%d_%s\">%s</h4>\n", group, index, data.name, data.name + empty(data.args));

		if (data.synopsis != null) writer.printf("<p><em class=\"method-synopsis\">%s</em></p>\n", data.synopsis);
		if (data.detail != null) writer.printf("<p>%s</p>", data.detail.replace("\n", "</p>\n<p>"));

		writer.println("<table class=\"method-details\">");
		writer.printf("<tr><td>Class</td><td><code>%s</code></td></tr>\n", data.method);
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

	private void writeMethods(int group, List<DocData> docData) {
		Collections.sort(docData);

		{
			int id = 0;
			writer.println("<table class=\"method-list\">");
			writer.println("<tr><th>Function</th><th>Synopsis</th></tr>");

			for (DocData data : docData) {
				writer.printf("<tr><td><a href=\"#%d_%d_%s\">%s</a></td><td>%s</td></tr>\n", group, id++, data.name, data.name, empty(data.synopsis));
			}
			writer.println("</table>");
		}

		{
			int id = 0;
			for (DocData data : docData) {
				writeMethod(group, id++, data);
			}
		}
	}

	private static String empty(String x) {
		return x == null ? "" : x;
	}
}
