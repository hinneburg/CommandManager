package cc.commandmanager.core;

import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.qualitycheck.Check;

import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph.CycleFoundException;
import org.jgrapht.graph.DefaultEdge;

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
		if (!hasCommand(commandName)) {
			throw new CommandNotFoundException(commandName);
		}
		return vertices.get(commandName);
	}

	public List<CommandClass> getDependencies(String commandName) {
		Check.notEmpty(commandName, "commandName");
		if (!hasCommand(commandName)) {
			throw new CommandNotFoundException(commandName);
		}
		List<CommandClass> targets = Lists.newArrayList();
		targets.addAll(getMandatoryDependencies(commandName));
		targets.addAll(getOptionalDependencies(commandName));
		return targets;
	}

	public List<CommandClass> getMandatoryDependencies(String commandName) {
		Check.notEmpty(commandName, "commandName");
		if (!hasCommand(commandName)) {
			throw new CommandNotFoundException(commandName);
		}
		List<CommandClass> mandatoryTargets = getDependenciesWithRequirementState(commandName, DependencyEdge.MANDATORY);
		return mandatoryTargets;
	}

	public List<CommandClass> getOptionalDependencies(String commandName) {
		Check.notEmpty(commandName, "commandName");
		if (!hasCommand(commandName)) {
			throw new CommandNotFoundException(commandName);
		}
		List<CommandClass> optionalTargets = getDependenciesWithRequirementState(commandName, DependencyEdge.OPTIONAL);
		return optionalTargets;
	}

	private List<CommandClass> getDependenciesWithRequirementState(String commandName, boolean mandatoryOrOptional) {
		List<CommandClass> targets = Lists.newArrayList();
		Set<DependencyEdge> dependencies = commandGraph.outgoingEdgesOf(vertices.get(commandName));
		for (DependencyEdge dependency : dependencies) {
			if (dependency.isMandatory() == mandatoryOrOptional) {
				targets.add(commandGraph.getEdgeTarget(dependency));
			}
		}
		return targets;
	}

	public static class CommandGraphBuilder {
		private Map<String, CommandClass> namesToCommandClasses = Maps.newHashMap();
		private DirectedAcyclicGraph<CommandClass, DependencyEdge> graph = new DirectedAcyclicGraph<CommandClass, DependencyEdge>(
				DependencyEdge.class);

		public CommandGraph build() {
			return new CommandGraph(this);
		}

		public boolean addCommand(String name, String className) {
			return addCommand(new CommandClass(Check.notNull(name, "name"), Check.notNull(className, "className")));
		}

		public boolean addCommand(CommandClass commandClass) {
			Check.notNull(commandClass, "commandClass");
			if (isAlreadyPresent(commandClass)) {
				return false;
			}

			namesToCommandClasses.put(commandClass.getName(), commandClass);
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
		 * Add a mandatory dependency from {@code sourceName} to {@code targetName} IFF <li>the given edge is not
		 * already a member of the graph <li>there is not already an edge from {@code sourceName} to {@code targetName}
		 * in the graph <li>
		 * the edge does not induce a cycle in the graph.
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
		 * Add a mandatory dependency from {@code source} to {@code target} IFF <li>the given edge is not already a
		 * member of the graph <li>there is not already an edge from {@code source} to {@code target} in the graph <li>
		 * the edge does not induce a cycle in the graph.
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
			if (graph.containsEdge(source, target) && !graph.getEdge(source, target).isMandatory()) {
				graph.getEdge(source, target).setMandatory(true);
				return true;
			} else {
				return graph.addDagEdge(source, target, new DependencyEdge(DependencyEdge.MANDATORY));
			}
		}

		/**
		 * Add an optional dependency from {@code sourceName} to {@code targetName} IFF <li>the given edge is not
		 * already a member of the graph <li>there is not already an edge from {@code sourceName} to {@code targetName}
		 * in the graph <li>
		 * the edge does not induce a cycle in the graph.
		 * 
		 * @param sourceName
		 *            source of the newly created edge
		 * @param targetName
		 *            target of the newly created edge
		 * @return {@code true} if the edge was added to the graph.
		 */
		public boolean addOptionalDependency(String sourceName, String targetName) {
			if (!isAlreadyPresent(Check.notNull(sourceName, "sourceName"))
					|| !isAlreadyPresent(Check.notNull(targetName, "targetName"))) {
				return false;
			}
			try {
				return addOptionalDependencyOfPresentCommands(sourceName, targetName);
			} catch (CycleFoundException e) {
				return false;
			}
		}

		/**
		 * Add an optional dependency from {@code source} to {@code target} IFF <li>the given edge is not already a
		 * member of the graph <li>there is not already an edge from {@code source} to {@code target} in the graph <li>
		 * the edge does not induce a cycle in the graph.
		 * 
		 * @param source
		 *            source of the newly created edge
		 * @param target
		 *            target of the newly created edge
		 * @return {@code true} if the edge was added to the graph.
		 */
		public boolean addOptionalDependency(CommandClass source, CommandClass target) {
			if (!isAlreadyPresent(Check.notNull(source, "source"))
					|| !isAlreadyPresent(Check.notNull(target, "target"))) {
				return false;
			}
			try {
				return addOptionalDependencyOfPresentCommands(source, target);
			} catch (CycleFoundException e) {
				return false;
			}
		}

		private boolean addOptionalDependencyOfPresentCommands(String sourceName, String targetName)
				throws CycleFoundException {
			return addOptionalDependencyOfPresentCommands(namesToCommandClasses.get(sourceName), namesToCommandClasses
					.get(targetName));
		}

		private boolean addOptionalDependencyOfPresentCommands(CommandClass source, CommandClass target)
				throws CycleFoundException {
			return graph.addDagEdge(source, target, new DependencyEdge(DependencyEdge.OPTIONAL));
		}

		// ***********************
		// ADD DEPENDENCIES END
		// ***********************

	}

	private static class DependencyEdge extends DefaultEdge {

		public static final boolean MANDATORY = true;
		public static final boolean OPTIONAL = false;
		private static final long serialVersionUID = 1357561909643656035L;

		private boolean mandatory;

		public DependencyEdge(boolean mandatory) {
			this.mandatory = mandatory;
		}

		public boolean isMandatory() {
			return mandatory;
		}

		public void setMandatory(boolean mandatory) {
			this.mandatory = mandatory;
		}

		@Override
		public String toString() {
			String mandatoryOrOptional = mandatory ? "Mandatory" : "Optional";
			return mandatoryOrOptional + " dependency:[" + super.getSource() + "] -> [" + super.getTarget() + "]";
		}

	}

}
