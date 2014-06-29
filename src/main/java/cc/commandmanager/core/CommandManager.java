package cc.commandmanager.core;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.qualitycheck.Check;
import net.sf.qualitycheck.exception.IllegalStateOfArgumentException;

import org.apache.log4j.Logger;

import cc.commandmanager.core.CommandGraph.CommandGraphBuilder;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * This class is used for the controlled execution of commands. Commands to be executed are declared in a catalog. Those
 * commands will be ordered and then executed.
 * <p>
 * This class executes specified initial commands needed for further tasks. Arguments will be parsed from command line
 * and may then be accessed.
 */
public class CommandManager {

	private final Context context;
	private final CommandGraph commandGraph;
	private static final Logger logger = Logger.getLogger(CommandManager.class);

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
	 * Returns a {@linkplain List<String>} with all commands of a given map of dependencies in an ordered sequence.
	 */
	public List<String> getOrderedCommands(Map<String, Set<String>> newCommandGraph) {
		Check.notNull(newCommandGraph, "newCommandGraph");
		return getOrderedCommands(newCommandGraph, Collections.<String> emptySet());
	}

	public List<String> getOrderedCommands(Map<String, Set<String>> newCommandGraph, Set<String> startCommands) {
		Check.notNull(newCommandGraph, "newCommandGraph");
		Check.notNull(startCommands, "startCommands");

		CommandGraph graph = buildFromDependencies(newCommandGraph);
		return getOrderedCommands(graph, startCommands, Collections.<String> emptySet());
	}

	/**
	 * Returns a {@linkplain List<String>} with all commands of a given map of dependencies in an ordered sequence.
	 * 
	 * @return An ordered {@linkplain List<String>} containing the commands of the catalog.
	 */
	public List<String> getOrderedCommands() {
		return getOrderedCommands(Collections.<String> emptySet(), Collections.<String> emptySet());
	}

	// TODO FOR NON-EMPTY END_COMMANDS THIS METHOD DOES NOT RETURN A CORRECT RESULT
	// Remove parameter endCommands or implement this method to be truely applicable for end commands
	// Consider, getOrderedCommands with end commands will not even be needed in CM1.0 (see #40)
	/**
	 * Returns a {@linkplain List<String>} with all commands of a given map of dependencies in an ordered sequence.
	 */
	public List<String> getOrderedCommands(Set<String> startCommands, Set<String> endCommands) {
		List<String> result = getOrderedCommands(commandGraph, startCommands, endCommands);
		return result;
	}

	private static List<String> getOrderedCommands(CommandGraph graph, Set<String> startCommands,
			Set<String> endCommands) {
		Check.notNull(startCommands, "startCommands");
		Check.stateIsTrue(endCommands == null || endCommands.isEmpty(), IllegalStateOfArgumentException.class);

		if (startCommands.isEmpty()) {
			return Lists.newLinkedList(transformGenericCollection(graph.topologicalOrderOfAllCommands()));
		} else {
			List<CommandClass> remaining = Lists.newLinkedList();
			for (Set<CommandClass> connectedComponent : filterConnectedComponentsContaining(startCommands, graph
					.getConnectedComponents())) {
				remaining.addAll(connectedComponent);
			}
			return Lists.newLinkedList(transformGenericCollection(graph.topologicalOrderOfGivenCommands(remaining)));
		}
	}

	private static List<Set<CommandClass>> filterConnectedComponentsContaining(Set<String> startCommands,
			List<Set<CommandClass>> connectedComponents) {
		Set<Set<CommandClass>> result = Sets.newHashSet();
		for (final String command : startCommands) {
			for (Set<CommandClass> connectedComponent : connectedComponents) {
				if (connectedComponentContains(connectedComponent, command)) {
					result.add(connectedComponent);
				}
			}
		}
		return Lists.newArrayList(result);
	}

	private static boolean connectedComponentContains(Set<CommandClass> connectedComponent, final String command) {
		return Iterables.any(connectedComponent, new Predicate<CommandClass>() {
			@Override
			public boolean apply(CommandClass commandClass) {
				return commandClass.getName().equals(command);
			}
		});
	}

	private static CommandGraph buildFromDependencies(Map<String, Set<String>> newCommandGraph) {
		CommandGraphBuilder builder = new CommandGraphBuilder();
		String noClassNameObtainable = "";
		for (String command : newCommandGraph.keySet()) {
			builder.addCommand(command, noClassNameObtainable);
			for (String dependency : newCommandGraph.get(command)) {
				DependencyAdded dependencyAdded = builder.addMandatoryDependency(command, dependency);
				if (dependencyAdded.isIn(DependencyAdded.FAILURE_STATES)) {
					throw new IllegalStateException(dependencyAdded.toString());
				}
			}
		}
		return builder.build();
	}

	public Map<String, Set<String>> getDependencies() {
		Map<String, Set<String>> result = new HashMap<String, Set<String>>();
		for (CommandClass command : commandGraph.topologicalOrderOfAllCommands()) {
			Collection<String> dependencies = transformGenericCollection(commandGraph
					.getDependencies(command.getName()));
			result.put(command.getName(), Sets.newHashSet(dependencies));
		}
		return result;
	}

	private static Collection<String> transformGenericCollection(List<CommandClass> commands) {
		return Collections2.transform(commands, new Function<CommandClass, String>() {

			@Override
			public String apply(CommandClass command) {
				return command.getName();
			}

		});
	}

	/**
	 * Executes all commands, previously ordered by the commands' specifications.
	 */
	public void executeAllCommands() {
		executeCommands(getOrderedCommands());
	}

	/**
	 * Takes a {@linkplain List} of commands and executes them in the list's sequence
	 */
	public void executeCommands(List<String> commands) {
		Check.notNull(commands, "commands");
		executeCommands(commands, context);
	}

	/**
	 * Takes a {@linkplain List} of commands and executes them in the list's sequence, using the specified
	 * {@linkplain Context}
	 */
	public void executeCommands(List<String> commands, Context localContext) {
		Check.notNull(commands, "commands");
		Check.notNull(localContext, "localContext");

		for (String command : commands) {
			if (!commandGraph.containsCommand(command)) {
				logger.error("Command " + command
						+ "could not be found in the command graph. Aborting execution of all commands.");
				break;
			}
			Command commandInstance = commandGraph.getCommandClass(command).newInstance();
			logger.info("Execute current command: " + commandInstance.getClass());
			long startTime = System.currentTimeMillis();
			ResultState resultState = commandInstance.execute(localContext);
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
	}
}
