package cc.commandmanager.core;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import net.sf.qualitycheck.Check;
import net.sf.qualitycheck.exception.IllegalEmptyArgumentException;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 * Represents a composite result state that is composed of {@linkplain ResultState}s from {@linkplain Command}
 * executions. The overall result state is the highest one present in the given states. The order is FAILURE, WARNING
 * followed by SUCCESS.
 * <p>
 * So if there is at least one warning in the given result states, the overall state will be warning. Same applies to
 * failure results.
 */
public final class ComposedResultState extends ResultState {

	private final ResultState overallState;
	private final ImmutableList<CommandClass> executedCommands;
	private final ImmutableList<ResultState> resultStates;

	/**
	 * Create a new {@linkplain ComposedResultState} from the given result states and command classes. The elements from
	 * both iterables correspond to each other so that the result state at index <i>i</i> was returned by the command at
	 * index <i>i</i>.
	 * 
	 * @param resultStates
	 * @param executedCommands
	 */
	public ComposedResultState(Iterable<ResultState> resultStates, Iterable<CommandClass> executedCommands) {
		if (!resultStates.iterator().hasNext()) {
			// TODO wait for https://github.com/before/quality-check/pull/19#event-142137188 to be in release
			throw new IllegalEmptyArgumentException("resultStates");
		}
		if (!executedCommands.iterator().hasNext()) {
			// TODO wait for https://github.com/before/quality-check/pull/19#event-142137188 to be in release
			throw new IllegalEmptyArgumentException("executedCommands");
		}
		Check.equals(Iterables.size(resultStates), Iterables.size(executedCommands),
				"Number of executions results and executed commands differ.");

		this.executedCommands = ImmutableList.copyOf(executedCommands);
		this.resultStates = ImmutableList.copyOf(resultStates);

		ResultState overallState = null;
		for (ResultState resultState : resultStates) {
			overallState = updateOverallState(resultState, overallState);
		}
		this.overallState = overallState;
	}

	private static ResultState updateOverallState(ResultState resultState, @Nullable ResultState overallState) {
		if (overallState == null) {
			return resultState;
		}

		if ((resultState.isSuccess() && !overallState.isSuccess() && !overallState.isFailure() && !overallState
				.isWarning())
				|| (resultState.isWarning() && !overallState.isFailure() && !overallState.isWarning())
				|| (resultState.isFailure() && !overallState.isFailure())) {
			return resultState;
		} else {
			return overallState;
		}
	}

	/**
	 * 
	 * @return all {@linkplain ResultState}s that were obtained while executing given {@link Command}s. Order of this
	 *         result corresponds to the sequence of the execution.
	 */
	public List<ResultState> getResultStates() {
		return resultStates;
	}

	/**
	 * 
	 * @return all names of the {@link Command}s that were just executed. Order of this result corresponds to the
	 *         sequence of the execution. Result will be immutable.
	 */
	public List<CommandClass> getExecutedCommands() {
		return executedCommands;
	}

	@Override
	public boolean isSuccess() {
		return overallState.isSuccess();
	}

	@Override
	public boolean isWarning() {
		return overallState.isWarning();
	}

	@Override
	public boolean isFailure() {
		return overallState.isFailure();
	}

	@Override
	public String getMessage() {
		return overallState.getMessage();
	}

	@Override
	@Nullable
	public Throwable getCause() {
		return overallState.getCause();
	}

	@Override
	public String toString() {
		String message = "[Overall result state: " + overallState + "; Composed execution results: ";
		Iterator<ResultState> result = resultStates.iterator();
		for (CommandClass command : executedCommands) {
			message += command + ": " + result.next();
			message += result.hasNext() ? ", " : "]";
		}
		return message;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((executedCommands == null) ? 0 : executedCommands.hashCode());
		result = prime * result + ((resultStates == null) ? 0 : resultStates.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof ComposedResultState)) {
			return false;
		}
		ComposedResultState other = (ComposedResultState) obj;
		if (executedCommands == null) {
			if (other.executedCommands != null) {
				return false;
			}
		} else if (!executedCommands.equals(other.executedCommands)) {
			return false;
		}
		if (resultStates == null) {
			if (other.resultStates != null) {
				return false;
			}
		} else if (!resultStates.equals(other.resultStates)) {
			return false;
		}
		return true;
	}

}
