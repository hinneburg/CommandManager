package cc.commandmanager.core;

import static org.fest.assertions.Assertions.assertThat;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import cc.commandmanager.core.CommandGraph.CommandGraphBuilder;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class CommandManagerTest {

	private CommandManager commandManager;

	@Before
	public void setUpCommandManagement() {
		CommandGraphBuilder builder = new CommandGraphBuilder();
		builder.addCommand("Success", SuccessfulCommand.class.getName());
		builder.addCommand("Warning", WarningCommand.class.getName());
		builder.addCommand("Failure", FailingCommand.class.getName());
		commandManager = new CommandManager(builder.build());
	}

	@Test
	public void testGetOrderedCommandsFromStartCommands() {
		Map<String, Set<String>> expected = Maps.newHashMap();
		expected.put("Success", new HashSet<String>());
		expected.put("Warning", new HashSet<String>());
		expected.put("Failure", new HashSet<String>());
		assertThat(
				commandManager.getOrderedCommands(Sets.newHashSet("Success", "Warning", "Failure"),
						new HashSet<String>())).containsExactly("Success", "Warning", "Failure");

		assertThat(commandManager.getOrderedCommands(Sets.newHashSet("Success"), Collections.<String> emptySet()))
				.containsExactly("Success");
	}

	@Test
	public void testGetOrderedCommands_focusOnConnectedComponent() {
		CommandGraphBuilder builder = new CommandGraphBuilder();
		builder.addCommand("Single", "single.ClassName");
		builder.addCommand("Connected1", "connected1.ClassName");
		builder.addCommand("Connected2", "connected2.ClassName");
		builder.addCommand("Connected3", "connected3.ClassName");
		builder.addMandatoryDependency("Connected3", "Connected2");
		builder.addMandatoryDependency("Connected2", "Connected1");

		List<String> orderedCommands = new CommandManager(builder.build()).getOrderedCommands(Sets
				.newHashSet("Connected1"), Collections.<String> emptySet());

		assertThat(orderedCommands).excludes("Single").doesNotHaveDuplicates();
	}

	@Test
	public void testGetOrderedCommands_wholeGraphGetsOrderedOnEmptyParameters() {
		assertThat(commandManager.getOrderedCommands(Collections.<String> emptySet(), Collections.<String> emptySet()))
				.contains("Success", "Warning", "Failure");
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
