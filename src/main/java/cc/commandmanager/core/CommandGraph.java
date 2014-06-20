package cc.commandmanager.core;

import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.qualitycheck.Check;

import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph.CycleFoundException;
import org.jgrapht.graph.DefaultEdge;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

/**
 * A graph to represent an amount of {@link Command}s and the dependency relationship between them. A
 * {@linkplain CommandGraph} contains a set V of {@link CommandClass} vertices and a set E of edges which represent
 * dependencies. Each dependency can either be mandatory or optional. Each edge e=(v1,v2) in E connects vertex v1 to
 * vertex v2. For more information about graphs and their related definitions see <a
 * href="http://mathworld.wolfram.com/Graph.html">Wolfram Mathworld</a>.
 * <p>
 * Furthermore instances of this graph are
 * <li>
 * directed
 * <li>
 * acyclic
 * <li>immutable and
 * <li>topologically sorted.
 */
public class CommandGraph {

	private final DirectedAcyclicGraph<CommandClass, DependencyEdge> commandGraph;
	private final Map<String, CommandClass> vertices;

	@SuppressWarnings("unchecked")
	private CommandGraph(CommandGraphBuilder builder) {
		commandGraph = (DirectedAcyclicGraph<CommandClass, DependencyEdge>) builder.graph.clone();
		vertices = Maps.newHashMap(builder.namesToCommandClasses);
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

	/**
	 * 
	 * @param commandName
	 * @return A {@code CommandClass} object having the given {@code commandName}.
	 * @throws CommandNotFoundException
	 *             if no command can be found in this graph for the given {@code commandName}.
	 */
	public CommandClass getCommandClass(String commandName) {
		Check.notNull(commandName, "commandName");
		if (!hasCommand(commandName)) {
			throw new CommandNotFoundException(commandName);
		}
		return vertices.get(commandName);
	}

	/**
	 * For a given {@code commandName} find all commands on which this command is dependent on. Mandatory dependencies
	 * will be returned as well as optional dependencies.
	 * 
	 * @param commandName
	 * @return Commands on which the given command is dependent on. Commands are not explicitly ordered by their
	 *         dependency type.
	 * @throws CommandNotFoundException
	 *             if no command can be found in this graph for the given {@code commandName}.
	 */
	public List<CommandClass> getDependencies(String commandName) {
		return ImmutableList.copyOf(Iterables.concat(getMandatoryDependencies(commandName),
				getOptionalDependencies(commandName)));
	}

	/**
	 * For a given {@code commandName} find all commands on which this command is mandatorily dependent on.
	 * 
	 * @param commandName
	 * @return Commands on which the given command is mandatorily dependent on.
	 * @throws CommandNotFoundException
	 *             if no command can be found in this graph for the given {@code commandName}.
	 */
	public List<CommandClass> getMandatoryDependencies(String commandName) {
		Check.notNull(commandName, "commandName");
		if (!hasCommand(commandName)) {
			throw new CommandNotFoundException(commandName);
		}
		return getDependenciesWithRequirementState(commandName, DependencyEdge.MANDATORY);
	}

	/**
	 * For a given {@code commandName} find all commands on which this command is optionally dependent on.
	 * 
	 * @param commandName
	 * @return Commands on which the given command is optionally dependent on.
	 * @throws CommandNotFoundException
	 *             if no command can be found in this graph for the given {@code commandName}.
	 */
	public List<CommandClass> getOptionalDependencies(String commandName) {
		Check.notNull(commandName, "commandName");
		if (!hasCommand(commandName)) {
			throw new CommandNotFoundException(commandName);
		}
		return getDependenciesWithRequirementState(commandName, DependencyEdge.OPTIONAL);
	}

	private ImmutableList<CommandClass> getDependenciesWithRequirementState(String commandName,
			boolean mandatoryRequired) {
		ImmutableList.Builder<CommandClass> result = ImmutableList.builder();
		Set<DependencyEdge> dependencies = commandGraph.outgoingEdgesOf(vertices.get(commandName));
		for (DependencyEdge dependency : dependencies) {
			if (dependency.isMandatory() == mandatoryRequired) {
				result.add(commandGraph.getEdgeTarget(dependency));
			}
		}
		return result.build();
	}

	/**
	 * A mutable representation of {@code Command}s, represented by related {@code CommandClass}es, and the relationship
	 * between them, represented by mandatory and optional dependencies, respectively. This class is designed to build
	 * an immutable {@link CommandGraph} from scratch.
	 */
	public static class CommandGraphBuilder {

		private Map<String, CommandClass> namesToCommandClasses = Maps.newHashMap();
		private DirectedAcyclicGraph<CommandClass, DependencyEdge> graph = new DirectedAcyclicGraph<CommandClass, DependencyEdge>(
				DependencyEdge.class);

		/**
		 * @return A new and immutable {@link CommandGraph} containing all commands and dependencies just added.
		 */
		public CommandGraph build() {
			return new CommandGraph(this);
		}

		/**
		 * Add a command with the given {@code name} and the given {@code className} to this builder.
		 * 
		 * @param name
		 *            must not be contained in this builder, yet.
		 * @param className
		 * @return {@code true} if the command could be added, {@code false} if the command could not be added because
		 *         another command previously was added whos name equals the given {@code name}.
		 */
		public boolean addCommand(String name, String className) {
			return addCommand(new CommandClass(Check.notNull(name, "name"), Check.notNull(className, "className")));
		}

		/**
		 * Add a command with the given {@code name} and the given {@code className} to this builder.
		 * 
		 * @param commandClass
		 *            name of {@code commandClass} must not be contained in this builder, yet.
		 * @return {@code true} if the command could be added, {@code false} if the command could not be added because
		 *         another command previously was added whos name equals the name of the given {@code commandClass}.
		 */
		public boolean addCommand(CommandClass commandClass) {
			Check.notNull(commandClass, "commandClass");
			if (isAlreadyPresent(commandClass)) {
				return false;
			}

			namesToCommandClasses.put(commandClass.getName(), commandClass);
			return graph.addVertex(commandClass);
		}

		private boolean isAlreadyPresent(String commandName) {
			return namesToCommandClasses.containsKey(commandName);
		}

		private boolean isAlreadyPresent(CommandClass commandClass) {
			return isAlreadyPresent(commandClass.getName());
		}

		/**
		 * Add a mandatory dependency from {@code sourceName} to {@code targetName} IFF <li>both, source and target have
		 * been added already <li>the given edge is not already a member of the graph <li>there has not been added a
		 * mandatory edge from {@code sourceName} to {@code targetName}, yet<li>
		 * the edge does not induce a circular dependency.
		 * <p>
		 * If an optional dependency between source and target already exists, the dependency state will be changed from
		 * optional to mandatory.
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
		 * Add a mandatory dependency from {@code source} to {@code target} IFF <li>both, source and target have been
		 * added already <li>the given edge is not already a member of the graph <li>there has not been added a
		 * mandatory edge from {@code source} to {@code target}, yet<li>
		 * the edge does not induce a circular dependency.
		 * <p>
		 * If an optional dependency between source and target already exists, the dependency state will be changed from
		 * optional to mandatory.
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
		 * Add an optional dependency from {@code sourceName} to {@code targetName} IFF <li>both, source and target have
		 * been added already <li>the given edge is not already a member of the graph <li>there has neither been added a
		 * mandatory nor an optional edge from {@code sourceName} to {@code targetName}, yet<li>
		 * the edge does not induce a circular dependency.
		 * <p>
		 * If a mandatory dependency between source and target already exists, the dependency state will not be changed
		 * but remains mandatory.
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
		 * Add an optional dependency from {@code source} to {@code target} IFF <li>both, source and target have been
		 * added already <li>the given edge is not already a member of the graph <li>there has neither been added a
		 * mandatory nor an optional edge from {@code source} to {@code target}, yet<li>
		 * the edge does not induce a circular dependency.
		 * <p>
		 * If a mandatory dependency between source and target already exists, the dependency state will not be changed
		 * but remains mandatory.
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

	}

	private static class DependencyEdge extends DefaultEdge {

		public static final boolean MANDATORY = true;
		public static final boolean OPTIONAL = !MANDATORY;
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
			String mandatoryOrOptional = isMandatory() ? "Mandatory" : "Optional";
			return mandatoryOrOptional + " dependency:[" + super.getSource() + "] -> [" + super.getTarget() + "]";
		}

	}

}
