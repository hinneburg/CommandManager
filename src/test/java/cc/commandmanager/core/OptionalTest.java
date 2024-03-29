package cc.commandmanager.core;

import static junit.framework.TestCase.fail;
import static org.fest.assertions.Assertions.assertThat;
import net.sf.qualitycheck.exception.IllegalNullArgumentException;

import net.sf.qualitycheck.exception.IllegalStateOfArgumentException;
import org.junit.Test;

public class OptionalTest {

	private Try<Object> optional;

	@Test
	public void testIsPresent_isPresent() {
		optional = new Try<Object>("Test");
		assertThat(optional.isPresent()).isTrue();
	}

	@Test
	public void testIsPresent_isNull() {
		optional = new Try<Object>(null);
		assertThat(optional.isPresent()).isFalse();
	}

	@Test
	public void testGet() {
		optional = new Try<Object>("Test");
		assertThat(optional.get()).isEqualTo("Test");
	}

	@Test
	public void testGetOrNull_isPresent() {
		optional = new Try<Object>("Test");
		assertThat(optional.getOrNull()).isEqualTo("Test");
	}

	@Test
	public void testGetOrNull_isNull() {
		optional = new Try<Object>(null);
		assertThat(optional.getOrNull()).isNull();
	}

	@Test(expected = IllegalStateOfArgumentException.class)
	public void testGet_null() {
		optional = new Try<Object>(null);
		optional.get();
	}

    @Test
    public void testGet_null_noteIsShipped() {
        optional = new Try<Object>(null, "Something went wrong.");
        try {
            optional.get();
            fail(IllegalStateOfArgumentException.class.getName() + " should have been thrown.");
        } catch (IllegalStateOfArgumentException e) {
            assertThat(e.getMessage()).contains("Something went wrong.");
        }
    }

	@Test
	public void testGetNote_notePresent() {
		optional = new Try<Object>(null, "Note");
		assertThat(optional.getNote()).isEqualTo("Note");
	}

	@Test
	public void testGetNote_noteNull() {
		optional = new Try<Object>(null, null);
		assertThat(optional.getNote()).isEqualTo("");
	}

	@Test
	public void testCreateWithEmptyNote() {
		optional = new Try<Object>("Test");
		assertThat(optional.getNote()).isEqualTo("");
	}

}
