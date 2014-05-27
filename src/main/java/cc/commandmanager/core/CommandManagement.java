package cc.commandmanager.core;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.qualitycheck.Check;

import org.apache.log4j.Logger;

/**
 * This class is used for the controlled execution of commands. Commands to be executed are declared in a catalog. Those
 * commands will be ordered and then executed.
 * <p>
 * This class executes specified initial commands needed for further tasks. Arguments will be parsed from command line
 * and may then be accessed.
 */
public class CommandManagement {

	private final Context context;
	private final Catalog catalog;
	private DependencyCollector dependencyCollector;
	private static final Logger logger = Logger.getLogger(CommandManagement.class);

	public CommandManagement(String catalogLocation) {
		this(catalogLocation, new Context());
	}

	public CommandManagement(String catalogLocation, Context context) {
		this(Catalog.fromXmlFile(catalogLocation), context);
	}

	public CommandManagement(Catalog catalog) {
		this(catalog, new Context());
	}

	public CommandManagement(Catalog catalog, Context context) {
		this.context = context;
		this.catalog = catalog;
	}

	/**
	 * Returns a {@linkplain List<String>} with all commands of a given map of dependencies in an ordered sequence.
	 * 
	 * @return An ordered {@linkplain List<String>} containing the commands of the catalog.
	 */
	public List<String> getOrderedCommands() {
		return getOrderedCommands(new HashSet<String>(), new HashSet<String>());
	}

	/**
	 * Returns a {@linkplain List<String>} with all commands of a given map of dependencies in an ordered sequence.
	 */
	public List<String> getOrderedCommands(Set<String> startCommands, Set<String> endCommands) {
		Check.notNull(startCommands, "startCommand");
		Check.notNull(endCommands, "endCommands");

		dependencyCollector = new DependencyCollector(catalog);

		Map<String, Set<String>> dependencies = getDependencies();

		Map<String, Set<String>> strongComponents = dependencyCollector.getStrongComponents(dependencies,
				startCommands, endCommands);

		return dependencyCollector.orderCommands(strongComponents);
	}

	public Map<String, Set<String>> getDependencies() {
		return dependencyCollector.getDependencies();
	}

	/**
	 * Returns a {@linkplain List<String>} with all commands of a given map of dependencies in an ordered sequence.
	 */
	public List<String> getOrderedCommands(Map<String, Set<String>> dependencies) {
		Check.notNull(dependencies, "dependencies");
		return getOrderedCommands(dependencies, new HashSet<String>(), new HashSet<String>());
	}

	public List<String> getOrderedCommands(Map<String, Set<String>> dependencies, Set<String> startCommands,
			Set<String> endCommands) {
		Check.notNull(dependencies, "dependencies");
		Check.notNull(startCommands, "startCommands");

		dependencyCollector = new DependencyCollector();

		dependencies = dependencyCollector.getStrongComponents(dependencies, startCommands, endCommands);

		return dependencyCollector.orderCommands(dependencies);
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

		for (String commandName : commands) {
			try {
				Command command;
				command = catalog.getCommand(commandName);
				logger.info("Execute current command : [ " + command.getClass() + " ]");
				long startTime = System.currentTimeMillis();
				command.execute(localContext);
				logger.info(System.currentTimeMillis() - startTime + " ms");
			} catch (RuntimeException e) {
				logger.error(String.format("The current command %s caused a critical exception", commandName));
				throw e;
			}
		}
	}

}
