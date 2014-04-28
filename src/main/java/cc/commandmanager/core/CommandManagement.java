package cc.commandmanager.core;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.chain.Catalog;
import org.apache.commons.chain.config.ConfigParser;
import org.apache.commons.chain.impl.CatalogFactoryBase;
import org.apache.log4j.Logger;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

/**
 * This class is used for the controlled execution of commands. Commands to be
 * executed are declared in an catalog. Those commands will be ordered and then
 * executed.
 * <p>
 * This class executes specified initial commands needed for further tasks.
 * Arguments will be parsed from command line and may then be accessed.
 * 
 * @author Sebastian Baer
 * 
 */
public class CommandManagement {
	private static final Logger logger = Logger.getLogger(CommandManagement.class);

	private final CommunicationContext communicationContext;
	private final Catalog catalog;
	private DependencyCollector dependencyCollector;

	// TODO builder pattern anwenden
	public CommandManagement(String catalogLocation) {
		this(new CommunicationContext(), catalogLocation);
	}

	public CommandManagement(CommunicationContext context, String catalogLocation) {
		this(context, loadCatalogFromResource(catalogLocation));
	}

	public CommandManagement(Catalog catalog) {
		this(new CommunicationContext(), catalog);
	}

	// WAS ENTHÄLT catalog?
	public CommandManagement(CommunicationContext context, Catalog catalog) {
		communicationContext = context;
		this.catalog = catalog;
	}

	/**
	 * This method takes a location to retrieve a catalog. If there is a valid
	 * catalog at the given location, this catalog will be set as the class
	 * variable in this instance of {@link CommandManagement}.
	 * 
	 * @param catalogLocation
	 * @throws CatalogNotInstantiableException
	 *             if problems occur while translating the catalog file at the
	 *             specified location
	 */
	public static Catalog loadCatalogFromResource(String catalogLocation) {
		Preconditions.checkArgument(!Strings.isNullOrEmpty(catalogLocation));
		ConfigParser configParser = new ConfigParser();
		try {
			logger.info("Loading catalog resource from location: "
					+ CommandManagement.class.getResource(catalogLocation));
			configParser.parse(CommandManagement.class.getResource(catalogLocation));
			return CatalogFactoryBase.getInstance().getCatalog();
		} catch (Exception e) { // Exception type cannot be more specified, due
			// to parse()-signature
			logger.error("There is no valid catalog at the given path: " + catalogLocation, e);
			throw new CatalogNotInstantiableException();
		}
	}

	public Catalog getCatalog() {
		return catalog;
	}

	public Map<String, Set<String>> getDependencies() {
		return dependencyCollector.getDependencies();
	}

	/**
	 * Returns a {@linkplain List<String>} with all commands of a given map of
	 * dependencies in an ordered sequence.
	 * 
	 * @return An ordered {@linkplain List<String>} containing the commands of
	 *         the catalog.
	 */
	public List<String> getOrderedCommands() {
		return getOrderedCommands(new HashSet<String>(), new HashSet<String>());
	}

	/**
	 * Returns a {@linkplain List<String>} with all commands of a given map of
	 * dependencies in an ordered sequence.
	 */
	public List<String> getOrderedCommands(Map<String, Set<String>> dependencies) {
		return getOrderedCommands(dependencies, new HashSet<String>(), new HashSet<String>());
	}

	/**
	 * Returns a {@linkplain List<String>} with all commands of a given map of
	 * dependencies in an ordered sequence.
	 */
	public List<String> getOrderedCommands(Set<String> startCommands, Set<String> endCommands) {
		dependencyCollector = new DependencyCollector(catalog);

		Map<String, Set<String>> dependencies = getDependencies();

		Map<String, Set<String>> strongComponents = dependencyCollector.getStrongComponents(dependencies,
				startCommands, endCommands);

		return dependencyCollector.orderCommands(strongComponents);
	}

	public List<String> getOrderedCommands(Map<String, Set<String>> dependencies, Set<String> startCommands,
			Set<String> endCommands) {

		dependencyCollector = new DependencyCollector();

		dependencies = dependencyCollector.getStrongComponents(dependencies, startCommands, endCommands);

		return dependencyCollector.orderCommands(dependencies);
	}

	/**
	 * Takes a {@linkplain List} of commands and executes them in the list's
	 * sequence
	 */
	public void executeCommands(List<String> commands) {
		executeCommands(commands, communicationContext);
	}

	/**
	 * Takes a {@linkplain List} of commands and executes them in the list's
	 * sequence, using the specified {@linkplain CommunicationContext}
	 */
	public void executeCommands(List<String> commands, CommunicationContext localCommunicationContext) {
		for (String commandName : commands) {
			try {
				DependencyCommand command;
				command = (DependencyCommand) catalog.getCommand(commandName);
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
		return communicationContext;
	}

	public void executeAllCommands() {
		executeCommands(getOrderedCommands());
	}
}
