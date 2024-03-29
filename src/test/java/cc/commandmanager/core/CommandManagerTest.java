package cc.commandmanager.core;

import static cc.commandmanager.testutils.Assertions.assertThatExecution;
import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;
import net.sf.qualitycheck.exception.IllegalStateOfArgumentException;

import org.junit.Before;
import org.junit.Test;

import cc.commandmanager.core.CommandGraph.CommandGraphBuilder;

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
		assertThat(commandManager.executeAllCommands().isFailure()).isTrue();
	}

	@Test
	public void testExecuteCommands() {
		assertThat(commandManager.executeCommands(Lists.newArrayList("Success", "Warning", "Failure")).isFailure())
				.isTrue();
	}

	@Test
	public void testExecuteCommandsRespectsGraphOrder() {
		CommandGraphBuilder builder = new CommandGraphBuilder();
		builder.addCommand("1", SuccessfulCommand.class.getName());
		builder.addCommand("2", FailingCommand.class.getName());
		builder.addMandatoryDependency("2", "1");
		commandManager = new CommandManager(builder.build());

		assertThat(commandManager.executeCommands(Lists.newArrayList("1", "2")).getResultStates()).containsExactly(
				ResultState.success(), ResultState.failure("Fail!"));

		CommandGraphBuilder backwards = new CommandGraphBuilder();
		backwards.addCommand("1", SuccessfulCommand.class.getName());
		backwards.addCommand("2", FailingCommand.class.getName());
		backwards.addMandatoryDependency("1", "2");
		commandManager = new CommandManager(backwards.build());

		assertThat(commandManager.executeCommands(Lists.newArrayList("1", "2")).getResultStates()).containsOnly(
				ResultState.failure("Fail!"));
	}

	@Test
	public void testExecuteConnectedComponentsContaining() {
		assertThat(commandManager.executeConnectedComponentsContaining(Lists.newArrayList("Success")).getResultStates())
				.containsOnly(ResultState.success(), ResultState.warning("Warning!"), ResultState.failure("Fail!"));
	}

	@Test
	public void testExecuteCommandsGracefully() {
		assertThat(commandManager.executeCommandsGracefully("Warning").getResultStates()).containsExactly(
				ResultState.success(), ResultState.warning("Warning!"));

		CommandGraphBuilder builder = new CommandGraphBuilder();
		builder.addCommand("Independent", SuccessfulCommand.class.getName());
		builder.addCommand("From one another", SuccessfulCommand.class.getName());
		assertThat(new CommandManager(builder.build()).executeCommandsGracefully("Independent").getExecutedCommands())
				.containsOnly(new CommandClass("Independent", SuccessfulCommand.class.getName()));
	}

	@Test
	public void testExecuteCommandsGracefully_dependenciesNotExecutedTwice() {
		CommandGraphBuilder commandGraphBuilder = CommandGraph.builder();
		commandGraphBuilder.addCommand("GracefulDependency", GracefulDependency.class.getName());
		commandGraphBuilder.addCommand("Dependee1", DummyCommand.class.getName());
		commandGraphBuilder.addCommand("Dependee2", DummyCommand.class.getName());
		commandGraphBuilder.addMandatoryDependency("Dependee1", "GracefulDependency");
		commandGraphBuilder.addMandatoryDependency("Dependee2", "GracefulDependency");

		CommandManager commandManager = new CommandManager(commandGraphBuilder.build());

		assertThatExecution(commandManager.executeCommandsGracefully("Dependee1", "Dependee2"))
				.isCompletedSuccessfully();
	}

	public static class GracefulDependency extends SimpleCommand {

		@Override
		public ResultState execute(Context context) {
			String commandName = GracefulDependency.class.getName();
			if (context.containsKey(commandName)) {
				fail(String.format("Command %s executed twice.", commandName));
			}
			context.bind(commandName, Boolean.TRUE);
			return ResultState.success();
		}

	}

	@Test
	public void testExecuteCommandsFromGraph() {
		CommandGraphBuilder builder = new CommandGraphBuilder();
		builder.addCommand("A1", SuccessfulCommand.class.getName());
		builder.addCommand("A2", SuccessfulCommand.class.getName());
		builder.addCommand("B", SuccessfulCommand.class.getName());
		builder.addMandatoryDependency("A2", "A1");

		assertThat(CommandManager.executeCommands(builder.build()).getExecutedCommands()).containsSequence(
				new CommandClass("A1", SuccessfulCommand.class.getName()),
				new CommandClass("A2", SuccessfulCommand.class.getName())).contains(
				new CommandClass("B", SuccessfulCommand.class.getName()));
	}

	@Test(expected = CommandNotFoundException.class)
	public void testExecuteCommands_commandNotFound() {
		commandManager.executeCommands(Lists.newArrayList("Missing"));
	}

	@Test(expected = IllegalStateOfArgumentException.class)
	public void testExecuteCommands_noCommandSpecified() {
		commandManager.executeCommands(Lists.<String> newArrayList());
	}

	@Test
	public void testExecuteCommandsDoesNotExecuteBeforeDependenciesAutomatically() {
		CommandGraphBuilder builder = new CommandGraphBuilder();
		builder.addCommand("Success", SuccessfulCommand.class.getName());
		builder.addCommand("Warning", WarningCommand.class.getName());
		builder.addMandatoryDependency("Success", "Warning");
		commandManager = new CommandManager(builder.build());

		assertThatExecution(commandManager.executeCommands(Lists.newArrayList("Success"))).isCompletedSuccessfully();
	}

	@Test
	public void testExecutionAbortionOnFailure() {
		CommandGraphBuilder builder = new CommandGraphBuilder();
		builder.addCommand("Success", SuccessfulCommand.class.getName());
		builder.addCommand("Warning", WarningCommand.class.getName());
		builder.addCommand("Failure", FailingCommand.class.getName());
		builder.addMandatoryDependency("Success", "Warning");
		builder.addMandatoryDependency("Warning", "Failure");
		commandManager = new CommandManager(builder.build());

		assertThat(commandManager.executeCommands(ImmutableList.of("Success", "Warning")).getResultStates())
				.containsOnly(ResultState.success(), ResultState.warning("Warning!"));

		assertThat(commandManager.executeCommands(ImmutableList.of("Success", "Warning", "Failure")).getResultStates())
				.containsOnly(ResultState.failure("Fail!"));
	}

	@Test(expected = IllegalStateOfArgumentException.class)
	public void testConstructorOnEmptyGraph() {
		new CommandManager(new CommandGraphBuilder().build());
	}

	@Test(expected = IllegalStateOfArgumentException.class)
	public void testExecuteCommandsOnEmptyGraph() {
		CommandManager.executeCommands(new CommandGraphBuilder().build());
	}

	public static class SuccessfulCommand extends SimpleCommand {

		@Override
		public ResultState execute(Context context) {
			return ResultState.success();
		}
	}

	public static class WarningCommand extends SimpleCommand {

		@Override
		public ResultState execute(Context context) {
			return ResultState.warning("Warning!");
		}

	}

	public static class FailingCommand extends SimpleCommand {

		@Override
		public ResultState execute(Context context) {
			return ResultState.failure("Fail!");
		}

	}

}
