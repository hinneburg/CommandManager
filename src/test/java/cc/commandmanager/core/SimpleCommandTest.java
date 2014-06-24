package cc.commandmanager.core;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

public class SimpleCommandTest {

	private SimpleCommand simpleCommand;

	@Before
	public void setUp() {
		simpleCommand = new SimpleCommandDummyImplementation();
	}

	@Test
	public void testGetAfterDependencies() {
		assertThat(simpleCommand.getAfterDependencies()).isEmpty();
	}

	@Test
	public void testGetBeforeDependencies() {
		assertThat(simpleCommand.getBeforeDependencies()).isEmpty();
	}

	@Test
	public void testGetOptionalAfterDependencies() {
		assertThat(simpleCommand.getOptionalAfterDependencies()).isEmpty();
	}

	@Test
	public void testGetOptionalBeforeDependencies() {
		assertThat(simpleCommand.getOptionalBeforeDependencies()).isEmpty();

	}

	private static class SimpleCommandDummyImplementation extends SimpleCommand {

		@Override
		public ResultState execute(Context context) {
			return ResultState.success();
		}

	}

}
