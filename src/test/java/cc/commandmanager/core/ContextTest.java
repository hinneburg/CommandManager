package cc.commandmanager.core;

import static org.fest.assertions.Assertions.assertThat;

import java.util.Map;

import net.sf.qualitycheck.exception.IllegalNullArgumentException;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ContextTest {

	private Context context;

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
		context.put("key", "value");
		assertThat(context.get("key")).isEqualTo("value");
	}

	@Test
	public void testPut_nullValue() {
		context.put("key", null);
		assertThat(context.get("key")).isNull();
	}

	@Test
	public void testRemove() {
		context.put("key", "value");
		context.remove("key");
		assertThat(context.get("key")).isNull();
	}

	@Test
	public void testPutAll() {
		Map<String, Object> numbers = Maps.newHashMap();
		numbers.put("one", 1);
		numbers.put("two", 2);
		numbers.put("three", 3);

		context.putAll(numbers);
		assertThat(context.get("one")).isEqualTo(1);
		assertThat(context.get("two")).isEqualTo(2);
		assertThat(context.get("three")).isEqualTo(3);
	}

	@Test
	public void testGet_typeSafe() {
		context.put("int", 1);
		context.put("string", "foo");
		assertThat(context.get("int", Integer.class)).isEqualTo(1);
		assertThat(context.get("string", String.class)).isEqualTo("foo");
	}

	@Test(expected = ResultTypeMismatchException.class)
	public void testGet_typeSafe_typeMismatch() {
		context.put("int", "foo");
		context.get("int", Integer.class);
	}

	@Test
	public void testGetInteger() {
		context.put("Integer", 1);
		assertThat(context.getInteger("Integer")).isEqualTo(1);
	}

	@Test(expected = ResultTypeMismatchException.class)
	public void testGetInteger_typeMismatch() {
		context.put("Integer", "noInt");
		context.getInteger("Integer");
	}

	@Test
	public void testGetDouble() {
		context.put("Double", 1d);
		assertThat(context.getDouble("Double")).isEqualTo(1d);
	}

	@Test(expected = ResultTypeMismatchException.class)
	public void testGetDouble_typeMismatch() {
		context.put("Double", "noDouble");
		context.getDouble("Double");
	}

	@Test
	public void testGetBoolean() {
		context.put("Boolean", Boolean.TRUE);
		assertThat(context.getBoolean("Boolean")).isEqualTo(Boolean.TRUE);
	}

	@Test(expected = ResultTypeMismatchException.class)
	public void testGetBoolean_typeMismatch() {
		context.put("Boolean", 5);
		context.getBoolean("Boolean");
	}

	@Test
	public void testGetString() {
		context.put("String", "String");
		assertThat(context.getString("String")).isEqualTo("String");
	}

	@Test(expected = ResultTypeMismatchException.class)
	public void testGetString_typeMismatch() {
		context.put("String", 5);
		context.getString("String");
	}

	@Test
	public void testGetIterable() {
		context.put("Iterable", Lists.newArrayList(1, 2, 3));
		assertThat(context.getIterable("Iterable")).containsOnly(1, 2, 3);
	}

	@Test(expected = ResultTypeMismatchException.class)
	public void testGetIterable_typeMismatch() {
		context.put("Iterable", "noIterable");
		context.getIterable("Iterable");
	}

	@Test(expected = IllegalNullArgumentException.class)
	public void testPut_nullKey() {
		context.put(null, "a");
	}

	@Test(expected = IllegalNullArgumentException.class)
	public void testGet_nullKey() {
		context.get(null);
	}

	@Test(expected = IllegalNullArgumentException.class)
	public void testRemove_nullKey() {
		context.remove(null);
	}

	@Test(expected = IllegalNullArgumentException.class)
	public void testPutAll_nullKey() {
		context.putAll(null);
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

	@Test
	public void testHashCode_equalHashCode() {
		Context equalContext = new Context();
		context.put("one", 1);
		equalContext.put("one", 1);
		assertThat(context.hashCode()).isEqualTo(equalContext.hashCode());
	}

	@Test
	public void testHashCode_differingHashCode() {
		Context otherContext = new Context();
		context.put("one", 1);
		otherContext.put("two", 2);
		assertThat(context.hashCode()).isNotEqualTo(otherContext.hashCode());
	}

}
