/*
 * ****************************************************************************
 * Original Source: Copyright (c) 2009-2011 Luaj.org. All rights reserved.
 * Modifications: Copyright (c) 2015-2017 SquidDev
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * ****************************************************************************
 */
package org.squiddev.plethora.utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;

public class LuaPattern {
	@Nullable
	public static String[] match(@Nonnull String string, @Nonnull String pattern) {
		if (pattern.equals("")) return new String[0];

		MatchState ms = new MatchState(string, pattern);

		boolean anchor = false;
		int patternOffset = 0;
		if (pattern.charAt(0) == '^') {
			anchor = true;
			patternOffset = 1;
		}

		int stringOffset = 0;
		do {
			int res;
			ms.reset();
			if ((res = ms.match(stringOffset, patternOffset)) != -1) {
				return ms.getCaptures(stringOffset, res);
			}
		} while (stringOffset++ < string.length() && !anchor);
		return null;
	}

	public static boolean matches(@Nonnull String string, @Nonnull String pattern) {
		if (pattern.equals("")) return true;

		MatchState ms = new MatchState(string, pattern);

		boolean anchor = false;
		int patternOffset = 0;
		if (pattern.charAt(0) == '^') {
			anchor = true;
			patternOffset = 1;
		}

		int stringOffset = 0;
		do {
			ms.reset();
			if (ms.match(stringOffset, patternOffset) != -1) return true;
		} while (stringOffset++ < string.length() && !anchor);
		return false;
	}
	// Pattern matching implementation

	private static final int L_ESC = '%';
	private static final String SPECIALS = ("^$*+?.([%-");
	private static final int MAX_CAPTURES = 32;

	private static final int CAP_UNFINISHED = -1;
	private static final int CAP_POSITION = -2;

	private static final byte MASK_ALPHA = 0x01;
	private static final byte MASK_LOWERCASE = 0x02;
	private static final byte MASK_UPPERCASE = 0x04;
	private static final byte MASK_DIGIT = 0x08;
	private static final byte MASK_PUNCT = 0x10;
	private static final byte MASK_SPACE = 0x20;
	private static final byte MASK_CONTROL = 0x40;
	private static final byte MASK_HEXDIGIT = (byte) 0x80;

	private static final byte[] CHAR_TABLE;

	static {
		CHAR_TABLE = new byte[256];

		for (int i = 0; i < 256; ++i) {
			final char c = (char) i;
			CHAR_TABLE[i] = (byte) ((Character.isDigit(c) ? MASK_DIGIT : 0) |
				(Character.isLowerCase(c) ? MASK_LOWERCASE : 0) |
				(Character.isUpperCase(c) ? MASK_UPPERCASE : 0) |
				((c < ' ' || c == 0x7F) ? MASK_CONTROL : 0));
			if ((c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F') || (c >= '0' && c <= '9')) {
				CHAR_TABLE[i] |= MASK_HEXDIGIT;
			}
			if ((c >= '!' && c <= '/') || (c >= ':' && c <= '@')) {
				CHAR_TABLE[i] |= MASK_PUNCT;
			}
			if ((CHAR_TABLE[i] & (MASK_LOWERCASE | MASK_UPPERCASE)) != 0) {
				CHAR_TABLE[i] |= MASK_ALPHA;
			}
		}

		CHAR_TABLE[' '] = MASK_SPACE;
		CHAR_TABLE['\r'] |= MASK_SPACE;
		CHAR_TABLE['\n'] |= MASK_SPACE;
		CHAR_TABLE['\t'] |= MASK_SPACE;
		CHAR_TABLE[0x0B] |= MASK_SPACE; // \v
		CHAR_TABLE['\f'] |= MASK_SPACE;
	}

	private static boolean matchClass(int character, char matchClass) {
		final byte lcl = (byte) (Character.toLowerCase(matchClass) & 255);
		int cdata = CHAR_TABLE[character & 0xFF];

		boolean res;
		switch (lcl) {
			case 'a':
				res = (cdata & MASK_ALPHA) != 0;
				break;
			case 'd':
				res = (cdata & MASK_DIGIT) != 0;
				break;
			case 'l':
				res = (cdata & MASK_LOWERCASE) != 0;
				break;
			case 'u':
				res = (cdata & MASK_UPPERCASE) != 0;
				break;
			case 'c':
				res = (cdata & MASK_CONTROL) != 0;
				break;
			case 'p':
				res = (cdata & MASK_PUNCT) != 0;
				break;
			case 's':
				res = (cdata & MASK_SPACE) != 0;
				break;
			case 'w':
				res = (cdata & (MASK_ALPHA | MASK_DIGIT)) != 0;
				break;
			case 'x':
				res = (cdata & MASK_HEXDIGIT) != 0;
				break;
			case 'z':
				res = (character == 0);
				break;
			default:
				return matchClass == character;
		}
		return (lcl == matchClass) ? res : !res;
	}

	private static final class MatchState {
		private final String string;
		private final String pattern;
		private int level;
		private final int[] captureInit = new int[MAX_CAPTURES];
		private final int[] captureLength = new int[MAX_CAPTURES];
		private final String[] captureValues = new String[MAX_CAPTURES];

		public MatchState(String string, String pattern) {
			this.string = string;
			this.pattern = pattern;
			this.level = 0;
		}

		/**
		 * Reset the match state machine
		 */
		public void reset() {
			level = 0;
			Arrays.fill(captureValues, null);
		}

		/**
		 * Push all captures of the match
		 *
		 * @param offset The start position of the match
		 * @param end    The end position of the match
		 * @return The matched string
		 */
		public String[] getCaptures(int offset, int end) {
			int levels = level == 0 ? 1 : level;
			String[] v = new String[levels];
			for (int i = 0; i < levels; ++i) {
				v[i] = getCapture(i, offset, end);
			}
			return v;
		}

		/**
		 * Get one capture
		 *
		 * @param index  The particular capture to get
		 * @param offset The start position of the match
		 * @param end    The end position of the match
		 * @return The specific capture
		 */
		private String getCapture(int index, int offset, int end) {
			if (index >= this.level) {
				if (index == 0) {
					return string.substring(offset, end);
				} else {
					throw new IllegalArgumentException("invalid capture index");
				}
			} else {
				int l = captureLength[index];
				if (l == CAP_UNFINISHED) {
					throw new IllegalArgumentException("unfinished capture");
				}
				if (l == CAP_POSITION) {
					return String.valueOf(captureInit[index] + 1);
				} else {
					int begin = captureInit[index];
					return string.substring(begin, begin + l);
				}
			}
		}

		private int captureToClose() {
			int index = this.level;
			for (index--; index >= 0; index--) {
				if (captureLength[index] == CAP_UNFINISHED) {
					return index;
				}
			}
			throw new IllegalArgumentException("invalid pattern capture");
		}

		/**
		 * Find the end point of this capture class (escape character, square brackets or normal).
		 *
		 * @param patternOffset The offset into the pattern
		 * @return The last position of this class
		 */
		private int classEnd(int patternOffset) {
			switch (pattern.charAt(patternOffset++)) {
				case L_ESC:
					if (patternOffset == pattern.length()) {
						throw new IllegalArgumentException("malformed pattern (ends with %)");
					}
					return patternOffset + 1;

				case '[':
					if (pattern.charAt(patternOffset) == '^') patternOffset++;
					do {
						if (patternOffset == pattern.length()) {
							throw new IllegalArgumentException("malformed pattern (missing ])");
						}
						if (pattern.charAt(patternOffset++) == L_ESC && patternOffset != pattern.length()) {
							patternOffset++;
						}
					} while (pattern.charAt(patternOffset) != ']');
					return patternOffset + 1;
				default:
					return patternOffset;
			}
		}

		/**
		 * Match a set of square backets
		 *
		 * @param character    The character to match
		 * @param patternStart The position to start with, inclusive
		 * @param patternEnd   The position to end at, exclusive
		 * @return If the match was successful
		 */
		private boolean matchBracketClass(int character, int patternStart, int patternEnd) {
			boolean sig = true;
			if (pattern.charAt(patternStart + 1) == '^') {
				sig = false;
				patternStart++;
			}
			while (++patternStart < patternEnd) {
				if (pattern.charAt(patternStart) == L_ESC) {
					patternStart++;
					if (matchClass(character, pattern.charAt(patternStart))) {
						return sig;
					}
				} else if ((pattern.charAt(patternStart + 1) == '-') && (patternStart + 2 < patternEnd)) {
					patternStart += 2;
					if (pattern.charAt(patternStart - 2) <= character && character <= pattern.charAt(patternStart)) {
						return sig;
					}
				} else if (pattern.charAt(patternStart) == character) return sig;
			}
			return !sig;
		}

		private boolean singleMatch(int c, int poff, int ep) {
			switch (pattern.charAt(poff)) {
				case '.':
					return true;
				case L_ESC:
					return matchClass(c, pattern.charAt(poff + 1));
				case '[':
					return matchBracketClass(c, poff, ep - 1);
				default:
					return pattern.charAt(poff) == c;
			}
		}

		/**
		 * Perform pattern matching. If there is a match, returns offset into string
		 * where match ends, otherwise returns -1.
		 */
		private int match(int stringOffset, int patternOffset) {
			while (true) {
				// Check if we are at the end of the pattern -
				// equivalent to the '\0' case in the C version, but our pattern
				// string is not NULL-terminated.
				if (patternOffset == pattern.length()) {
					return stringOffset;
				}

				// Handle all the "advanced" matches
				switch (pattern.charAt(patternOffset)) {
					case '(':
						if (++patternOffset < pattern.length() && pattern.charAt(patternOffset) == ')') {
							// If we've got 0 length captures then push the position
							return startCapture(stringOffset, patternOffset + 1, CAP_POSITION);
						} else {
							// Otherwise push an "unfinished" capture
							return startCapture(stringOffset, patternOffset, CAP_UNFINISHED);
						}
					case ')':
						// Attempt to end the current capture
						return endCapture(stringOffset, patternOffset + 1);
					case L_ESC:
						if (patternOffset + 1 == pattern.length()) {
							throw new IllegalArgumentException("malformed pattern (ends with '%')");
						}
						switch (pattern.charAt(patternOffset + 1)) {
							case 'b':
								stringOffset = matchBalance(stringOffset, patternOffset + 2);
								if (stringOffset == -1) return -1;
								patternOffset += 4;
								continue;
							case 'f': {
								patternOffset += 2;
								if (pattern.charAt(patternOffset) != '[') {
									throw new IllegalArgumentException("Missing [ after %f in pattern");
								}
								int ep = classEnd(patternOffset);
								int previous = (stringOffset == 0) ? 0 : string.charAt(stringOffset - 1);
								if (matchBracketClass(previous, patternOffset, ep - 1) || (stringOffset < string.length() && !matchBracketClass(string.charAt(stringOffset), patternOffset, ep - 1))) {
									return -1;
								}
								patternOffset = ep;
								continue;
							}
							default: {
								int c = pattern.charAt(patternOffset + 1);
								if (Character.isDigit((char) c)) {
									stringOffset = matchCapture(stringOffset, c);
									if (stringOffset == -1) {
										return -1;
									}
									return match(stringOffset, patternOffset + 2);
								}
							}
						}
						break;
					case '$':
						if (patternOffset + 1 == pattern.length()) {
							return (stringOffset == string.length()) ? stringOffset : -1;
						}
				}

				// Handle normal things
				int patternEnd = classEnd(patternOffset);
				boolean m = stringOffset < string.length() && singleMatch(string.charAt(stringOffset), patternOffset, patternEnd);
				int patChar = (patternEnd < pattern.length()) ? pattern.charAt(patternEnd) : '\0';

				switch (patChar) {
					case '?':
						int res;
						if (m && ((res = match(stringOffset + 1, patternEnd + 1)) != -1)) {
							return res;
						}
						patternOffset = patternEnd + 1;
						continue;
					case '*':
						return maxExpand(stringOffset, patternOffset, patternEnd);
					case '+':
						return (m ? maxExpand(stringOffset + 1, patternOffset, patternEnd) : -1);
					case '-':
						return minExpand(stringOffset, patternOffset, patternEnd);
					default:
						if (!m) {
							return -1;
						}
						stringOffset++;
						patternOffset = patternEnd;
				}
			}
		}

		private int maxExpand(int stringOffset, int patternOffset, int ep) {
			int i = 0;
			while (stringOffset + i < string.length() &&
				singleMatch(string.charAt(stringOffset + i), patternOffset, ep)) {
				i++;
			}
			while (i >= 0) {
				int res = match(stringOffset + i, ep + 1);
				if (res != -1) {
					return res;
				}
				i--;
			}
			return -1;
		}

		private int minExpand(int stringOffset, int patternOffset, int ep) {
			while (true) {
				int res = match(stringOffset, ep + 1);
				if (res != -1) {
					return res;
				} else if (stringOffset < string.length() && singleMatch(string.charAt(stringOffset), patternOffset, ep)) {
					stringOffset++;
				} else {
					return -1;
				}
			}
		}

		private int startCapture(int stringOffset, int patternOffset, int what) {
			int level = this.level;
			if (level >= MAX_CAPTURES) throw new IllegalArgumentException("too many captures");

			captureInit[level] = stringOffset;
			captureLength[level] = what;

			// Move to the next capture index
			this.level = level + 1;

			int res = match(stringOffset, patternOffset);

			// If match failed then undo capture
			if (res == -1) this.level--;

			return res;
		}

		private int endCapture(int stringOffset, int patternOffset) {
			// Find the capture to close
			int level = captureToClose();

			// "Finish" this capture
			captureLength[level] = stringOffset - captureInit[level];

			int res = match(stringOffset, patternOffset);

			// If matching failed then undo capture
			if (res == -1) captureLength[level] = CAP_UNFINISHED;

			return res;
		}

		/**
		 * Ensure the string matches the a previous capture
		 *
		 * @param stringOffset The offset to start matching at
		 * @param index        The capture index
		 * @return The new offset, or -1 if it failed
		 */
		private int matchCapture(int stringOffset, int index) {
			index -= '1';
			if (index < 0 || index >= level || captureLength[index] == CAP_UNFINISHED) {
				throw new IllegalArgumentException("invalid capture index %" + index);
			}

			int len = captureLength[index];
			if (string.length() - stringOffset >= len && equals(string, captureInit[index], string, stringOffset, len)) {
				return stringOffset + len;
			} else {
				return -1;
			}
		}

		private static boolean equals(String a, int startA, String b, int startB, int length) {
			return a.substring(startA, startA + length - 1).equals(b.substring(startB, startB + length - 1));
		}

		/**
		 * Match balanced pairs, counting the number of opening and closing brackets
		 *
		 * @param stringOffset  The offset of the string to match
		 * @param patternOffset The offset of the pattern to match
		 * @return The new offset to continue matching at or -1 if it failed
		 */
		private int matchBalance(int stringOffset, int patternOffset) {
			final int patternLen = pattern.length();
			if (patternOffset == patternLen || patternOffset + 1 == patternLen) {
				throw new IllegalArgumentException("unbalanced pattern");
			}

			if (stringOffset >= string.length() || string.charAt(stringOffset) != pattern.charAt(patternOffset)) {
				return -1;
			} else {
				int begin = pattern.charAt(patternOffset);
				int end = pattern.charAt(patternOffset + 1);
				int count = 1;
				while (++stringOffset < string.length()) {
					if (string.charAt(stringOffset) == end) {
						if (--count == 0) return stringOffset + 1;
					} else if (string.charAt(stringOffset) == begin) {
						count++;
					}
				}
			}

			return -1;
		}
	}
}
