package cc.commandmanager.core;

import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public enum DependencyAdded {

	SUCCESSFUL, OPTIONAL_OVERWRITTEN, MANDATORY_NOT_OVERWRITTEN, CYCLE_DETECTED, ALREADY_PRESENT, COMMAND_MISSING;

	public static Set<DependencyAdded> SUCCESS_STATES = ImmutableSet.of(SUCCESSFUL, OPTIONAL_OVERWRITTEN,
			ALREADY_PRESENT);
	public static Set<DependencyAdded> FAILURE_STATES = Sets.difference(ImmutableSet.copyOf(DependencyAdded.values()),
			SUCCESS_STATES).immutableCopy();

}
