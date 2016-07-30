package org.squiddev.plethora.core.docdump;

import com.google.common.base.Strings;
import org.squiddev.plethora.api.method.IMethod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Details about a method
 */
public class DocData implements Comparable<DocData> {
	private static final Pattern docString = Pattern.compile("^function(\\([^)]*\\).*?)--(.*)$");
	private static final char[] terminators = new char[]{'!', '.', '?', '\r', '\n'};


	@Nonnull
	public final Class<?> target;

	@Nonnull
	public final IMethod<?> method;

	@Nonnull
	public final String name;

	@Nullable
	public final String synopsis;

	@Nullable
	public final String detail;

	@Nullable
	public final String args;

	public DocData(@Nonnull Class<?> target, @Nonnull IMethod<?> method) {
		this.target = target;
		this.method = method;
		this.name = method.getName();

		String doc = method.getDocString();
		if (doc != null) {
			String synopsis, detail = null, args = null;
			Matcher match = docString.matcher(doc);
			if (match.find()) {
				args = match.group(1).trim();
				doc = match.group(2);
			}

			// Get minimum position
			int position = -1;
			for (char chr : terminators) {
				int newPos = doc.indexOf(chr);
				if (position == -1 || (newPos > -1 && newPos < position)) position = newPos;
			}

			if (position > -1) {
				synopsis = doc.substring(0, position).trim();
				detail = doc.substring(position + 1).trim();
			} else if (doc.length() > 50) {
				synopsis = doc.substring(0, 47).trim() + "...";
				detail = doc;
			} else {
				synopsis = doc;
			}

			this.synopsis = Strings.isNullOrEmpty(synopsis) ? null : synopsis;
			this.args = Strings.isNullOrEmpty(args) ? null : args;
			this.detail = Strings.isNullOrEmpty(detail) ? null : detail;

		} else {
			this.synopsis = null;
			this.detail = null;
			this.args = null;
		}
	}

	@Override
	public int compareTo(@Nonnull DocData o) {
		return name.compareTo(o.name);
	}
}
