package cc.commandmanager.core;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.qualitycheck.Check;

import org.apache.log4j.Logger;

import cc.commandmanager.core.ResultState.Failure;
import cc.commandmanager.core.ResultState.Warning;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * This class is used for the controlled execution of commands. Commands to be executed are declared in a catalog. Those
 * commands will be ordered and then executed.
 * <p>
 * This class executes specified initial commands needed for further tasks. Arguments will be parsed from command line
 * and may then be accessed.
 */
public class CommandManager {
	private static final Logger logger = Logger.getLogger(CommandManager.class);

	private final Context context;
	private final CommandGraph commandGraph;

	/**
	 * Create a new {@link CommandManager}. Use the {@linkplain CommandGraph}, parsed from the given XML file. The
	 * command graph specifies which commands which will be executed and in which order this will happen. A new
	 * {@link Context} will be used to execute the commands with.
	 * 
	 * @param catalog
	 *            see {@link CommandGraph#fromFile(File))} for specifications on the catalog file.
	 */
	public CommandManager(File catalog) {
		this(catalog, new Context());
	}

	/**
	 * Create a new {@link CommandManager}. Use the {@linkplain CommandGraph}, parsed from the given XML file. The
	 * command graph specifies which commands which will be executed and in which order this will happen.
	 * 
	 * @param catalog
	 *            see {@link CommandGraph#fromFile(File))} for specifications on the catalog file.
	 * @param context
	 *            information in the context will be used to execute the commands with.
	 */
	public CommandManager(File catalog, Context context) {
		this(CommandGraph.fromXml(catalog).get(), context);
	}

	/**
	 * Create a new {@link CommandManager}. A new {@link Context} will be used to execute the commands with.
	 * 
	 * @param commandGraph
	 *            specifies which commands which will be executed and in which order this will happen.
	 */
	public CommandManager(CommandGraph commandGraph) {
		this(commandGraph, new Context());
	}

	/**
	 * Create a new {@link CommandManager}.
	 * 
	 * @param commandGraph
	 *            specifies which commands which will be executed and in which order this will happen.
	 * @param context
	 *            information in the context will be used to execute the commands with.
	 */
	public CommandManager(CommandGraph commandGraph, Context context) {
		this.commandGraph = Check.notNull(commandGraph, "commandGraph");
		this.context = Check.notNull(context, "context");
	}

	/**
	 * 
	 * @return {@linkplain CommandGraph} object that was used to build this {@linkplain CommandManager}.
	 */
	public CommandGraph getCommandGraph() {
		return commandGraph;
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

	private static List<String> commandNamesOf(List<CommandClass> commands) {
		return ImmutableList.copyOf(Iterables.transform(commands, new Function<CommandClass, String>() {

			@Override
			public String apply(CommandClass command) {
				return command.getName();
			}

		}));
	}

	public ComposedResult executeAllCommands() {
		return executeCommands(commandNamesOf(commandGraph.topologicalOrderOfAllCommands()));
	}

	public ComposedResult executeCommands(String... commandNames) {
		return executeCommands(commandNames);
	}

	public ComposedResult executeCommands(Context context, String... commandNames) {
		return executeCommands(context, commandNames);
	}

	/**
	 * Takes a {@linkplain List} of commands and executes them in the list's sequence
	 */
	public ComposedResult executeCommands(Iterable<String> commandNames) {
		return executeCommands(commandNames, context);
	}

	/**
	 * Takes a {@linkplain List} of commands and executes them in the list's sequence, using the specified
	 * {@linkplain Context}
	 */
	public ComposedResult executeCommands(Iterable<String> commandNames, Context context) {
		Check.notEmpty(Lists.newArrayList(commandNames), "commandNames");
		return executeCommands(commandNamesOf(commandGraph.topologicalOrderOfNames(commandNames)), context,
				commandGraph);
	}

	private static ComposedResult executeCommands(Iterable<String> commandNames, Context context,
			CommandGraph commandGraph) {
		Check.noNullElements(commandNames, "commandNames");
		Check.notNull(context, "context");

		ComposedResult result = new ComposedResult();
		for (String command : commandNames) {
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
				String message = "Command " + commandInstance.getClass() + " failed to execute (took "
						+ (System.currentTimeMillis() - startTime) + " ms): " + resultState.getMessage();
				if (resultState.hasCause()) {
					message += " " + resultState.getCause();
				}
				logger.error(message);
				logger.error("Aborting execution of all commands.");
				break;
			}
		}
		return result;
	}

	public static final class ComposedResult {
		private ResultState2 composedResult = ResultState2.SUCCESS;
		private final Map<String, ResultState> commandResults = Maps.newLinkedHashMap();

		public ResultState2 getComposedResult() {
			return composedResult;
		}

		@VisibleForTesting
		void addResult(String commandName, ResultState resultState) {
			commandResults.put(commandName, resultState);
			setOverallStateRespectfully(resultState);
		}

		private void setOverallStateRespectfully(ResultState resultState) {
			if (resultState instanceof Warning && composedResult.equals(ResultState2.SUCCESS)) {
				composedResult = ResultState2.WARNING;
			} else if (resultState instanceof Failure
					&& (composedResult.equals(ResultState2.SUCCESS) || composedResult.equals(ResultState2.WARNING))) {
				composedResult = ResultState2.FAILURE;
			}
		}

		public List<ResultState> getPartialResults() {
			return Lists.newLinkedList(commandResults.values());
		}

		public Map<String, ResultState> getPartialResultsWithCorrespondingCommandName() {
			return ImmutableMap.<String, ResultState> copyOf(commandResults);
		}

		@Override
		public String toString() {
			return "Composed execution result: " + composedResult + ". Partial execution results: " + commandResults;
		}
	}

	public static enum ResultState2 {
		SUCCESS, WARNING, FAILURE
	}

}
