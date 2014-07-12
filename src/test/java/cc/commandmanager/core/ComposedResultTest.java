package cc.commandmanager.core;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import cc.commandmanager.core.CommandManager.ComposedResult;

public class ComposedResultTest {

	// TODO find proper names for members
	private ComposedResult result;

	@Before
	public void setup() {
		result = new ComposedResult();
	}

	@Test
	public void testGetOverallResult() {
		assertThat(result.getState()).isEqualTo(CommandManager.SimpleState.SUCCESS);
	}

	@Test
	public void testAddResult() {
		result.addResult("Success", ResultState.success());
		assertThat(result.getState()).isEqualTo(CommandManager.SimpleState.SUCCESS);

		result.addResult("Warning", ResultState.warning("Warning!"));
		assertThat(result.getState()).isEqualTo(CommandManager.SimpleState.WARNING);

		result.addResult("Failure", ResultState.failure("Failure!"));
		assertThat(result.getState()).isEqualTo(CommandManager.SimpleState.FAILURE);

		assertThat(result.getPartialResults()).containsExactly(ResultState.success(), ResultState.warning("Warning!"),
				ResultState.failure("Failure!"));
	}

	@Test
	public void testAddResult_warningLevelRemains() {
		result.addResult("Warning", ResultState.warning("Warning!"));
		result.addResult("Success", ResultState.success());
		assertThat(result.getState()).isEqualTo(CommandManager.SimpleState.WARNING);
	}

	@Test
	public void testAddResult_failureLevelRemains() {
		result.addResult("Failure", ResultState.failure("Failure!"));
		result.addResult("Warning", ResultState.warning("Warning!"));
		assertThat(result.getState()).isEqualTo(CommandManager.SimpleState.FAILURE);
	}

	@Test
	public void testGetExecutedCommandNames() {
		result.addResult("Success", ResultState.success());
		result.addResult("Warning", ResultState.warning("Warning!"));
		result.addResult("Failure", ResultState.failure("Failure!"));
		assertThat(result.getExecutedCommandNames()).containsExactly("Success", "Warning", "Failure");
	}

	@Test
	public void testGetPartialResults() {
		result.addResult("Success", ResultState.success());
		result.addResult("Warning", ResultState.warning("Warning!"));
		result.addResult("Failure", ResultState.failure("Failure!"));
		assertThat(result.getPartialResults()).containsExactly(ResultState.success(), ResultState.warning("Warning!"),
				ResultState.failure("Failure!"));
	}

}
