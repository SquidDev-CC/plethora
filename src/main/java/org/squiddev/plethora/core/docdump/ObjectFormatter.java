package org.squiddev.plethora.core.docdump;

public interface ObjectFormatter {
	String formatInteger(int value);

	String formatDouble(double value);

	String formatBoolean(boolean value);

	String formatNil();

	String formatString(String value);

	String formatSpecial(String value);
}
