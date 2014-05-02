package cc.commandmanager.core;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.qualitycheck.Check;

import org.apache.commons.chain.Catalog;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.config.ConfigParser;
import org.apache.commons.chain.impl.CatalogFactoryBase;
import org.apache.log4j.Logger;

import com.google.common.annotations.VisibleForTesting;

/**
 * This class is used for the controlled execution of commands. Commands to be executed are declared in a catalog. Those
 * commands will be ordered and then executed.
 * <p>
 * This class executes specified initial commands needed for further tasks. Arguments will be parsed from command line
 * and may then be accessed.
 */
public class CommandManagement {

	@VisibleForTesting
	public Catalog catalog;
	private final CommunicationContext communicationContext;
	private DependencyCollector dependencyCollector;
	private static final Logger logger = Logger.getLogger(CommandManagement.class);

	/**
	 * Keys {@code path_logFile} and {@code path_dotFile} will be set to defaults. Defaults are
	 * {@code path_logFile = logs/Preprocessing.log} respectively {@code path_dotFile =
	 * etc/graph.dot}.
	 */
	public CommandManagement() {
		this(new CommunicationContext());
	}

	/**
	 * @param context
	 *            will be checked for keys {@code path_logFile} and key {@code path_dotFile}. If not found they will be
	 *            set to defaults. Defaults are {@code path_logFile = logs/Preprocessing.log} respectively
	 *            {@code path_dotFile = etc/graph.dot}.
	 */
	public CommandManagement(CommunicationContext context) {
		communicationContext = Check.notNull(context, "context");
		context = ensureAtLeastDefaultLogFileProperties(context);
	}

	private CommunicationContext ensureAtLeastDefaultLogFileProperties(CommunicationContext context) {
		if (!context.containsKey("path_logFile") || context.get("path_logFile") == null) {
			context.put("path_logFile", "logs/Preprocessing.log");
		}
		if (!context.containsKey("path_dotFile") || context.get("path_dotFile") == null) {
			context.put("path_dotFile", "etc/graph.dot");
		}
		return context;
	}

	/**
	 * This method takes a location to retrieve a catalog. If there is a valid catalog at the given location, it will
	 * set the global catalog variable in this class.
	 *
	 * @param catalogLocation
	 * @throws CatalogNotInstantiableException
	 *             if problems occur while translating the catalog file at the specified location
	 */
	public void setCatalog(String catalogLocation) {
		Check.notNull(catalogLocation, "catalogLocation");

		ConfigParser configParser = new ConfigParser();

		try {
			logger.info("this.getClass().getResource(catalogLocation)" + this.getClass().getResource(catalogLocation));

			configParser.parse(this.getClass().getResource(catalogLocation));
			this.catalog = CatalogFactoryBase.getInstance().getCatalog();

		} catch (Exception e) { // Exception type cannot be more specified, due
			// to parse()-signature
			logger.error("There is no valid catalog at the given path: " + catalogLocation, e);
			throw new CatalogNotInstantiableException();
		}
	}

	public Map<String, Set<String>> getDependencies() {
		return dependencyCollector.getDependencies();
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
	public List<String> getOrderedCommands(Map<String, Set<String>> dependencies) {
		Check.notNull(dependencies, "dependencies");
		return getOrderedCommands(dependencies, new HashSet<String>(), new HashSet<String>());
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

	public List<String> getOrderedCommands(Map<String, Set<String>> dependencies, Set<String> startCommands,
			Set<String> endCommands) {
		Check.notNull(dependencies, "dependencies");
		Check.notNull(startCommands, "startCommands");

		dependencyCollector = new DependencyCollector();

		dependencies = dependencyCollector.getStrongComponents(dependencies, startCommands, endCommands);

		return dependencyCollector.orderCommands(dependencies);
	}

	/**
	 * Takes a {@linkplain List} of commands and executes them in the list's sequence
	 */
	public void executeCommands(List<String> commands) {
		Check.notNull(commands, "commands");
		this.executeCommands(commands, communicationContext);
	}

	/**
	 * Takes a {@linkplain List} of commands and executes them in the list's sequence, using the specified
	 * {@linkplain CommunicationContext}
	 */
	public void executeCommands(List<String> commands, CommunicationContext localCommunicationContext) {
		Check.notNull(commands, "commands");
		Check.notNull(localCommunicationContext, "localCommunicationContext");

		for (String commandName : commands) {
			try {
				Command command;
				command = this.catalog.getCommand(commandName);
				command.execute(localCommunicationContext);
			} catch (RuntimeException e1) {
				logger.error(String.format("The current command %s caused a critical exception", commandName));
				throw e1;
			} catch (Exception e2) {// Exception type cannot be more specified,
				// due to Command-signature
				logger.warn(String.format("The current command %s caused a non critical exception.", commandName), e2);
			}
		}
	}

	public CommunicationContext getCommunicationContext() {
		return this.communicationContext;
	}
}
