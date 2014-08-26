package cc.commandmanager.core;

import static org.fest.assertions.Assertions.assertThat;

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

	@Test(expected = IllegalStateException.class)
	public void testGetMessage_noWarningOrFailure() {
		success.getMessage();
	}

	@Test
	public void testHasCause() {
		assertThat(warning.hasCause()).isTrue();
		assertThat(ResultState.warning("Warning!").hasCause()).isFalse();
	}

	@Test(expected = IllegalStateException.class)
	public void testHasCause_noWarningOrFailure() {
		success.hasCause();
	}

	@Test
	public void testGetCause() {
		assertThat(warning.getCause()).isEqualTo(cause);
		assertThat(failure.getCause()).isEqualTo(cause);
	}

	@Test(expected = IllegalStateException.class)
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

	@Test
	public void testEquals() {
		assertThat(success.equals(ResultState.success())).isTrue();
		assertThat(success.equals(warning)).isFalse();
		assertThat(success.equals(failure)).isFalse();
		assertThat(warning.equals(ResultState.warning("Different warning!"))).isFalse();
		assertThat(failure.equals(ResultState.failure("Different failure!"))).isFalse();
		assertThat(warning.equals(ResultState.warning("Warning!", cause))).isTrue();
		assertThat(failure.equals(ResultState.failure("Failure!", cause))).isTrue();
		assertThat(warning.equals(ResultState.failure("Warning!", cause))).isFalse();
		assertThat(failure.equals(ResultState.warning("Failure!", cause))).isFalse();
	}

	@Test
	public void testHashcode() {
		assertThat(success.hashCode() == ResultState.success().hashCode());
		assertThat(warning.hashCode() == ResultState.warning("Warning!", cause).hashCode());
		assertThat(failure.hashCode() == ResultState.failure("Failure!", cause).hashCode());
		assertThat(warning.hashCode() != ResultState.failure("Warning!", cause).hashCode());
		assertThat(failure.hashCode() != ResultState.warning("Failure!", cause).hashCode());
	}

}
