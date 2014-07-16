package cc.commandmanager.core;

import static org.fest.assertions.Assertions.assertThat;

import java.util.Collections;

import net.sf.qualitycheck.exception.IllegalEmptyArgumentException;
import net.sf.qualitycheck.exception.IllegalNotEqualException;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class ComposedResultStateTest {

	private Throwable cause = new RuntimeException("Exception!");
	private ResultState success = ResultState.success();
	private ResultState warning = ResultState.warning("Warning!", cause);
	private ResultState failure = ResultState.failure("Failure!", cause);
	private CommandClass command = new CommandClass("command", "command.class");

	@Test
	public void testGetOverallResultState_success_singleExecution() {
		ComposedResultState result = new ComposedResultState(ImmutableList.of(success), ImmutableList.of(command));
		assertSuccess(result);
	}

	@Test
	public void testGetOverallResultState_success_multipleExecutions() {
		ComposedResultState result = new ComposedResultState(ImmutableList.of(success, success, success), ImmutableList
				.of(command, command, command));
		assertSuccess(result);
	}

	@Test
	public void testGetOverallResultState_warning_successAndWarning() {
		ComposedResultState result = new ComposedResultState(ImmutableList.of(success, warning), ImmutableList.of(
				command, command));
		assertWarning(result);
	}

	@Test
	public void testGetOverallResultState_warning_onlyWarnings() {
		ComposedResultState result = new ComposedResultState(ImmutableList.of(warning, warning), ImmutableList.of(
				command, command));
		assertWarning(result);
	}

	@Test
	public void testGetOverallResultState_failure_successWarningAndFailure() {
		ComposedResultState result = new ComposedResultState(ImmutableList.of(success, warning, failure), ImmutableList
				.of(command, command, command));
		assertFailure(result);
	}

	@Test
	public void testGetOverallResultState_failure_WarningAndFailure() {
		ComposedResultState result = new ComposedResultState(ImmutableList.of(warning, failure), ImmutableList.of(
				command, command));
		assertFailure(result);
	}

	@Test
	public void testGetOverallResultState_failure_onlyFailures() {
		ComposedResultState result = new ComposedResultState(ImmutableList.of(failure, failure), ImmutableList.of(
				command, command));
		assertFailure(result);
	}

	@Test
	public void testGetOverallResultState_failure_successAndFailure() {
		ComposedResultState result = new ComposedResultState(ImmutableList.of(failure, success), ImmutableList.of(
				command, command));
		assertFailure(result);
	}

	@Test
	public void testGetResultStates() {
		ComposedResultState result = new ComposedResultState(ImmutableList.of(success, warning, failure), ImmutableList
				.of(command, command, command));
		assertThat(result.getResultStates()).containsOnly(success, warning, failure);
	}

	@Test
	public void testGetExecutedCommands() {
		ComposedResultState result = new ComposedResultState(ImmutableList.of(success, warning, failure), ImmutableList
				.of(command, command, command));
		assertThat(result.getExecutedCommands()).containsOnly(command, command, command);
	}

	@Test
	public void testGetMessage() {
		ComposedResultState result = new ComposedResultState(ImmutableList.of(success, failure, warning), ImmutableList
				.of(command, command, command));
		assertThat(result.getMessage()).isEqualTo(failure.getMessage());
	}

	@Test(expected = IllegalStateException.class)
	public void testGetMessage_successStateDoesNotHaveMessage() {
		ComposedResultState result = new ComposedResultState(ImmutableList.of(success), ImmutableList.of(command));
		result.getMessage();
	}

	@Test
	public void testGetCause() {
		ComposedResultState result = new ComposedResultState(ImmutableList.of(success, failure, warning), ImmutableList
				.of(command, command, command));
		assertThat(result.getCause()).isEqualTo(failure.getCause());
	}

	@Test(expected = IllegalStateException.class)
	public void testGetCause_successStateDoesNotHaveCause() {
		ComposedResultState result = new ComposedResultState(ImmutableList.of(success), ImmutableList.of(command));
		result.getCause();
	}

	@Test(expected = IllegalEmptyArgumentException.class)
	public void testCreate_emptyStates() {
		new ComposedResultState(Collections.<ResultState> emptyList(), Collections.<CommandClass> emptyList());
	}

	@Test(expected = IllegalNotEqualException.class)
	public void testCreate_lessResultStatesThanCommands() {
		new ComposedResultState(ImmutableList.of(success), ImmutableList.of(command, command));
	}

	@Test(expected = IllegalNotEqualException.class)
	public void testCreate_moreResultStatesThanCommands() {
		new ComposedResultState(ImmutableList.of(success, success), ImmutableList.of(command));
	}

	@Test
	public void testEquals_self() {
		ComposedResultState state1 = new ComposedResultState(ImmutableList.<ResultState> of(ResultState
				.failure("Failure!")), ImmutableList.of(new CommandClass("name", "class")));
		assertThat(state1.equals(state1)).isTrue();
	}

	@Test
	public void testEquals_sameContent_differentReference() {
		ComposedResultState state1 = new ComposedResultState(ImmutableList.<ResultState> of(ResultState
				.failure("Failure!")), ImmutableList.of(new CommandClass("name", "class")));
		ComposedResultState state2 = new ComposedResultState(ImmutableList.<ResultState> of(ResultState
				.failure("Failure!")), ImmutableList.of(new CommandClass("name", "class")));
		assertThat(state1.equals(state2)).isTrue();
	}

	@Test
	public void testEquals_differentCommandClass() {
		ComposedResultState state1 = new ComposedResultState(ImmutableList.<ResultState> of(ResultState
				.failure("Failure!")), ImmutableList.of(new CommandClass("name", "class")));
		ComposedResultState state2 = new ComposedResultState(ImmutableList.<ResultState> of(ResultState
				.failure("Failure!")), ImmutableList.of(new CommandClass("name2", "class")));
		assertThat(state1.equals(state2)).isFalse();
	}

	@Test
	public void testEquals_differentResultState() {
		ComposedResultState state1 = new ComposedResultState(ImmutableList.<ResultState> of(ResultState
				.failure("Failure!")), ImmutableList.of(new CommandClass("name", "class")));
		ComposedResultState state2 = new ComposedResultState(ImmutableList.<ResultState> of(ResultState
				.failure("Failure2!")), ImmutableList.of(new CommandClass("name", "class")));
		assertThat(state1.equals(state2)).isFalse();
	}

	@Test
	public void testEquals_oneIsEqual_oneNot() {
		ComposedResultState state1 = new ComposedResultState(ImmutableList.<ResultState> of(ResultState.success(),
				ResultState.success()), ImmutableList.of(new CommandClass("name", "class"), new CommandClass("name2",
				"class")));
		ComposedResultState state2 = new ComposedResultState(ImmutableList.<ResultState> of(ResultState.success(),
				ResultState.success()), ImmutableList.of(new CommandClass("name", "class"), new CommandClass("name2",
				"class2")));
		assertThat(state1.equals(state2)).isFalse();
	}

	@Test
	public void testHashCode_equal() {
		ComposedResultState state1 = new ComposedResultState(ImmutableList.<ResultState> of(ResultState
				.failure("Failure!")), ImmutableList.of(new CommandClass("name", "class")));
		ComposedResultState state2 = new ComposedResultState(ImmutableList.<ResultState> of(ResultState
				.failure("Failure!")), ImmutableList.of(new CommandClass("name", "class")));
		assertThat(state1.hashCode()).isEqualTo(state2.hashCode());
	}

	@Test
	public void testHashCode_notEqual_differentCommandClasses() {
		ComposedResultState state1 = new ComposedResultState(ImmutableList.<ResultState> of(ResultState
				.failure("Failure!")), ImmutableList.of(new CommandClass("name", "class")));
		ComposedResultState state2 = new ComposedResultState(ImmutableList.<ResultState> of(ResultState
				.failure("Failure!")), ImmutableList.of(new CommandClass("name2", "class")));
		assertThat(state1.hashCode()).isNotEqualTo(state2.hashCode());
	}

	@Test
	public void testHashCode_notEqual_differentResultStates() {
		ComposedResultState state1 = new ComposedResultState(ImmutableList.<ResultState> of(ResultState
				.failure("Failure!")), ImmutableList.of(new CommandClass("name", "class")));
		ComposedResultState state2 = new ComposedResultState(ImmutableList.<ResultState> of(ResultState
				.failure("Failure2!")), ImmutableList.of(new CommandClass("name", "class")));
		assertThat(state1.hashCode()).isNotEqualTo(state2.hashCode());
	}

	private static void assertSuccess(ComposedResultState result) {
		assertThat(result.isSuccess()).isTrue();
		assertThat(result.isWarning()).isFalse();
		assertThat(result.isFailure()).isFalse();
	}

	private static void assertWarning(ComposedResultState result) {
		assertThat(result.isSuccess()).isFalse();
		assertThat(result.isWarning()).isTrue();
		assertThat(result.isFailure()).isFalse();
	}

	private static void assertFailure(ComposedResultState result) {
		assertThat(result.isSuccess()).isFalse();
		assertThat(result.isWarning()).isFalse();
		assertThat(result.isFailure()).isTrue();
	}

}
