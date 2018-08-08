package org.squiddev.plethora.core.docdump;

import dan200.computercraft.api.lua.ILuaObject;
import dan200.computercraft.api.lua.LuaException;
import org.squiddev.plethora.core.MethodWrapperLuaObject;
import org.squiddev.plethora.integration.MetaWrapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ObjectWriter {
	private final Appendable output;
	private final ObjectFormatter formatter;

	public ObjectWriter(Appendable stream, ObjectFormatter formatter) {
		this.output = stream;
		this.formatter = formatter;
	}

	public void write(Object value) throws IOException {
		write(value, "");
	}

	private void write(Object value, String indent) throws IOException {
		if (value instanceof String) {
			output.append(formatter.formatString((String) value));
		} else if (value instanceof Number) {
			Number number = (Number) value;
			if (number.intValue() == number.doubleValue()) {
				output.append(formatter.formatInteger(number.intValue()));
			} else {
				output.append(formatter.formatDouble(number.doubleValue()));
			}
		} else if (value == null) {
			output.append(formatter.formatNil());
		} else if (value instanceof Boolean) {
			output.append(formatter.formatBoolean((Boolean) value));
		} else if (value instanceof Map) {
			Map<?, ?> map = (Map) value;
			if (map.isEmpty()) {
				output.append("{}");
				return;
			}

			// Try to work out if this is an "array like" table.
			boolean arrayLike = true;
			int max = 0;
			for (Object key : map.keySet()) {
				if (!(key instanceof Number)) {
					arrayLike = false;
					break;
				}

				Number number = (Number) key;
				int intVal = number.intValue();
				if (intVal <= 0 || intVal != number.doubleValue()) {
					arrayLike = false;
					break;
				}

				if (intVal > max) max = intVal;
			}

			if (arrayLike && max / 2 > map.size()) arrayLike = false;

			String childIndent = indent + "  ";
			output.append("{");

			if (arrayLike) {
				Object[] values = new Object[max];
				for (Map.Entry<?, ?> entry : map.entrySet()) {
					values[((Number) entry.getKey()).intValue() - 1] = entry.getValue();
				}

				for (int i = 0; i < max; i++) {
					if (i > 0) output.append(",");
					output.append("\n").append(childIndent);
					write(values[i], childIndent);
				}
			} else {
				List<Map.Entry<?, ?>> entries = new ArrayList<>(map.entrySet());
				entries.sort((a, b) -> {
					Object ak = a.getKey(), bk = b.getKey();
					if (ak instanceof String && bk instanceof String) {
						return ((String) ak).compareTo((String) bk);
					} else if (ak instanceof Number && bk instanceof Number) {
						return Double.compare(((Number) ak).doubleValue(), ((Number) bk).doubleValue());
					} else {
						return Integer.compare(ak.hashCode(), bk.hashCode());
					}
				});

				for (int i = 0; i < entries.size(); i++) {
					Map.Entry<?, ?> entry = entries.get(i);

					if (i > 0) output.append(",");
					output.append("\n").append(childIndent);

					Object key = entry.getKey();
					if (key instanceof String && ((String) key).matches("^[a-zA-Z_][0-9a-zA-Z_]*$")) {
						output.append((String) key);
					} else {
						output.append("[");
						write(entry.getKey(), childIndent);
						output.append("]");
					}

					output.append(" = ");
					write(entry.getValue(), childIndent);
				}
			}

			output.append("\n").append(indent).append("}");
		} else if (value instanceof MethodWrapperLuaObject) {
			Object target = null;
			try {
				target = ((MethodWrapperLuaObject) value).getContext(0).safeBake().getTarget();
			} catch (LuaException ignored) {
			}

			while (target instanceof MetaWrapper) {
				target = ((MetaWrapper) target).value();
			}

			if (target != null) {
				output.append(formatter.formatSpecial("\u00abreference to " + target.getClass().getName() + "\u00bb"));
			} else {
				output.append(formatter.formatSpecial("\u00abobject " + value.getClass().getName() + "\u00bb"));
			}
		} else if (value instanceof ILuaObject) {
			output.append(formatter.formatSpecial("\u00abobject " + value.getClass().getName() + "\u00bb"));
		} else {
			output.append(formatter.formatSpecial("\u00abunknown " + value.getClass().getName() + "\u00bb"));
		}
	}
}
