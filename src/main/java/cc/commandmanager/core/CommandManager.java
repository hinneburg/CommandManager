package cc.commandmanager.core;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.sf.qualitycheck.Check;
import net.sf.qualitycheck.exception.IllegalStateOfArgumentException;

import org.apache.log4j.Logger;

import cc.commandmanager.core.ResultState.Failure;
import cc.commandmanager.core.ResultState.Warning;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Manages controlled execution of {@linkplain Command}s which are represented by a {@linkplain CommandGraph}. Execution
 * success is reflected by a {@linkplain ComposedResult}.
 */
public class CommandManager {
	private static final Logger logger = Logger.getLogger(CommandManager.class);

	private final Context context;
	private final CommandGraph commandGraph;

	/**
	 * Create a new {@linkplain CommandManager}. Use the {@linkplain CommandGraph}, parsed from the given XML file. The
	 * command graph specifies which commands which will be executed and in which order this will happen. A new
	 * {@linkplain Context} will be used to execute the commands with.
	 * 
	 * @param catalog
	 *            see {@linkplain CommandGraph#fromFile(File))} for specifications on the catalog file.
	 */
	public CommandManager(File catalog) {
		this(catalog, new Context());
	}

	/**
	 * Create a new {@linkplain CommandManager}. Use the {@linkplain CommandGraph}, parsed from the given XML file. The
	 * command graph specifies which commands which will be executed and in which order this will happen.
	 * 
	 * @param catalog
	 *            see {@linkplain CommandGraph#fromFile(File))} for specifications on the catalog file.
	 * @param context
	 *            information in the context will be used to execute the commands with.
	 */
	public CommandManager(File catalog, Context context) {
		this(CommandGraph.fromXml(catalog).get(), context);
	}

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
		Check.stateIsTrue(!commandGraph.topologicalOrderOfAllCommands().isEmpty(),
				"commandGraph must have at least one command in it");
		this.commandGraph = commandGraph;
		context = new Context();
	}

	/**
	 * Create a new {@linkplain CommandManager}.
	 * 
	 * @param commandGraph
	 *            specifies which commands which will be executed and in which order this will happen.
	 * @param context
	 *            information in the context will be used to execute the commands with.
	 */
	public CommandManager(CommandGraph commandGraph, Context context) {
		Check.stateIsTrue(!commandGraph.topologicalOrderOfAllCommands().isEmpty(),
				"commandGraph must have at least one command in it");
		this.commandGraph = commandGraph;
		this.context = Check.notNull(context, "context");
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
	 * @return {@linkplain ComposedResult} that reflects the overall success of the just executed {@linkplain Command}s.
	 */
	public ComposedResult executeAllCommands() {
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
	 * @return {@linkplain ComposedResult} that reflects the overall success of the just executed {@linkplain Command}s.
	 */
	public ComposedResult executeAllCommands(Context context) {
		return executeOrderedCommands(commandGraph.topologicalOrderOfAllCommands(), context, commandGraph);
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
	 * @return {@linkplain ComposedResult} that reflects the overall success of the just executed {@linkplain Command}s.
	 */
	public ComposedResult executeConnectedComponentsContaining(Iterable<String> commandNames) {
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
	 * @return {@linkplain ComposedResult} that reflects the overall success of the just executed {@linkplain Command}s.
	 */
	public ComposedResult executeConnectedComponentsContaining(Iterable<String> commandNames, Context context) {
		Check.noNullElements(commandNames);

		List<CommandClass> commands = Lists.newLinkedList();
		for (CommandGraph graph : filterConnectedComponentsContaining(Sets.newHashSet(commandNames), commandGraph
				.getConnectedComponents())) {
			commands.addAll(graph.topologicalOrderOfAllCommands());
		}
		return executeOrderedCommands(commands, context, commandGraph);
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
	 * @return {@linkplain ComposedResult} that reflects the overall success of the just executed {@linkplain Command}s.
	 * 
	 */
	public ComposedResult executeCommandsGracefully(String... commandNames) {
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
	 * @return {@linkplain ComposedResult} that reflects the overall success of the just executed {@linkplain Command}s.
	 * 
	 */
	public ComposedResult executeCommandsGracefully(Context context, String... commandNames) {
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
	 * @return {@linkplain ComposedResult} that reflects the overall success of the just executed {@linkplain Command}s.
	 * 
	 */
	public ComposedResult executeCommandsGracefully(Iterable<String> commandNames) {
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
	 * @return {@linkplain ComposedResult} that reflects the overall success of the just executed {@linkplain Command}s.
	 * 
	 */
	public ComposedResult executeCommandsGracefully(Iterable<String> commandNames, Context context) {
		Set<String> commandsAndTheirDependencies = Sets.newHashSet(Check.noNullElements(commandNames));
		for (String command : commandNames) {
			commandsAndTheirDependencies.addAll(successiveBeforeDependencies(command, new HashSet<String>()));
		}
		return executeCommands(commandsAndTheirDependencies, context);
	}

	private Set<String> successiveBeforeDependencies(String commandName, Iterable<String> accumulator) {
		Set<String> result = Sets.newHashSet(accumulator);
		result.addAll(commandNamesOf(commandGraph.getDependencies(commandName)));
		for (CommandClass command : commandGraph.getDependencies(commandName)) {
			result.addAll(successiveBeforeDependencies(command.getName(), result));
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
	 * @return {@linkplain ComposedResult} that reflects the overall success of the just executed {@linkplain Command}s.
	 * 
	 */
	public ComposedResult executeCommands(String... commandNames) {
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
	 * @return {@linkplain ComposedResult} that reflects the overall success of the just executed {@linkplain Command}s.
	 * 
	 */
	public ComposedResult executeCommands(Context context, String... commandNames) {
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
	 * @return {@linkplain ComposedResult} that reflects the overall success of the just executed {@linkplain Command}s.
	 * 
	 */
	public ComposedResult executeCommands(Iterable<String> commandNames) {
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
	 * @return {@linkplain ComposedResult} that reflects the overall success of the just executed {@linkplain Command}s.
	 * 
	 */
	public ComposedResult executeCommands(Iterable<String> commandNames, Context context) {
		Check.noNullElements(Lists.newArrayList(commandNames), "commandNames");
		return executeOrderedCommands(commandGraph.topologicalOrderOfNames(commandNames), context, commandGraph);
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
	 * @return {@linkplain ComposedResult} that reflects the overall success of the just executed {@linkplain Command}s.
	 */
	public static ComposedResult executeCommands(CommandGraph graph) {
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
	 * @return {@linkplain ComposedResult} that reflects the overall success of the just executed {@linkplain Command}s.
	 */
	public static ComposedResult executeCommands(CommandGraph graph, Context context) {
		Check.stateIsTrue(!Check.notNull(graph, "graph").topologicalOrderOfAllCommands().isEmpty(),
				"graph must have at least one command in it");
		return executeOrderedCommands(graph.topologicalOrderOfAllCommands(), context, graph);
	}

	private static ComposedResult executeOrderedCommands(List<CommandClass> commandNames, Context context,
			CommandGraph commandGraph) {
		Check.stateIsTrue(commandNames.iterator().hasNext(), "commandNames must have at least one command name");
		Check.noNullElements(commandNames, "commandNames");
		Check.notNull(context, "context");

		ComposedResult result = new ComposedResult();
		for (String command : commandNamesOf(commandNames)) {
			if (!commandGraph.containsCommand(command)) {
				throw new CommandNotFoundException(command);
			}
			Command commandInstance = commandGraph.getCommandClass(command).newInstance();
			logger.info("Execute current command: " + commandInstance.getClass());
			long startTime = System.currentTimeMillis();
			ResultState resultState = commandInstance.execute(context);
			result.addResult(command, resultState);
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
		return result;
	}

	private static List<String> commandNamesOf(List<CommandClass> commands) {
		return ImmutableList.copyOf(Iterables.transform(commands, new Function<CommandClass, String>() {

			@Override
			public String apply(CommandClass command) {
				return command.getName();
			}

		}));
	}

	/**
	 * Represents a composite result state that is composed of partial {@linkplain ResultState}s. During an execution
	 * workflow the final {@linkplain SimpleState} will reflect the most urgent {@linkplain ResultState} that came up in
	 * the workflow. Highest urgency level is FAILURE, followed by WARNING, followed by SUCCESS.
	 */
	public static final class ComposedResult {
		private SimpleState state = SimpleState.SUCCESS;
		private final List<String> executedCommands = Lists.newLinkedList();
		private final List<ResultState> partialResults = Lists.newLinkedList();

		/**
		 * @return most urgent execution result.
		 */
		public SimpleState getState() {
			return state;
		}

		@VisibleForTesting
		void addResult(String commandName, ResultState resultState) {
			executedCommands.add(commandName);
			partialResults.add(resultState);
			setOverallStateRespectfully(resultState);
		}

		private void setOverallStateRespectfully(ResultState resultState) {
			if (resultState instanceof Warning && state.equals(SimpleState.SUCCESS)) {
				state = SimpleState.WARNING;
			} else if (resultState instanceof Failure
					&& (state.equals(SimpleState.SUCCESS) || state.equals(SimpleState.WARNING))) {
				state = SimpleState.FAILURE;
			}
		}

		/**
		 * 
		 * @return all {@linkplan ResultState}s that were obtained while executing given {@link Command}s. Order of this
		 *         result corresponds to the sequence of the execution. Result will be immutable.
		 */
		public List<ResultState> getPartialResults() {
			return ImmutableList.copyOf(partialResults);
		}

		/**
		 * 
		 * @return all names of the {@link Command}s that were just executed. Order of this result corresponds to the
		 *         sequence of the execution. Result will be immutable.
		 */
		public List<String> getExecutedCommandNames() {
			return ImmutableList.copyOf(executedCommands);
		}

		/**
		 * @return {@link String} representation of the given state. Result will look like <br>
		 *         <code>Composed execution result: SUCCESS. Partial execution results: {NameOfCommand: Execution completed successfully!}</code>
		 */
		@Override
		public String toString() {
			String message = "Composed execution result: " + state + ". Partial execution results: {";
			Iterator<ResultState> result = partialResults.iterator();
			for (String command : executedCommands) {
				message += command + ": " + result.next();
				message += result.hasNext() ? ", " : "}";
			}
			return message;
		}
	}

	public static enum SimpleState {
		SUCCESS, WARNING, FAILURE
	}

}
