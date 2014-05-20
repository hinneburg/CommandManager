package cc.commandmanager.core;

import static org.fest.assertions.Assertions.assertThat;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Maps;

public class ContextTest {

	Context context;

	@Before
	public void setUp() {
		context = new Context();
	}

	@Test
	public void testCopyConstructor() {
		assertThat(new Context(context)).isEqualTo(context);
		assertThat(new Context(context)).isNotSameAs(context);
	}

	@Test
	public void testPut() {
		Object expected = new Object();
		String actual = "new Object";

		context.put(actual, expected);

		assertThat(context.get(actual)).isSameAs(expected);
	}

	@Test
	public void testPutWithNulls() {
		Object expected = new Object();
		context.put(null, expected);
		assertThat(context.get(null)).isSameAs(expected);

		String actual = "new Object";
		context.put(actual, null);
		assertThat(context.get(actual)).isNull();
	}

	@Test
	public void testRemove() {
		String actual = "new Object";
		context.put(actual, new Object());

		context.remove(actual);

		assertThat(context.get(actual)).isNull();
	}

	@Test
	public void testPutAll() {
		Map<String, Object> expected = Maps.newHashMap();
		Object one = new Object(), two = new Object(), three = new Object();
		expected.put("one", one);
		expected.put("two", two);
		expected.put("three", three);

		context.putAll(expected);

		Map<String, Object> actual = Maps.newHashMap();
		actual.put("one", context.get("one"));
		actual.put("two", context.get("two"));
		actual.put("three", context.get("three"));
		assertThat(actual).isEqualTo(expected);
	}

	@Test
	public void testEquals() {
		assertThat(context.equals(42)).isFalse();
		assertThat(context.equals(null)).isFalse();
		assertThat(context.equals(context)).isTrue();

		Context equalContext = new Context();

		context.put("one", 1);
		equalContext.put("one", 1);

		assertThat(context.equals(equalContext)).isTrue();
	}

}
