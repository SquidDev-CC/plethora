package org.squiddev.plethora.core;

import com.google.common.collect.Lists;
import dan200.computercraft.api.lua.LuaException;
import org.junit.Before;
import org.junit.Test;
import org.squiddev.plethora.api.converter.ConstantConverter;
import org.squiddev.plethora.api.reference.Reference;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class ConverterRegistryTest {
	private ConverterRegistry registry;

	@Before
	public void setup() {
		registry = new ConverterRegistry();
		registry.registerConverter(Value.class, (ConstantConverter<Value, Value>) from -> new Value(!from.active, from.value));
	}

	@Test
	public void testExtendConvertedValues() {
		List<String> keys = Lists.newArrayList("test");
		List<Object> values = Lists.newArrayList(new Value(true, 0));
		registry.extendConverted(keys, values, 0);

		assertEquals(Arrays.asList("test", "test"), keys);
		assertEquals(Arrays.asList(new Value(true, 0), new Value(false, 0)), values);
	}

	@Test
	public void testExtendConvertedValuesMultiple() {
		List<String> keys = Lists.newArrayList("test", "other");
		List<Object> values = Lists.newArrayList(new Value(true, 0), new Value(true, 0));
		registry.extendConverted(keys, values, 0);

		assertEquals(Arrays.asList("test", "other", "test", "other"), keys);
		assertEquals(Arrays.asList(new Value(true, 0), new Value(true, 0), new Value(false, 0), new Value(false, 0)), values);
	}

	@Test
	public void testExtendConvertedReferences() {
		List<String> keys = Lists.newArrayList("test");
		List<Object> values = Lists.newArrayList(new Value(true, 0));
		List<Object> references = Lists.newArrayList(Reference.id(new Value(true, 0)));

		registry.extendConverted(keys, values, references, 0);

		assertEquals(Arrays.asList("test", "test"), keys);
		assertEquals(Arrays.asList(new Value(true, 0), new Value(false, 0)), values);
		assertEquals(Arrays.asList(references.get(0), new Value(false, 0)), references);
	}

	@Test
	public void testExtendConverteReferencesMultiple() throws LuaException {
		List<String> keys = Lists.newArrayList("test", "other");
		List<Object> values = Lists.newArrayList(new Value(true, 0), new Value(true, 0));
		List<Object> references = Lists.newArrayList(values.stream().map(Reference::id).collect(Collectors.toList()));

		registry.extendConverted(keys, values, references, 0);

		assertEquals(Arrays.asList("test", "other", "test", "other"), keys);
		assertEquals(Arrays.asList(new Value(true, 0), new Value(true, 0), new Value(false, 0), new Value(false, 0)), values);

		assertEquals(new Value(false, 0), references.get(2));
		assertEquals(ConverterReference.class, references.get(3).getClass());
		assertEquals(new Value(false, 0), ((ConverterReference) references.get(3))
			.tryConvert(new Object[]{null, null, new Value(false, 0)}));
	}

	public static class Value {
		public final boolean active;
		public final int value;

		public Value(boolean active, int value) {
			this.active = active;
			this.value = value;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			Value other = (Value) o;
			return active == other.active && value == other.value;
		}

		@Override
		public int hashCode() {
			return (active ? 1 : 0) + (value << 1);
		}

		@Override
		public String toString() {
			return "«" + active + ": " + value + "»";
		}
	}
}
