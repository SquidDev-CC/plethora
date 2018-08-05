package org.squiddev.plethora.core.docdump;

import com.google.common.base.Strings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DocData<T> implements Comparable<MethodData> {
	private static final char[] terminators = new char[]{'!', '.', '?', '\r', '\n'};

	@Nonnull
	public final T value;

	/**
	 * The unique identifier for this value
	 */
	@Nonnull
	public final String id;

	/**
	 * The "friendly" display name for this object
	 */
	@Nonnull
	public final String name;

	/**
	 * A brief summary of the object
	 */
	@Nullable
	public final String synopsis;

	/**
	 * The remaining description after the synopsis
	 */
	@Nullable
	public final String detail;

	public DocData(@Nonnull T value, @Nonnull String id, @Nonnull String name, @Nullable String doc) {
		this.value = value;
		this.id = id;
		this.name = name;

		if (doc != null) {
			String synopsis, detail = null;

			// Get minimum position
			int position = -1;
			for (char chr : terminators) {
				int newPos = doc.indexOf(chr);
				if (position == -1 || (newPos > -1 && newPos < position)) position = newPos;
			}

			if (position > -1) {
				synopsis = doc.substring(0, position).trim();
				detail = doc.substring(position + 1).trim();
			} else if (doc.length() > 80) {
				synopsis = doc.substring(0, 77).trim() + "...";
				detail = doc;
			} else {
				synopsis = doc;
			}

			this.synopsis = Strings.isNullOrEmpty(synopsis) ? null : synopsis;
			this.detail = Strings.isNullOrEmpty(detail) ? null : detail;

		} else {
			this.synopsis = null;
			this.detail = null;
		}
	}

	@Override
	public int compareTo(@Nonnull MethodData o) {
		return name.compareTo(o.name);
	}
}
