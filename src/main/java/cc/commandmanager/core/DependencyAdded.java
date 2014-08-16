package cc.commandmanager.core;

import java.util.Set;

import net.sf.qualitycheck.Check;
import cc.commandmanager.core.CommandGraph.CommandGraphBuilder;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

/**
 * Result state indicating whether a dependency was added successfully to a {@linkplain CommandGraphBuilder}.
 */
public enum DependencyAdded {

	/**
	 * Dependency added successfully.
	 */
	SUCCESSFUL("Dependency added successfully."),

	/**
	 * Mandatory dependency added and overwrote an existing optional one.
	 */
	OPTIONAL_OVERWRITTEN("Mandatory dependency added and overwrote an existing optional one."),

	/**
	 * Tried to add an optional dependency where a mandatory one was already present.
	 */
	MANDATORY_NOT_OVERWRITTEN("Tried to add an optional dependency where a mandatory one was already present."),

	/**
	 * Tried to add a dependency that would have introduced a cycle.
	 */
	CYCLE_DETECTED("Tried to add a dependency that would have introduced a cycle."),

	/**
	 * Tried to add a dependency that is already present. Nothing happened.
	 */
	ALREADY_PRESENT("Tried to add a dependency that is already present. Nothing happened."),

	/**
	 * Tried to add a dependency between commands of which one or both do not exist in the
	 * {@linkplain CommandGraphBuilder}.
	 */
	COMMAND_MISSING("Tried to add a dependency between commands of which one or both do not exist in the graph.");

	/**
	 * {@linkplain Set} of states indicating that adding the dependency did work or at least was not canceled because it
	 * would do any harm.
	 */
	public static Set<DependencyAdded> SUCCESS_STATES = ImmutableSet.of(SUCCESSFUL, OPTIONAL_OVERWRITTEN,
			ALREADY_PRESENT, MANDATORY_NOT_OVERWRITTEN);

	/**
	 * {@linkplain Set} of states indicating that adding the dependency was not successful because it violated the
     * contract of a {@linkplain CommandGraph}. Difference of all {@linkplain cc.commandmanager.core.DependencyAdded}
     * states and {@linkplain cc.commandmanager.core.DependencyAdded#SUCCESS_STATES}.
     */
	public static Set<DependencyAdded> FAILURE_STATES = Sets.difference(ImmutableSet.copyOf(DependencyAdded.values()),
			SUCCESS_STATES).immutableCopy();

	/**
	 * Returns whether this result state is part of the given state group.
	 * 
	 * @param states
	 */
	public boolean isIn(Set<DependencyAdded> states) {
		Check.notNull(states, "states");
		return states.contains(this);
	}

	private final String message;

	private DependencyAdded(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return message;
	}

}
