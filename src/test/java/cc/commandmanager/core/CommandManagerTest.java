package cc.commandmanager.core;

import static org.fest.assertions.Assertions.assertThat;
import net.sf.qualitycheck.exception.IllegalStateOfArgumentException;

import org.junit.Before;
import org.junit.Test;

import cc.commandmanager.core.CommandGraph.CommandGraphBuilder;
import cc.commandmanager.core.CommandManager.ResultState2;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class CommandManagerTest {

	private CommandManager commandManager;

	@Before
	public void setUpCommandManagement() {
		CommandGraphBuilder builder = new CommandGraphBuilder();
		builder.addCommand("Success", SuccessfulCommand.class.getName());
		builder.addCommand("Warning", WarningCommand.class.getName());
		builder.addCommand("Failure", FailingCommand.class.getName());
		builder.addMandatoryDependency("Failure", "Warning");
		builder.addMandatoryDependency("Warning", "Success");
		commandManager = new CommandManager(builder.build());
	}

	@Test
	public void testExecuteAllCommands() {
		assertThat(commandManager.executeAllCommands().getComposedResult()).isEqualTo(ResultState2.FAILURE);
	}

	@Test
	public void testExecuteCommands() {
		assertThat(
				commandManager.executeCommands(Lists.newArrayList("Success", "Warning", "Failure")).getComposedResult())
				.isEqualTo(ResultState2.FAILURE);
	}

	@Test
	public void testExecuteCommandsRespectsGraphOrder() {
		CommandGraphBuilder builder = new CommandGraphBuilder();
		builder.addCommand("1", SuccessfulCommand.class.getName());
		builder.addCommand("2", FailingCommand.class.getName());
		builder.addMandatoryDependency("2", "1");
		commandManager = new CommandManager(builder.build());

		assertThat(commandManager.executeCommands(Lists.newArrayList("1", "2")).getPartialResults()).containsExactly(
				ResultState.success(), ResultState.failure("Fail!"));

		CommandGraphBuilder backwards = new CommandGraphBuilder();
		backwards.addCommand("1", SuccessfulCommand.class.getName());
		backwards.addCommand("2", FailingCommand.class.getName());
		backwards.addMandatoryDependency("1", "2");
		commandManager = new CommandManager(backwards.build());

		assertThat(commandManager.executeCommands(Lists.newArrayList("1", "2")).getPartialResults()).containsOnly(
				ResultState.failure("Fail!"));
	}

	@Test
	public void testExecuteConnectedComponentsContaining() {
		assertThat(
				commandManager.executeConnectedComponentsContaining(Lists.newArrayList("Success")).getPartialResults())
				.containsOnly(ResultState.success(), ResultState.warning("Warning!"), ResultState.failure("Fail!"));
	}

	@Test
	public void testExecuteCommandsGracefully() {
		assertThat(commandManager.executeCommandsGracefully("Warning").getPartialResults()).containsExactly(
				ResultState.success(), ResultState.warning("Warning!"));
	}

	@Test
	public void testExecuteCommandsFromGraph() {
		CommandGraphBuilder builder = new CommandGraphBuilder();
		builder.addCommand("A1", SuccessfulCommand.class.getName());
		builder.addCommand("A2", SuccessfulCommand.class.getName());
		builder.addCommand("B", SuccessfulCommand.class.getName());
		builder.addMandatoryDependency("A2", "A1");

		assertThat(CommandManager.executeCommands(builder.build()).getExecutedCommandNames()).containsSequence("A1",
				"A2").contains("B");
	}

	@Test(expected = CommandNotFoundException.class)
	public void testExecuteCommands_commandNotFound() {
		commandManager.executeCommands(Lists.newArrayList("Missing"));
	}

	@Test(expected = IllegalStateOfArgumentException.class)
	public void testExecuteAtLeastOnCommand() {
		commandManager.executeCommands(Lists.<String> newArrayList());
	}

	@Test
	public void testExecuteCommands_ignoresUnspecifiedDependencies() {
		CommandGraphBuilder builder = new CommandGraphBuilder();
		builder.addCommand("Misses dependency", SuccessfulCommand.class.getName());
		builder.addCommand("Warning dependency", WarningCommand.class.getName());
		builder.addMandatoryDependency("Misses dependency", "Warning dependency");
		commandManager = new CommandManager(builder.build());

		assertThat(commandManager.executeCommands(Lists.newArrayList("Misses dependency")).getComposedResult())
				.isEqualTo(ResultState2.SUCCESS);
	}

	@Test
	public void testExecutionAbortionOnFailure() {
		// TODO only way of doing this until #41 is implemented and returns the result state
		CommandGraphBuilder builder = new CommandGraphBuilder();
		builder.addCommand("Success", SuccessfulCommand.class.getName());
		builder.addCommand("Warning", WarningCommand.class.getName());
		builder.addCommand("Failure", FailingCommand.class.getName());
		builder.addMandatoryDependency("Success", "Warning");
		builder.addMandatoryDependency("Warning", "Failure");
		commandManager = new CommandManager(builder.build());

		assertThat(commandManager.executeCommands(ImmutableList.of("Success", "Warning")).getPartialResults())
				.containsOnly(ResultState.success(), ResultState.warning("Warning!"));

		assertThat(
				commandManager.executeCommands(ImmutableList.of("Success", "Warning", "Failure")).getPartialResults())
				.containsOnly(ResultState.failure("Fail!"));
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
