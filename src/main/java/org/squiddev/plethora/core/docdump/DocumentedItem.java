package org.squiddev.plethora.core.docdump;

import com.google.common.base.Strings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class DocumentedItem<T> implements Comparable<DocumentedItem<T>> {
	private static final char[] TERMINATORS = new char[]{ '!', '.', '?', '\r', '\n' };

	private final T object;
	private final String id;
	private final String name;
	private final String synopsis;
	private final String detail;

	DocumentedItem(@Nonnull T object, @Nonnull String id, @Nonnull String name, @Nullable String doc) {
		this.object = object;
		this.id = id;
		this.name = name;

		if (doc != null) {

			// Get minimum position
			int position = -1;
			for (char chr : TERMINATORS) {
				int newPos = doc.indexOf(chr);
				if (position == -1 || (newPos > -1 && newPos < position)) position = newPos;
			}

			String detail;
			String synopsis;
			if (position > -1) {
				synopsis = doc.substring(0, position + 1).trim();
				detail = doc.substring(position + 1).trim();
			} else if (doc.length() > 80) {
				synopsis = doc.substring(0, 77).trim() + "...";
				detail = doc;
			} else {
				synopsis = doc;
				detail = "";
			}

			this.synopsis = Strings.isNullOrEmpty(synopsis) ? null : synopsis;
			this.detail = Strings.isNullOrEmpty(detail) ? null : detail;

		} else {
			synopsis = null;
			detail = null;
		}
	}

	/**
	 * Get the root object which is being documented
	 *
	 * @return The object which is being documented
	 */
	@Nonnull
	public T getObject() {
		return object;
	}

	/**
	 * Get the identifier for this object. This is used as an ID to link to it.
	 *
	 * @return The identifier for this object
	 */
	@Nonnull
	public String getId() {
		return id;
	}

	/**
	 * Get the display or friendly name for this object. This is used in titles and links
	 * to it.
	 *
	 * @return The display name for this object.
	 */
	@Nonnull
	public String getFriendlyName() {
		return name;
	}

	/**
	 * Get a brief description about this object.
	 *
	 * @return A brief description, or {@code null} if none is available.
	 * @see #getDetail()
	 */
	@Nullable
	public String getSynopsis() {
		return synopsis;
	}

	/**
	 * Get the remaining description about this object.
	 *
	 * @return The remaining description, or {@code null} if none is available.
	 * @see #getSynopsis()
	 */
	@Nullable
	public String getDetail() {
		return detail;
	}

	@Override
	public int compareTo(@Nonnull DocumentedItem<T> o) {
		return name.compareTo(o.name);
	}
}
