package cc.commandmanager.core;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sf.qualitycheck.Check;

import org.jgrapht.graph.DirectedMultigraph;

import com.google.common.collect.Maps;

public class CommandGraph {

	private final DirectedMultigraph<CommandClass, DependencyEdge> commandGraph;
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

	public static class CommandGraphBuilder {
		private Map<String, CommandClass> namesToCommandClasses = Maps.newHashMap();
		private Map<String, Dependencies> namesToMandatoryDependencies = Maps.newHashMap();
		private Map<String, Dependencies> namesToOptionalDependencies = Maps.newHashMap();
		private DirectedMultigraph<CommandClass, DependencyEdge> graph = new DirectedMultigraph<CommandClass, DependencyEdge>(
				DependencyEdge.class);

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

		public CommandGraph build() {
			for (String command : namesToCommandClasses.keySet()) {
				graph.addVertex(namesToCommandClasses.get(command));
				addMandatoryDependenciesToGraph(command);
				addOptionalDependenciesToGraph(command);
			}
			return new CommandGraph(this);
		}

		private void addMandatoryDependenciesToGraph(String command) {
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

		private void addOptionalDependenciesToGraph(String command) {
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
