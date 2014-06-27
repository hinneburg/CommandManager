package cc.commandmanager.core;

import static org.fest.assertions.Assertions.assertThat;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import cc.commandmanager.catalog.Catalog;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class CommandManagerTest {

	private CommandManager commandManager;

	@Before
	public void setUpCommandManagement() {
		Map<String, Class<? extends Command>> commands = Maps.newHashMap();
		commands.put("Success", SuccessfulCommand.class);
		commands.put("Warning", WarningCommand.class);
		commands.put("Failure", FailingCommand.class);
		Catalog catalog = Catalog.fromMap(commands);
		commandManager = new CommandManager(catalog);
	}

	@Test
	public void testGetDependencies() {

	}

	@Test(expected = NullPointerException.class)
	public void testDependencies_NPE() {
		commandManager.getDependencies();
	}

	@Test
	public void testGetOrderedCommandsFromDependenciesAndStartCommands() {

	}

	@Test
	public void testGetOrderedCommandsFromStartAndEndCommands() {
		Map<String, Set<String>> expected = Maps.newHashMap();
		expected.put("Success", new HashSet<String>());
		expected.put("Warning", new HashSet<String>());
		expected.put("Failure", new HashSet<String>());
		assertThat(
				commandManager.getOrderedCommands(Sets.newHashSet("Success", "Warning", "Failure"),
						new HashSet<String>())).containsOnly("Success", "Warning", "Failure").doesNotHaveDuplicates();

		assertThat(commandManager.getOrderedCommands(Sets.newHashSet("Success"), new HashSet<String>()))
				.containsExactly("Success");
	}

	@Test
	public void testExecutionAbortionOnFailure() {
		// TODO only way of doing this until #41 is implemented and returns the result state
		commandManager.executeCommands(ImmutableList.of("Success", "Warning", "Failure"));
		assertThat(SuccessfulCommand.isExecuted).isTrue();
		assertThat(WarningCommand.isExecuted).isTrue();
		assertThat(FailingCommand.isExecuted).isTrue();
		SuccessfulCommand.isExecuted = false;
		WarningCommand.isExecuted = false;
		FailingCommand.isExecuted = false;

		commandManager.executeCommands(ImmutableList.of("Success", "Failure", "Warning"));
		assertThat(SuccessfulCommand.isExecuted).isTrue();
		assertThat(FailingCommand.isExecuted).isTrue();
		assertThat(WarningCommand.isExecuted).isFalse();
		SuccessfulCommand.isExecuted = false;
		WarningCommand.isExecuted = false;
		FailingCommand.isExecuted = false;
	}

	public static class SuccessfulCommand extends SimpleCommand {

		static boolean isExecuted = false;

		@Override
		public ResultState execute(Context context) {
			isExecuted = true;
			return ResultState.success();
		}
	}

	public static class WarningCommand extends SimpleCommand {

		static boolean isExecuted = false;

		@Override
		public ResultState execute(Context context) {
			isExecuted = true;
			return ResultState.warning("Warning!");
		}

	}

	public static class FailingCommand extends SimpleCommand {

		static boolean isExecuted = false;

		@Override
		public ResultState execute(Context context) {
			isExecuted = true;
			return ResultState.failure("Fail!");
		}

	}

}
