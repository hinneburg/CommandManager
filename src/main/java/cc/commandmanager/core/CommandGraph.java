package cc.commandmanager.core;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sf.qualitycheck.Check;

import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph.CycleFoundException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class CommandGraph {

	private final DirectedAcyclicGraph<CommandClass, DependencyEdge> commandGraph;
	private final Map<String, CommandClass> vertices;

	private CommandGraph(CommandGraphBuilder builder) {
		commandGraph = builder.graph;
		vertices = builder.namesToCommandClasses;
	}

	/**
	 * @param commandName
	 * @return {@code true} if this command graph contains a vertex with the given command name, {@code false}
	 *         otherwise.
	 */
	public boolean hasCommand(String commandName) {
		Check.notNull(commandName, "commandName");
		return vertices.containsKey(commandName);
	}

	public CommandClass getCommandClass(String commandName) {
		Check.notEmpty(commandName, "commandName");
		if (hasCommand(commandName)) {
			return vertices.get(commandName);
		} else {
			throw new CommandNotFoundException(commandName);
		}
	}

	public Iterable<CommandClass> getDependencies(String commandName) {
		Check.notEmpty(commandName, "commandName");
		if (hasCommand(commandName)) {
			Collection<CommandClass> targets = Lists.newArrayList();
			Collection<DependencyEdge> dependencies = commandGraph.edgesOf(vertices.get(commandName));
			for (DependencyEdge dependency : dependencies) {
				targets.add(commandGraph.getEdgeTarget(dependency));
			}
			return targets;
		} else {
			throw new CommandNotFoundException(commandName);
		}
	}
	public static class CommandGraphBuilder {
		private Map<String, CommandClass> namesToCommandClasses = Maps.newHashMap();
		private Map<String, Dependencies> namesToMandatoryDependencies = Maps.newHashMap();
		private Map<String, Dependencies> namesToOptionalDependencies = Maps.newHashMap();
		private DirectedAcyclicGraph<CommandClass, DependencyEdge> graph = new DirectedAcyclicGraph<CommandClass, DependencyEdge>(
				DependencyEdge.class);

		public boolean addCommand(String name, String className) {
			return addCommand(new CommandClass(Check.notNull(name, "name"), Check.notNull(className, "className")));
		}

		public boolean addCommand(CommandClass commandClass) {
			Check.notNull(commandClass, "commandClass");
			if (isAlreadyPresent(commandClass)) {
				return false;
			}

			String command = commandClass.getName();
			namesToCommandClasses.put(command, commandClass);
			namesToMandatoryDependencies.put(command, new Dependencies());
			namesToOptionalDependencies.put(command, new Dependencies());
			graph.addVertex(commandClass);
			return true;
		}

		private boolean isAlreadyPresent(String commandName) {
			return namesToCommandClasses.containsKey(commandName);
		}

		private boolean isAlreadyPresent(CommandClass commandClass) {
			return isAlreadyPresent(commandClass.getName());
		}

		// ***********************
		// ADD DEPENDENCIES START
		// ***********************

		/**
		 * Add the given edge IFF <li>the given edge is not already a member of the graph <li>there is not already an
		 * edge from {@code source} to {@code target} in the graph <li>the edge does not induce a cycle in the graph.
		 * 
		 * @param sourceName
		 *            source of the newly created edge
		 * @param targetName
		 *            target of the newly created edge
		 * @return {@code true} if the edge was added to the graph.
		 */
		public boolean addMandatoryDependency(String sourceName, String targetName) {
			if (!isAlreadyPresent(Check.notNull(sourceName, "sourceName"))
					|| !isAlreadyPresent(Check.notNull(targetName, "targetName"))) {
				return false;
			}
			try {
				return addMandatoryDependencyOfPresentCommands(sourceName, targetName);
			} catch (CycleFoundException e) {
				return false;
			}
		}

		/**
		 * Add the given edge IFF <li>the given edge is not already a member of the graph <li>there is not already an
		 * edge from {@code source} to {@code target} in the graph <li>the edge does not induce a cycle in the graph.
		 * 
		 * @param source
		 *            source of the newly created edge
		 * @param target
		 *            target of the newly created edge
		 * @return {@code true} if the edge was added to the graph.
		 */
		public boolean addMandatoryDependency(CommandClass source, CommandClass target) {
			if (!isAlreadyPresent(Check.notNull(source, "source"))
					|| !isAlreadyPresent(Check.notNull(target, "target"))) {
				return false;
			}
			try {
				return addMandatoryDependencyOfPresentCommands(source, target);
			} catch (CycleFoundException e) {
				return false;
			}
		}

		private boolean addMandatoryDependencyOfPresentCommands(String sourceName, String targetName)
				throws CycleFoundException {
			return addMandatoryDependencyOfPresentCommands(namesToCommandClasses.get(sourceName), namesToCommandClasses
					.get(targetName));
		}

		private boolean addMandatoryDependencyOfPresentCommands(CommandClass source, CommandClass target)
				throws CycleFoundException {
			Set<String> targets = namesToMandatoryDependencies.get(source.getName()).beforeDependencies;
			targets.add(target.getName());
			return graph.addDagEdge(source, target, new DependencyEdge());
		}
		public CommandGraphBuilder addCommandWithDependencies(CommandClass commandClass,
				Dependencies mandatoryDependencies, Dependencies optionalDependencies) {
			Check.notNull(commandClass, "commandClass");
			Check.noNullElements(mandatoryDependencies.beforeDependencies, "mandatoryBeforeDependencies");
			Check.noNullElements(mandatoryDependencies.afterDependencies, "mandatoryAfterDependencies");
			Check.noNullElements(optionalDependencies.beforeDependencies, "optionalBeforeDependencies");
			Check.noNullElements(optionalDependencies.afterDependencies, "optionalAfterDependencies");

			namesToCommandClasses.put(commandClass.getName(), commandClass);
			namesToMandatoryDependencies.put(commandClass.getName(), mandatoryDependencies);
			namesToOptionalDependencies.put(commandClass.getName(), optionalDependencies);
			return this;
		}

		// ***********************
		// ADD DEPENDENCIES END
		// ***********************

		public CommandGraph build() {
			return new CommandGraph(this);
		}

		private static DirectedAcyclicGraph<CommandClass, DependencyEdge> composeCurrentState(
				Map<String, CommandClass> namesToCommandClasses,
				Map<String, Dependencies> namesToMandatoryDependencies,
				Map<String, Dependencies> namesToOptionalDependencies) {
			Check.notNull(namesToCommandClasses, "namesToCommandClasses");
			Check.notNull(namesToMandatoryDependencies, "namesToMandatoryDependencies");
			Check.notNull(namesToOptionalDependencies, "namesToOptionalDependencies");

			DirectedAcyclicGraph<CommandClass, DependencyEdge> graph = new DirectedAcyclicGraph<CommandClass, CommandGraph.DependencyEdge>(
					CommandGraph.DependencyEdge.class);
			for (String command : namesToCommandClasses.keySet()) {
				graph.addVertex(namesToCommandClasses.get(command));
				addMandatoryDependenciesToGraph(command, namesToCommandClasses, namesToMandatoryDependencies, graph);
				addOptionalDependenciesToGraph(command, namesToCommandClasses, namesToOptionalDependencies, graph);
			}
			return graph;
		}

		private static void addMandatoryDependenciesToGraph(String command,
				Map<String, CommandClass> namesToCommandClasses,
				Map<String, Dependencies> namesToMandatoryDependencies,
				DirectedAcyclicGraph<CommandClass, DependencyEdge> graph) {
			Check.notNull(command, "command");
			Check.notNull(namesToCommandClasses, "namesToCommandClasses");
			Check.notNull(namesToMandatoryDependencies, "namesToMandatoryDependencies");
			Check.notNull(graph, "graph");

			Dependencies dependencies = namesToMandatoryDependencies.get(command);
			if (!dependencies.beforeDependencies.isEmpty()) {
				for (String dependency : dependencies.beforeDependencies) {
					graph.addEdge(namesToCommandClasses.get(command), namesToCommandClasses.get(dependency));
				}
			}
			if (!dependencies.afterDependencies.isEmpty()) {
				for (String dependency : dependencies.afterDependencies) {
					graph.addEdge(namesToCommandClasses.get(dependency), namesToCommandClasses.get(command));
				}
			}
		}

		private static void addOptionalDependenciesToGraph(String command,
				Map<String, CommandClass> namesToCommandClasses, Map<String, Dependencies> namesToOptionalDependencies,
				DirectedAcyclicGraph<CommandClass, DependencyEdge> graph) {
			Dependencies dependencies = namesToOptionalDependencies.get(command);
			if (!dependencies.beforeDependencies.isEmpty()) {
				for (String dependency : dependencies.beforeDependencies) {
					graph.addEdge(namesToCommandClasses.get(command), namesToCommandClasses.get(dependency));
				}
			}
			if (!dependencies.afterDependencies.isEmpty()) {
				for (String dependency : dependencies.afterDependencies) {
					graph.addEdge(namesToCommandClasses.get(dependency), namesToCommandClasses.get(command));
				}
			}
		}

	}

	public static class DependencyEdge {
		// TODO implement Optional and Mandatory Dependencies
	}

	/**
	 * Before dependencies represent commands on which the given command depends on. After dependencies represent
	 * commands that are depending on a given command.
	 */
	public static class Dependencies {
		public Set<String> beforeDependencies;
		public Set<String> afterDependencies;

		public Dependencies() {
			this(new HashSet<String>(), new HashSet<String>());
		}

		public Dependencies(Set<String> beforeDependencies, Set<String> afterDependencies) {
			this.beforeDependencies = beforeDependencies;
			this.afterDependencies = afterDependencies;
		}
	}

}
