package cc.commandmanager.core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.qualitycheck.Check;

import org.apache.log4j.Logger;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

/**
 * Collects dependencies of commands mentioned in the catalog and gets them ordered.
 * <p>
 * Each map follows the semantic that a key of a map is dependent of the correspondent value (respectively dependent of
 * each element of the values ArrayList).
 */
public class DependencyCollector {

	private Catalog catalog;
	private static final Logger logger = Logger.getLogger(DependencyCollector.class);

	/**
	 * Class constructor taking the catalog argument and sets it in this class.
	 * 
	 * @param catalog
	 */
	public DependencyCollector(Catalog catalog) {
		this.catalog = Check.notNull(catalog, "catalog");
	}

	public DependencyCollector() {

	}

	private static class Dependencies {

		public Map<String, Set<String>> necessaryDependencies = new HashMap<String, Set<String>>();
		public Map<String, Set<String>> optionalDependencies = new HashMap<String, Set<String>>();

		public Dependencies(Map<String, Set<String>> necessaryDependencies,
				Map<String, Set<String>> optionalDependencies) {
			this.necessaryDependencies = necessaryDependencies;
			this.optionalDependencies = optionalDependencies;
		}
	}

	/**
	 * Every command of the catalog asked for its mandatory and optional before and after dependencies. Then those
	 * dependencies will be processed by the updateDependencies method.
	 */
	public Map<String, Set<String>> getDependencies() {
		Dependencies dependencies = composeDependencies(catalog.getCommandNames());
		Preconditions.checkNotNull(dependencies.necessaryDependencies);

		Map<String, Set<String>> necessaryDependencies = dependencies.necessaryDependencies;
		Map<String, Set<String>> optionalDependencies = dependencies.optionalDependencies;

		/*
		 * TODO Following code contains parts that must be reworked. They were inserted to ensure that the
		 * DependencyCollector is working properly. The focus thereby lies on the correct ordering of optional
		 * dependencies.
		 */
		logger.info("Necessary dependencies " + necessaryDependencies);
		logger.info("Optional dependencies " + optionalDependencies);

		Map<String, Set<String>> composedDependencies = new HashMap<String, Set<String>>(necessaryDependencies);
		// start
		try {

			// end
			for (String key : optionalDependencies.keySet()) {
				logger.info(key);
				optionalDependencies.get(key);

				// TODO check whether if block makes sense here. It was inserted as a quick fix.
				if (composedDependencies.containsKey(key)) {
					for (String value : optionalDependencies.get(key)) {
						if (composedDependencies.containsKey(value)) {
							// start
							try {
								// end

								composedDependencies.get(key).add(value);
								// start
							} catch (Exception e) {
								logger.info("Error 3b");
								logger.info("key " + key);
								logger.info("value " + value);
								logger.info("containsKey " + composedDependencies.containsKey(value) + " getKey "
										+ composedDependencies.get(key));
								logger.error(e.getStackTrace());
							}
							// end
						}
					}
				}
			}
			// start
		} catch (Exception e) {
			logger.info("Error 3");
			logger.error(e.getStackTrace());
		}

		try {
			// end
			makeDotFile(composedDependencies, optionalDependencies, "");

		} catch (Exception e) {
			// start
			logger.info("Error 4");
			logger.error(e.getStackTrace());
			// end
		}

		return composedDependencies;
	}

	private Dependencies composeDependencies(Iterable<String> commandNames) {
		Check.noNullElements(commandNames, "commandNames");

		Map<String, Set<String>> necessaryDependencies = new HashMap<String, Set<String>>();
		Map<String, Set<String>> optionalDependencies = new HashMap<String, Set<String>>();

		for (String commandName : commandNames) {
			Command command = catalog.getCommand(commandName);
			updateDependencies(commandName, necessaryDependencies, command.getAfterDependencies(),
					command.getBeforeDependencies());
			updateDependencies(commandName, optionalDependencies, command.getOptionalAfterDependencies(),
					command.getOptionalBeforeDependencies());
		}

		return new Dependencies(necessaryDependencies, optionalDependencies);
	}

	/**
	 * Checks if the name is contained as key in the dependencies map. If it is, it takes the value of the key (an
	 * arrayList) and merges it with the given beforeDependencies. After merging, the compoundBeforeList is set as new
	 * value of the key. Otherwise the name and the beforeDependencies are added as new key-value-pair to the
	 * dependencies.
	 * <p>
	 * If there is an element in the afterDependencies, each element of the list must be added to the dependencies as
	 * key with the name as correspondent value. If that pair is contained the old and the new list have to be merged,
	 * otherwise only the new key-value-pair is added.
	 * 
	 * @param name
	 * @param dependencies
	 * @param afterDependencies
	 * @param beforeDependencies
	 */
	@VisibleForTesting
	static void updateDependencies(String name, Map<String, Set<String>> dependencies, Set<String> afterDependencies,
			Set<String> beforeDependencies) {
		Check.notNull(dependencies, "dependencies");
		Check.notNull(afterDependencies, "afterDependencies");
		Check.notNull(beforeDependencies, "beforeDependencies");

		if (dependencies.containsKey(name)) {
			dependencies.get(name).addAll(beforeDependencies);
		} else {
			dependencies.put(name, beforeDependencies);
		}

		if (!afterDependencies.isEmpty()) {
			for (String key : afterDependencies) {
				if (dependencies.containsKey(key)) {
					dependencies.get(key).add(name);
				} else {
					dependencies.put(key, new HashSet<String>(Arrays.asList(name)));
				}
			}
		}
	}

	/**
	 * Creates a file in dot format. A -> B means that A depends of B. A dashed line represents an optional dependency.
	 * It accesses the global dependency maps, so it must be executed before the maps are changed, e.g. before executing
	 * the orderCommands method because it changes the maps.
	 */
	private static void makeDotFile(Map<String, Set<String>> composedDependencies,
			Map<String, Set<String>> optionalDependencies, String name) {
		Check.notNull(composedDependencies, "composedDependencies");
		Check.notNull(optionalDependencies, "optionalDependencies");
		Check.notNull(name, "name");

		// TODO StringBuilder
		String dotContent = "digraph G { \n";
		dotContent += "rankdir = BT; \n";
		dotContent += "node [shape=record]; \n";
		dotContent += "edge [arrowhead=vee]; \n";

		for (String key : composedDependencies.keySet()) {
			if (composedDependencies.get(key).isEmpty()) {
				dotContent += key + "; \n";
			} else {
				for (String value : composedDependencies.get(key)) {
					dotContent += (key + " -> " + value + "; \n");
				}
			}
		}
		for (String key : optionalDependencies.keySet()) {
			if (!optionalDependencies.get(key).isEmpty()) {
				for (String value : optionalDependencies.get(key)) {
					dotContent += (key + " -> " + value + " [style = dotted] " + "; \n");
				}
			}
		}

		dotContent += "}";

		final String etc = "etc";
		final String dotFile = etc + "/graph" + name + ".dot";
		try {
			File dir = new File(etc);
			if (!dir.exists()) {
				dir.mkdir();
			}

			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(dotFile));
			bufferedWriter.write(dotContent);
			bufferedWriter.close();
		} catch (IOException e) {
			logger.warn("Dot file could not be created at " + dotFile, e);
		}
	}

	/**
	 * Return a list of commands, topologically sorted by their dependency representation in the catalog that was used
	 * to create this {@link DependencyCollector} with.
	 * 
	 * @throws IllegalStateException
	 *             if any command in the catalog is dependent on another command which cannot be found in the catalog.
	 */
	public List<String> getOrderedCommands() {
		return orderCommands(getDependencies());
	}

	/**
	 * Return a list of commands, topologically sorted by their dependency representation in dependencies.
	 * 
	 * @param dependencies
	 *            represents commands which will get ordered. If a command maps a non empty {@link Set} that means that
	 *            the given command is dependent on other commands, contained by this {@link Set}. Any command of this
	 *            {@link Set} must occur as a key in dependencies as well.
	 * @throws IllegalStateException
	 *             if any {@link Set} to a given key holds a command which does not appear as a key in dependencies as
	 *             well.
	 */
	public static List<String> orderCommands(Map<String, Set<String>> dependencies) {
		Check.notNull(dependencies, "dependencies");

		Map<String, Set<String>> commandsWithOutgoingEdges = new ConcurrentHashMap<String, Set<String>>(dependencies);
		List<String> orderedCommands = new ArrayList<String>();
		List<String> commandsToBeOrdered = new ArrayList<String>();
		String node = "";

		// find all nodes with no dependencies, put into helpList, remove from
		// HashMap
		for (String key : commandsWithOutgoingEdges.keySet()) {
			Set<String> outgoingEdges = commandsWithOutgoingEdges.get(key);

			if (outgoingEdges.isEmpty()) {
				commandsToBeOrdered.add(key);
				commandsWithOutgoingEdges.remove(key);
			}
		}

		// as long as helpList contains a node without dependencies, take one,
		// remove it from helpList, put into commandList
		while (!commandsToBeOrdered.isEmpty()) {
			node = commandsToBeOrdered.iterator().next();
			commandsToBeOrdered.remove(node);
			orderedCommands.add(node);

			// check if there is any edge between the node and another one
			for (String key : commandsWithOutgoingEdges.keySet()) {
				Set<String> list = commandsWithOutgoingEdges.get(key);

				// if the node is in a value list, remove it
				if (list.contains(node)) {
					list.remove(node);
					commandsWithOutgoingEdges.put(key, list);
				}

				// if the node has no other incoming edges, put it into
				// commandList
				if (commandsWithOutgoingEdges.get(key).isEmpty()) {
					commandsToBeOrdered.add(key);
					commandsWithOutgoingEdges.remove(key);
				}
			}
		}

		// only if the dependencyMap is empty the graph was correct, otherwise
		// there was something wrong with it
		if (!commandsWithOutgoingEdges.isEmpty()) {
			logger.error("The dependencyMap wasn't empty yet but it should have been: " + commandsWithOutgoingEdges);
			throw new IllegalStateException();
		}

		return orderedCommands;
	}

	public static Map<String, Set<String>> getStrongComponents(Map<String, Set<String>> dependencies,
			Set<String> startCommands) {
		Check.notNull(dependencies, "dependencies");
		Check.notNull(startCommands, "startCommands");

		Map<String, Set<String>> newDependencies = new HashMap<String, Set<String>>();

		logger.info("startCommands " + startCommands + "+++");

		if (startCommands.isEmpty()) {
			newDependencies.putAll(dependencies);
		} else {
			for (String command : startCommands) {
				// check whether it is actually the root.
				// Therefore dependencies must not have any values to the key 'command'
				if (!dependencies.get(command).isEmpty()) {
					logger.error("Given command seems not to be a root.");
					throw new IllegalStateException();
				} else {
					newDependencies.put(command, new HashSet<String>());
					iterateDependenciesDown(dependencies, newDependencies, command);
				}
			}
		}

		makeDotFile(newDependencies, new HashMap<String, Set<String>>(), "_strongComponents");

		return newDependencies;
	}

	private static void iterateDependenciesDown(Map<String, Set<String>> dependencies,
			Map<String, Set<String>> newDependencies, String command) {

		// check which commands are pointing to the given command, i. e. which commands are dependent on the given
		// command
		for (String key : dependencies.keySet()) {
			if (dependencies.get(key).contains(command)) {
				// if any command is dependent on the given command, a new set will be filled with the given command and
				// the dependency check will be performed on this command recursively.
				Set<String> tmp = new HashSet<String>();
				tmp.add(command);
				if (newDependencies.containsKey(key)) {
					tmp.addAll(newDependencies.get(key));
				}
				newDependencies.put(key, tmp);
				iterateDependenciesDown(dependencies, newDependencies, key);
			}
		}
	}
}
