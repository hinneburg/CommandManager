package cc.commandmanager.core;

import static org.fest.assertions.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import cc.commandmanager.core.CommandManager.ComposedResult;

public class CmResultTest {

	// TODO find proper names for members
	private ComposedResult result;

	@Before
	public void setup() {
		result = new ComposedResult();
	}

	@Test
	public void testGetOverallResult() {
		assertThat(result.getComposedResult()).isEqualTo(CommandManager.ResultState2.SUCCESS);
	}

	@Test
	public void testAddResult() {
		result.addResult("Success", ResultState.success());
		assertThat(result.getComposedResult()).isEqualTo(CommandManager.ResultState2.SUCCESS);

		result.addResult("Warning", ResultState.warning("Warning!"));
		assertThat(result.getComposedResult()).isEqualTo(CommandManager.ResultState2.WARNING);

		result.addResult("Failure", ResultState.failure("Failure!"));
		assertThat(result.getComposedResult()).isEqualTo(CommandManager.ResultState2.FAILURE);

		Map<String, ResultState> commandResults = new LinkedHashMap<String, ResultState>();
		commandResults.put("Success", ResultState.success());
		commandResults.put("Warning", ResultState.warning("Warning!"));
		commandResults.put("Failure", ResultState.failure("Failure!"));
		assertThat(result.getPartialResults()).isEqualTo(commandResults);
	}

	@Test
	public void testAddResult_warningLevelRemains() {
		result.addResult("Warning", ResultState.warning("Warning!"));
		result.addResult("Success", ResultState.success());
		assertThat(result.getComposedResult()).isEqualTo(CommandManager.ResultState2.WARNING);
	}

	@Test
	public void testAddResult_failureLevelRemains() {
		result.addResult("Failure", ResultState.failure("Failure!"));
		result.addResult("Warning", ResultState.warning("Warning!"));
		assertThat(result.getComposedResult()).isEqualTo(CommandManager.ResultState2.FAILURE);
	}

	@Test
	public void testGetCommandResults() {

	}

}
