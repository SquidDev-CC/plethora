package org.squiddev.plethora.core.docdump;

import dan200.computercraft.api.lua.ILuaObject;
import dan200.computercraft.api.lua.LuaException;
import org.squiddev.plethora.api.meta.TypedMeta;
import org.squiddev.plethora.core.MethodWrapperLuaObject;
import org.squiddev.plethora.integration.MetaWrapper;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ObjectWriter {
	private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("#.#######");

	protected final Appendable output;

	public ObjectWriter(Appendable stream) {
		output = stream;
	}

	protected void writeValue(String value) throws IOException {
		output.append('"').append(value).append('"');
	}

	protected void writeValue(int value) throws IOException {
		output.append(Integer.toString(value));
	}

	protected void writeValue(double value) throws IOException {
		if (Double.isFinite(value)) {
			output.append(NUMBER_FORMAT.format(value));
		} else if (Double.isNaN(value)) {
			output.append("nan");
		} else {
			output.append(value > 0 ? "inf" : "-inf");
		}
	}

	protected void writeValue(Void value) throws IOException {
		output.append("nil");
	}

	protected void writeValue(boolean value) throws IOException {
		output.append(Boolean.toString(value));
	}

	protected void writeValue(Map<?, ?> value, String indent) throws IOException {
		if (value.isEmpty()) {
			output.append("{}");
			return;
		}

		output.append("{");
		writeMapBody(value, indent);
		output.append("}");
	}

	protected void writeMapBody(Map<?, ?> value, String indent) throws IOException {
		// Try to work out if this is an "array like" table.
		boolean arrayLike = true;
		int max = 0;
		for (Object key : value.keySet()) {
			if (!(key instanceof Number)) {
				arrayLike = false;
				break;
			}

			Number number = (Number) key;
			int intVal = number.intValue();
			if (intVal <= 0 || intVal != number.doubleValue() || !Double.isFinite(intVal)) {
				arrayLike = false;
				break;
			}

			if (intVal > max) max = intVal;
		}

		if (arrayLike && max / 2 > value.size()) arrayLike = false;

		String childIndent = indent + "  ";

		if (arrayLike) {
			Object[] values = new Object[max];
			for (Map.Entry<?, ?> entry : value.entrySet()) {
				values[((Number) entry.getKey()).intValue() - 1] = entry.getValue();
			}

			for (int i = 0; i < max; i++) {
				if (i > 0) output.append(",");
				output.append("\n").append(childIndent);
				write(values[i], childIndent);
			}
		} else {
			List<Map.Entry<?, ?>> entries = new ArrayList<>(value.entrySet());
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

		output.append("\n").append(indent);
	}

	protected void writeSpecial(String special) throws IOException {
		output.append('\u00ab').append(special).append('\u00bb');
	}

	protected void writeReference(Object target) throws IOException {
		writeSpecial("reference to " + target.getClass().getName());
	}

	protected void writeLuaObject(ILuaObject object) throws IOException {
		writeSpecial("object " + object.getClass().getName());
	}

	protected void writeMeta(TypedMeta<?, ?> meta, String indent) throws IOException {
		writeValue(meta, indent);
	}

	public final void write(Object value) throws IOException {
		write(value, "");
	}

	protected final void write(Object value, String indent) throws IOException {
		if (value instanceof String) {
			writeValue((String) value);
		} else if (value instanceof Number) {
			Number number = (Number) value;
			if (number.intValue() == number.doubleValue()) {
				writeValue(number.intValue());
			} else {
				writeValue(number.doubleValue());
			}
		} else if (value == null) {
			writeValue((Void) null);
		} else if (value instanceof Boolean) {
			writeValue((boolean) value);
		} else if (value instanceof TypedMeta) {
			writeMeta((TypedMeta<?, ?>) value, indent);
		} else if (value instanceof Map) {
			writeValue((Map<?, ?>) value, indent);
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
				writeReference(target);
			} else {
				writeLuaObject((ILuaObject) value);
			}
		} else if (value instanceof ILuaObject) {
			writeLuaObject((ILuaObject) value);
		} else {
			writeSpecial("unknown " + value.getClass().getName());
		}
	}
}
