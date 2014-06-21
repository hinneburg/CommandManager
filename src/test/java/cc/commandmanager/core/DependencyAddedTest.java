package cc.commandmanager.core;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Test;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public class DependencyAddedTest {

	@Test
	public void testSuccessStates() {
		assertThat(DependencyAdded.SUCCESS_STATES).containsOnly(DependencyAdded.ALREADY_PRESENT,
				DependencyAdded.OPTIONAL_OVERWRITTEN, DependencyAdded.SUCCESSFUL);
	}

	@Test
	public void testFailureStates() {
		assertThat(DependencyAdded.FAILURE_STATES).containsOnly(DependencyAdded.CYCLE_DETECTED,
				DependencyAdded.COMMAND_MISSING, DependencyAdded.MANDATORY_NOT_OVERWRITTEN);
	}

	@Test
	public void testAllStatesAreEitherFailureOrSuccess() {
		assertThat(ImmutableSet.copyOf(DependencyAdded.values())).isEqualTo(
				Sets.union(DependencyAdded.SUCCESS_STATES, DependencyAdded.FAILURE_STATES));
	}

}
