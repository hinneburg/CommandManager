package cc.commandmanager.core;

import static org.fest.assertions.Assertions.assertThat;
import net.sf.qualitycheck.exception.IllegalInstanceOfArgumentException;

import org.junit.Test;

public class ResultStateTest {

	private Throwable cause = new RuntimeException("Exception!");
	private ResultState success = ResultState.success();
	private ResultState warning = ResultState.warning("Warning!", cause);
	private ResultState failure = ResultState.failure("Failure!", cause);

	@Test
	public void testIsSuccess() {
		assertThat(success.isSuccess()).isTrue();
		assertThat(warning.isSuccess()).isFalse();
		assertThat(failure.isSuccess()).isFalse();
	}

	@Test
	public void testIsWarning() {
		assertThat(success.isWarning()).isFalse();
		assertThat(warning.isWarning()).isTrue();
		assertThat(failure.isWarning()).isFalse();
	}

	@Test
	public void testIsFailure() {
		assertThat(success.isFailure()).isFalse();
		assertThat(warning.isFailure()).isFalse();
		assertThat(failure.isFailure()).isTrue();
	}

	@Test
	public void testGetMessage() {
		assertThat(warning.getMessage()).isEqualTo("Warning!");
		assertThat(failure.getMessage()).isEqualTo("Failure!");
	}

	@Test(expected = IllegalInstanceOfArgumentException.class)
	public void testGetMessage_noWarningOrFailure() {
		success.getMessage();
	}

	@Test
	public void testHasCause() {
		assertThat(warning.hasCause()).isTrue();
		assertThat(ResultState.warning("Warning!").hasCause()).isFalse();
	}

	@Test(expected = IllegalInstanceOfArgumentException.class)
	public void testHasCause_noWarningOrFailure() {
		success.hasCause();
	}

	@Test
	public void testGetCause() {
		assertThat(warning.getCause()).isEqualTo(cause);
		assertThat(failure.getCause()).isEqualTo(cause);
	}

	@Test(expected = IllegalInstanceOfArgumentException.class)
	public void testGetCause_noWarningOrFailure() {
		success.getCause();
	}

	@Test
	public void testConstructWithoutCause() {
		ResultState state = ResultState.failure("Failure!");
		assertThat(state.getMessage()).isEqualTo("Failure!");
		assertThat(state.getCause()).isNull();
	}

	@Test
	public void testConstructWithoutMessage() {
		ResultState state = ResultState.failure(cause);
		assertThat(state.getMessage()).isEqualTo("Exception!");
		assertThat(state.getCause()).isEqualTo(cause);
	}

}
