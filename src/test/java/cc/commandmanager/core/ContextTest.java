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
	public void testBind() {
		context.bind("key", "value");
		assertThat(context.get("key")).isEqualTo("value");
	}

	@Test(expected = KeyAlreadyBoundException.class)
	public void testBind_alreadyBound() {
		context.bind("key", "value");
		context.bind("key", "value");
	}

	@Test
	public void testBind_nullValue() {
		context.bind("key", null);
		assertThat(context.get("key")).isNull();
	}

	@Test
	public void testUnbind() {
		context.bind("key", "value");
		context.unbind("key");
		assertThat(context.get("key")).isNull();
	}

	@Test(expected = KeyNotBoundException.class)
	public void testUnbind_nothingBound() {
		context.unbind("key");
	}

	@Test
	public void testRebind() {
		context.bind("key", "value");
		context.rebind("key", "newValue");
		assertThat(context.get("key")).isEqualTo("newValue");
	}

	@Test(expected = KeyNotBoundException.class)
	public void testRebind_nothingBound() {
		context.rebind("key", "newValue");
	}

	@Test
	public void testBindAll() {
		Map<String, Object> numbers = Maps.newHashMap();
		numbers.put("one", 1);
		numbers.put("two", 2);
		numbers.put("three", 3);

		context.bindAll(numbers);
		assertThat(context.get("one")).isEqualTo(1);
		assertThat(context.get("two")).isEqualTo(2);
		assertThat(context.get("three")).isEqualTo(3);
	}

	@Test(expected = KeyAlreadyBoundException.class)
	public void testBindAll_alreadyBound() {
		Map<String, Object> numbers = Maps.newHashMap();
		numbers.put("one", 1);
		numbers.put("two", 2);
		numbers.put("three", 3);

		context.bind("one", 1.0D);
		context.bindAll(numbers);
	}

	@Test
	public void testGet_typeSafe() {
		context.bind("int", 1);
		context.bind("string", "foo");
		assertThat(context.get("int", Integer.class)).isEqualTo(1);
		assertThat(context.get("string", String.class)).isEqualTo("foo");
	}

	@Test(expected = ResultTypeMismatchException.class)
	public void testGet_typeSafe_typeMismatch() {
		context.bind("int", "foo");
		context.get("int", Integer.class);
	}

	@Test
	public void testGetInteger() {
		context.bind("Integer", 1);
		assertThat(context.getInteger("Integer")).isEqualTo(1);
	}

	@Test(expected = ResultTypeMismatchException.class)
	public void testGetInteger_typeMismatch() {
		context.bind("Integer", "noInt");
		context.getInteger("Integer");
	}

	@Test
	public void testGetDouble() {
		context.bind("Double", 1d);
		assertThat(context.getDouble("Double")).isEqualTo(1d);
	}

	@Test(expected = ResultTypeMismatchException.class)
	public void testGetDouble_typeMismatch() {
		context.bind("Double", "noDouble");
		context.getDouble("Double");
	}

	@Test
	public void testGetBoolean() {
		context.bind("Boolean", Boolean.TRUE);
		assertThat(context.getBoolean("Boolean")).isEqualTo(Boolean.TRUE);
	}

	@Test(expected = ResultTypeMismatchException.class)
	public void testGetBoolean_typeMismatch() {
		context.bind("Boolean", 5);
		context.getBoolean("Boolean");
	}

	@Test
	public void testGetString() {
		context.bind("String", "String");
		assertThat(context.getString("String")).isEqualTo("String");
	}

	@Test(expected = ResultTypeMismatchException.class)
	public void testGetString_typeMismatch() {
		context.bind("String", 5);
		context.getString("String");
	}

	@Test
	public void testGetIterable() {
		context.bind("Iterable", Lists.newArrayList(1, 2, 3));
		assertThat(context.getIterable("Iterable")).containsOnly(1, 2, 3);
	}

	@Test(expected = ResultTypeMismatchException.class)
	public void testGetIterable_typeMismatch() {
		context.bind("Iterable", "noIterable");
		context.getIterable("Iterable");
	}

	@Test(expected = IllegalNullArgumentException.class)
	public void testBind_nullKey() {
		context.bind(null, "a");
	}

	@Test(expected = IllegalNullArgumentException.class)
	public void testGet_nullKey() {
		context.get(null);
	}

	@Test(expected = IllegalNullArgumentException.class)
	public void testRemove_nullKey() {
		context.unbind(null);
	}

	@Test(expected = IllegalNullArgumentException.class)
	public void testBindAll_nullKey() {
		context.bindAll(null);
	}

	@Test
	public void testEquals() {
		assertThat(context.equals(42)).isFalse();
		assertThat(context.equals(null)).isFalse();
		assertThat(context.equals(context)).isTrue();

		Context equalContext = new Context();
		context.bind("one", 1);
		equalContext.bind("one", 1);
		assertThat(context.equals(equalContext)).isTrue();
	}

	@Test
	public void testHashCode_equalHashCode() {
		Context equalContext = new Context();
		context.bind("one", 1);
		equalContext.bind("one", 1);
		assertThat(context.hashCode()).isEqualTo(equalContext.hashCode());
	}

	@Test
	public void testHashCode_differingHashCode() {
		Context otherContext = new Context();
		context.bind("one", 1);
		otherContext.bind("two", 2);
		assertThat(context.hashCode()).isNotEqualTo(otherContext.hashCode());
	}

}
