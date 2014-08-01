package cc.commandmanager.core;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.qualitycheck.Check;
import net.sf.qualitycheck.exception.IllegalStateOfArgumentException;

import org.apache.log4j.Logger;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Manages controlled execution of {@linkplain Command}s which are represented by a {@linkplain CommandGraph}. Execution
 * success is reflected by a {@linkplain ComposedResultState}.
 */
public class CommandManager {

	private static final Logger logger = Logger.getLogger(CommandManager.class);

	private final Context context;
	private final CommandGraph commandGraph;

	/**
	 * @param commandGraph
	 *            specifies which commands can be executed. The internal order of this graph will influence
	 *            <ul>
	 *            <li> {@linkplain #executeAllCommands}
	 *            <li> {@linkplain #executeCommandsGracefully} and
	 *            <li> {@linkplain #executeConnectedComponentsContaining} as well as their parameter variations.
	 *            </ul>
	 *            The graph must have at least one command in it.
	 * @throws IllegalStateOfArgumentException
	 *             when the graph is empty.
	 */
	public CommandManager(CommandGraph commandGraph) {
		Check.stateIsTrue(!commandGraph.isEmpty(), "commandGraph must have at least one command in it");
		this.commandGraph = commandGraph;
		context = new Context();
	}

	/**
	 * 
	 * @return {@linkplain CommandGraph} object that was used to build this {@linkplain CommandManager}.
	 */
	public CommandGraph getCommandGraph() {
		return commandGraph;
	}

	/**
	 * Execute all commands that are in the {@linkplain CommandGraph} of this {@linkplain CommandManager}. Respect the
	 * internal order of the graph. It will have no effect on the execution whether a {@link Command} is mandatory or
	 * optional dependent on another one. Execution will be aborted with the first {@linkplain Command} that returns a
	 * {@linkplain ResultState.Failure}. A new context will be created which will be passed to every
	 * {@linkplain Command}.
	 * 
	 * @return {@linkplain ComposedResultState} that reflects the overall success of the just executed
	 *         {@linkplain Command}s.
	 */
	public ComposedResultState executeAllCommands() {
		return executeAllCommands(context);
	}

	/**
	 * Execute all commands that are in the {@linkplain CommandGraph} of this {@linkplain CommandManager}. Respect the
	 * internal order of the graph. It will have no effect on the execution whether a {@link Command} is mandatory or
	 * optional dependent on another one. Execution will be aborted with the first {@linkplain Command} that returns a
	 * {@linkplain ResultState.Failure}.
	 * 
	 * @param context
	 *            will be used as the argument for every executed {@linkplain Command}.
	 * 
	 * @return {@linkplain ComposedResultState} that reflects the overall success of the just executed
	 *         {@linkplain Command}s.
	 */
	public ComposedResultState executeAllCommands(Context context) {
		return executeOrderedCommands(commandGraph.topologicalOrderOfAllCommands(), context);
	}

	/**
	 * Find connected components in which the given command names are included. No command will be executed that is not
	 * at least transitively connected to one of the given commands. Respect the internal order of the found connected
	 * components. It will have no effect on the execution whether a {@link Command} is mandatory or optional dependent
	 * on another one. Execution will be aborted with the first {@linkplain Command} that returns a
	 * {@linkplain ResultState.Failure}. A new context will be created which will be passed to every
	 * {@linkplain Command}.
	 * 
	 * @param commandNames
	 *            names of the {@linkplain Command}s as specified in the {@linkplain CommandClass}es in the underlying
	 *            graph. Must not be empty. However, command names can be empty.
	 * 
	 * @throws {@linkplain CommandNotFoundException} if no command can be found for a given command name in the
	 *         underlying graph.
	 * @throws {@linkplain IllegalStateOfArgumentException} if no command is specified.
	 * 
	 * @return {@linkplain ComposedResultState} that reflects the overall success of the just executed
	 *         {@linkplain Command}s.
	 */
	public ComposedResultState executeConnectedComponentsContaining(Iterable<String> commandNames) {
		return executeConnectedComponentsContaining(commandNames, context);
	}

	/**
	 * Find connected components in which the given command names are included. No command will be executed that is not
	 * at least transitively connected to one of the given commands. Respect the internal order of the found connected
	 * components. It will have no effect on the execution whether a {@link Command} is mandatory or optional dependent
	 * on another one. Execution will be aborted with the first {@linkplain Command} that returns a
	 * {@linkplain ResultState.Failure}.
	 * 
	 * @param commandNames
	 *            names of the {@linkplain Command}s as specified in the {@linkplain CommandClass}es in the underlying
	 *            graph. Must not be empty. However, command names can be empty.
	 * @param context
	 *            will be used as the argument for every executed {@linkplain Command}.
	 * 
	 * @throws {@linkplain CommandNotFoundException} if no command can be found for a given command name in the
	 *         underlying graph.
	 * @throws {@linkplain IllegalStateOfArgumentException} if no command is specified.
	 * 
	 * @return {@linkplain ComposedResultState} that reflects the overall success of the just executed
	 *         {@linkplain Command}s.
	 */
	public ComposedResultState executeConnectedComponentsContaining(Iterable<String> commandNames, Context context) {
		Check.noNullElements(commandNames);

		List<CommandClass> commands = Lists.newLinkedList();
		for (CommandGraph graph : filterConnectedComponentsContaining(Sets.newHashSet(commandNames), commandGraph
				.getConnectedComponents())) {
			commands.addAll(graph.topologicalOrderOfAllCommands());
		}
		return executeOrderedCommands(commands, context);
	}

	private static Set<CommandGraph> filterConnectedComponentsContaining(Set<String> startCommands,
			Set<CommandGraph> connectedComponents) {
		ImmutableSet.Builder<CommandGraph> result = ImmutableSet.builder();
		for (final String command : startCommands) {
			for (CommandGraph connectedComponent : connectedComponents) {
				if (connectedComponent.containsCommand(command)) {
					result.add(connectedComponent);
				}
			}
		}
		return result.build();
	}

	/**
	 * Execute the specified {@linkplain Command}s. Additionally, for every specified {@linkplain Command} find all
	 * {@linkplain Command}s that need to be executed beforehand. I. e., before dependencies will be computed
	 * recursively. Respect the internal order of all gracefully found {@linkplain Command}s. It will have no effect on
	 * the execution whether a {@link Command} is mandatory or optional dependent on another one. Execution will be
	 * aborted with the first {@linkplain Command} that returns a {@linkplain ResultState.Failure}. A new context will
	 * be created which will be passed to every {@linkplain Command}.
	 * 
	 * @param commandNames
	 *            names of the {@linkplain Command}s as specified in the {@linkplain CommandClass}es in the underlying
	 *            graph. Must not be empty. However, command names can be empty.
	 * 
	 * @throws {@linkplain CommandNotFoundException} if no command can be found for a given command name in the
	 *         underlying graph.
	 * @throws {@linkplain IllegalStateOfArgumentException} if no command is specified.
	 * 
	 * @return {@linkplain ComposedResultState} that reflects the overall success of the just executed
	 *         {@linkplain Command}s.
	 * 
	 */
	public ComposedResultState executeCommandsGracefully(String... commandNames) {
		return executeCommandsGracefully(context, commandNames);
	}

	/**
	 * Execute the specified {@linkplain Command}s. Additionally, for every specified {@linkplain Command} find all
	 * {@linkplain Command}s that need to be executed beforehand. I. e., before dependencies will be computed
	 * recursively. Respect the internal order of all gracefully found {@linkplain Command}s. It will have no effect on
	 * the execution whether a {@link Command} is mandatory or optional dependent on another one. Execution will be
	 * aborted with the first {@linkplain Command} that returns a {@linkplain ResultState.Failure}.
	 * 
	 * @param commandNames
	 *            names of the {@linkplain Command}s as specified in the {@linkplain CommandClass}es in the underlying
	 *            graph. Must not be empty. However, command names can be empty.
	 * @param context
	 *            will be used as the argument for every executed {@linkplain Command}.
	 * 
	 * @throws {@linkplain CommandNotFoundException} if no command can be found for a given command name in the
	 *         underlying graph.
	 * @throws {@linkplain IllegalStateOfArgumentException} if no command is specified.
	 * 
	 * @return {@linkplain ComposedResultState} that reflects the overall success of the just executed
	 *         {@linkplain Command}s.
	 * 
	 */
	public ComposedResultState executeCommandsGracefully(Context context, String... commandNames) {
		return executeCommandsGracefully(Arrays.asList(commandNames), context);
	}

	/**
	 * Execute the specified {@linkplain Command}s. Additionally, for every specified {@linkplain Command} find all
	 * {@linkplain Command}s that need to be executed beforehand. I. e., before dependencies will be computed
	 * recursively. Respect the internal order of all gracefully found {@linkplain Command}s. It will have no effect on
	 * the execution whether a {@link Command} is mandatory or optional dependent on another one. Execution will be
	 * aborted with the first {@linkplain Command} that returns a {@linkplain ResultState.Failure}. A new context will
	 * be created which will be passed to every {@linkplain Command}.
	 * 
	 * @param commandNames
	 *            names of the {@linkplain Command}s as specified in the {@linkplain CommandClass}es in the underlying
	 *            graph. Must not be empty. However, command names can be empty.
	 * 
	 * @throws {@linkplain CommandNotFoundException} if no command can be found for a given command name in the
	 *         underlying graph.
	 * @throws {@linkplain IllegalStateOfArgumentException} if no command is specified.
	 * 
	 * @return {@linkplain ComposedResultState} that reflects the overall success of the just executed
	 *         {@linkplain Command}s.
	 * 
	 */
	public ComposedResultState executeCommandsGracefully(Iterable<String> commandNames) {
		return executeCommandsGracefully(commandNames, context);
	}

	/**
	 * Execute the specified {@linkplain Command}s. Additionally, for every specified {@linkplain Command} find all
	 * {@linkplain Command}s that need to be executed beforehand. I. e., before dependencies will be computed
	 * recursively. Respect the internal order of all gracefully found {@linkplain Command}s. It will have no effect on
	 * the execution whether a {@link Command} is mandatory or optional dependent on another one. Execution will be
	 * aborted with the first {@linkplain Command} that returns a {@linkplain ResultState.Failure}.
	 * 
	 * @param commandNames
	 *            names of the {@linkplain Command}s as specified in the {@linkplain CommandClass}es in the underlying
	 *            graph. Must not be empty. However, command names can be empty.
	 * @param context
	 *            will be used as the argument for every executed {@linkplain Command}.
	 * 
	 * @throws {@linkplain CommandNotFoundException} if no command can be found for a given command name in the
	 *         underlying graph.
	 * @throws {@linkplain IllegalStateOfArgumentException} if no command is specified.
	 * 
	 * @return {@linkplain ComposedResultState} that reflects the overall success of the just executed
	 *         {@linkplain Command}s.
	 * 
	 */
	public ComposedResultState executeCommandsGracefully(Iterable<String> commandNames, Context context) {
		Check.noNullElements(commandNames);
		List<CommandClass> commands = Lists.newLinkedList();
		for (String commandName : commandNames) {
			commands.add(commandGraph.getCommandClass(commandName));
		}

		Set<CommandClass> commandsAndTheirDependencies = Sets.newHashSet(commands);
		for (CommandClass command : commands) {
			commandsAndTheirDependencies.addAll(successiveBeforeDependencies(command, new HashSet<CommandClass>()));
		}
		return executeOrderedCommands(commandGraph.topologicalOrderOf(commandsAndTheirDependencies), context);
	}

	private Set<CommandClass> successiveBeforeDependencies(CommandClass command, Set<CommandClass> accumulator) {
		Set<CommandClass> result = Sets.newHashSet(accumulator);
		List<CommandClass> dependencies = commandGraph.getDependencies(command.getName());
		result.addAll(dependencies);
		for (CommandClass dependency : dependencies) {
			result.addAll(successiveBeforeDependencies(dependency, result));
		}
		return result;
	}

	/**
	 * In contrast to {@linkplain #executeCommandsGracefully} this method will ignore before dependencies. I. e, only
	 * the specified {@linkplain Command}s will be executed. Respect the internal order of the specified
	 * {@linkplain Command}s. It will have no effect on the execution whether a {@link Command} is mandatory or optional
	 * dependent on another one. Execution will be aborted with the first {@linkplain Command} that returns a
	 * {@linkplain ResultState.Failure}. A new context will be created which will be passed to every
	 * {@linkplain Command}.
	 * 
	 * @param commandNames
	 *            names of the {@linkplain Command}s as specified in the {@linkplain CommandClass}es in the underlying
	 *            graph. Must not be empty. However, command names can be empty.
	 * 
	 * @throws {@linkplain CommandNotFoundException} if no command can be found for a given command name in the
	 *         underlying graph.
	 * @throws {@linkplain IllegalStateOfArgumentException} if no command is specified.
	 * 
	 * @return {@linkplain ComposedResultState} that reflects the overall success of the just executed
	 *         {@linkplain Command}s.
	 * 
	 */
	public ComposedResultState executeCommands(String... commandNames) {
		return executeCommands(context, commandNames);
	}

	/**
	 * In contrast to {@linkplain #executeCommandsGracefully} this method will ignore before dependencies. I. e, only
	 * the specified {@linkplain Command}s will be executed. Respect the internal order of the specified
	 * {@linkplain Command}s. It will have no effect on the execution whether a {@link Command} is mandatory or optional
	 * dependent on another one. Execution will be aborted with the first {@linkplain Command} that returns a
	 * {@linkplain ResultState.Failure}.
	 * 
	 * @param commandNames
	 *            names of the {@linkplain Command}s as specified in the {@linkplain CommandClass}es in the underlying
	 *            graph. Must not be empty. However, command names can be empty.
	 * @param context
	 *            will be used as the argument for every executed {@linkplain Command}.
	 * 
	 * @throws {@linkplain CommandNotFoundException} if no command can be found for a given command name in the
	 *         underlying graph.
	 * @throws {@linkplain IllegalStateOfArgumentException} if no command is specified.
	 * 
	 * @return {@linkplain ComposedResultState} that reflects the overall success of the just executed
	 *         {@linkplain Command}s.
	 * 
	 */
	public ComposedResultState executeCommands(Context context, String... commandNames) {
		return executeCommands(Arrays.asList(commandNames), context);
	}

	/**
	 * In contrast to {@linkplain #executeCommandsGracefully} this method will ignore before dependencies. I. e, only
	 * the specified {@linkplain Command}s will be executed. Respect the internal order of the specified
	 * {@linkplain Command}s. It will have no effect on the execution whether a {@link Command} is mandatory or optional
	 * dependent on another one. Execution will be aborted with the first {@linkplain Command} that returns a
	 * {@linkplain ResultState.Failure}. A new context will be created which will be passed to every
	 * {@linkplain Command}.
	 * 
	 * @param commandNames
	 *            names of the {@linkplain Command}s as specified in the {@linkplain CommandClass}es in the underlying
	 *            graph. Must not be empty. However, command names can be empty.
	 * 
	 * @throws {@linkplain CommandNotFoundException} if no command can be found for a given command name in the
	 *         underlying graph.
	 * @throws {@linkplain IllegalStateOfArgumentException} if no command is specified.
	 * 
	 * @return {@linkplain ComposedResultState} that reflects the overall success of the just executed
	 *         {@linkplain Command}s.
	 * 
	 */
	public ComposedResultState executeCommands(Iterable<String> commandNames) {
		return executeCommands(commandNames, context);
	}

	/**
	 * In contrast to {@linkplain #executeCommandsGracefully} this method will ignore before dependencies. I. e, only
	 * the specified {@linkplain Command}s will be executed. Respect the internal order of the specified
	 * {@linkplain Command}s. It will have no effect on the execution whether a {@link Command} is mandatory or optional
	 * dependent on another one. Execution will be aborted with the first {@linkplain Command} that returns a
	 * {@linkplain ResultState.Failure}.
	 * 
	 * @param commandNames
	 *            names of the {@linkplain Command}s as specified in the {@linkplain CommandClass}es in the underlying
	 *            graph. Must not be empty. However, command names can be empty.
	 * @param context
	 *            will be used as the argument for every executed {@linkplain Command}.
	 * 
	 * @throws {@linkplain CommandNotFoundException} if no command can be found for a given command name in the
	 *         underlying graph.
	 * @throws {@linkplain IllegalStateOfArgumentException} if no command is specified.
	 * 
	 * @return {@linkplain ComposedResultState} that reflects the overall success of the just executed
	 *         {@linkplain Command}s.
	 * 
	 */
	public ComposedResultState executeCommands(Iterable<String> commandNames, Context context) {
		Check.noNullElements(commandNames, "commandNames");
		return executeOrderedCommands(commandGraph.topologicalOrderOfNames(commandNames), context);
	}

	/**
	 * Execute all commands that are in the given {@linkplain CommandGraph}. Respect the internal order of the graph. It
	 * will have no effect on the execution whether a {@link Command} is mandatory or optional dependent on another one.
	 * Execution will be aborted with the first {@linkplain Command} that returns a {@linkplain ResultState.Failure}. A
	 * new context will be created which will be passed to every {@linkplain Command}. This is short for creating a new
	 * {@linkplain CommandManager} with this {@linkplain CommandGraph} and calling {@linkplain #executeAllCommands}
	 * afterwards.
	 * 
	 * @param graph
	 *            that is used to obtain all {@linkplain Command}s that can be executed.
	 * 
	 * @throws IllegalStateOfArgumentException
	 *             when the graph is empty.
	 * 
	 * @return {@linkplain ComposedResultState} that reflects the overall success of the just executed
	 *         {@linkplain Command}s.
	 */
	public static ComposedResultState executeCommands(CommandGraph graph) {
		return executeCommands(graph, new Context());
	}

	/**
	 * Execute all commands that are in the given {@linkplain CommandGraph}. Respect the internal order of the graph. It
	 * will have no effect on the execution whether a {@link Command} is mandatory or optional dependent on another one.
	 * Execution will be aborted with the first {@linkplain Command} that returns a {@linkplain ResultState.Failure}.
	 * This is short for creating a new {@linkplain CommandManager} with this {@linkplain CommandGraph} and calling
	 * {@linkplain #executeAllCommands} afterwards.
	 * 
	 * @param context
	 *            will be used as the argument for every executed {@linkplain Command}.
	 * 
	 * @throws IllegalStateOfArgumentException
	 *             when the graph is empty.
	 * 
	 * @return {@linkplain ComposedResultState} that reflects the overall success of the just executed
	 *         {@linkplain Command}s.
	 */
	public static ComposedResultState executeCommands(CommandGraph graph, Context context) {
		Check.notNull(graph, "graph");
		Check.stateIsTrue(!graph.isEmpty(), "graph must have at least one command in it");
		return executeOrderedCommands(graph.topologicalOrderOfAllCommands(), context);
	}

	/**
	 * Executes the given ordered commands using the specified context.
	 * 
	 * @param commands
	 * @param context
	 * @return whether the execution was successful
	 */
	private static ComposedResultState executeOrderedCommands(Iterable<CommandClass> commands, Context context) {
		Check.noNullElements(commands, "commandNames");
		Check.stateIsTrue(!Iterables.isEmpty(commands), "commandNames must have at least one command name");
		Check.notNull(context, "context");

		ImmutableList.Builder<ResultState> resultStates = ImmutableList.builder();
		ImmutableList.Builder<CommandClass> executedCommands = ImmutableList.builder();
		for (CommandClass command : commands) {
			Command commandInstance = command.newInstance();
			logger.info("Execute current command: " + commandInstance.getClass());
			long startTime = System.currentTimeMillis();
			ResultState resultState = commandInstance.execute(context);
			resultStates.add(resultState);
			executedCommands.add(command);
			if (resultState.isSuccess()) {
				logger.info("Command " + commandInstance.getClass() + " successfully executed in "
						+ (System.currentTimeMillis() - startTime) + " ms");
			} else if (resultState.isWarning()) {
				logger.warn("Command " + commandInstance.getClass() + " executed with warning in "
						+ (System.currentTimeMillis() - startTime) + " ms: " + resultState.getMessage() + " "
						+ resultState.getCause());
			} else {
				logger.error("Command " + commandInstance.getClass() + " failed to execute (took "
						+ (System.currentTimeMillis() - startTime) + " ms): " + resultState.getMessage() + " "
						+ resultState.getCause());
				logger.error("Aborting execution of all commands.");
				break;
			}
		}
		return new ComposedResultState(resultStates.build(), executedCommands.build());
	}

}
