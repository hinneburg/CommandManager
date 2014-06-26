package cc.commandmanager.core;

import static org.fest.assertions.Assertions.assertThat;
import net.sf.qualitycheck.exception.IllegalNullArgumentException;

import org.junit.Test;

public class OptionalTest {

	private Optional<Object> optional;

	@Test
	public void testIsPresent_isPresent() {
		optional = new Optional<Object>("Test");
		assertThat(optional.isPresent()).isTrue();
	}

	@Test
	public void testIsPresent_isNull() {
		optional = new Optional<Object>(null);
		assertThat(optional.isPresent()).isFalse();
	}

	@Test
	public void testGet() {
		optional = new Optional<Object>("Test");
		assertThat(optional.get()).isEqualTo("Test");
	}

	@Test
	public void testGetOrNull_isPresent() {
		optional = new Optional<Object>("Test");
		assertThat(optional.getOrNull()).isEqualTo("Test");
	}

	@Test
	public void testGetOrNull_isNull() {
		optional = new Optional<Object>(null);
		assertThat(optional.getOrNull()).isNull();
	}

	@Test(expected = IllegalNullArgumentException.class)
	public void testGet_null() {
		optional = new Optional<Object>(null);
		optional.get();
	}

	@Test
	public void testGetNote_notePresent() {
		optional = new Optional<Object>(null, "Note");
		assertThat(optional.getNote()).isEqualTo("Note");
	}

	@Test
	public void testGetNote_noteNull() {
		optional = new Optional<Object>(null, null);
		assertThat(optional.getNote()).isEqualTo("");
	}

	@Test
	public void testCreateWithEmptyNote() {
		optional = new Optional<Object>("Test");
		assertThat(optional.getNote()).isEqualTo("");
	}

}
