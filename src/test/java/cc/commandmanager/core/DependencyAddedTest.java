package cc.commandmanager.core;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Test;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public class DependencyAddedTest {

	@Test
	public void testSuccessStates() {
		assertThat(DependencyAdded.SUCCESS_STATES).containsOnly(DependencyAdded.ALREADY_PRESENT,
				DependencyAdded.OPTIONAL_OVERWRITTEN, DependencyAdded.MANDATORY_NOT_OVERWRITTEN,
				DependencyAdded.SUCCESSFUL);
	}

	@Test
	public void testFailureStates() {
		assertThat(DependencyAdded.FAILURE_STATES).containsOnly(DependencyAdded.CYCLE_DETECTED,
				DependencyAdded.COMMAND_MISSING);
	}

	@Test
	public void testAllStatesAreEitherFailureOrSuccess() {
		assertThat(ImmutableSet.copyOf(DependencyAdded.values())).isEqualTo(
				Sets.union(DependencyAdded.SUCCESS_STATES, DependencyAdded.FAILURE_STATES));
	}

	@Test
	public void testIsIn_isIn() {
		assertThat(DependencyAdded.SUCCESSFUL.isIn(DependencyAdded.SUCCESS_STATES)).isTrue();
	}

	@Test
	public void testIsIn_isNotIn() {
		assertThat(DependencyAdded.COMMAND_MISSING.isIn(DependencyAdded.SUCCESS_STATES)).isFalse();
	}

}
