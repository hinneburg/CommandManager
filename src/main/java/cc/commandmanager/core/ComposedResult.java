package cc.commandmanager.core;

import java.util.Iterator;
import java.util.List;

import cc.commandmanager.core.ResultState.Failure;
import cc.commandmanager.core.ResultState.Warning;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * Represents a composite result state that is composed of partial {@linkplain ResultState}s. During an execution
 * workflow the final {@linkplain SimpleState} will reflect the most urgent {@linkplain ResultState} that came up in the
 * workflow. Highest urgency level is FAILURE, followed by WARNING, followed by SUCCESS.
 */
public final class ComposedResult {

	private SimpleState state = SimpleState.SUCCESS;
	private final List<String> executedCommands = Lists.newLinkedList();
	private final List<ResultState> partialResults = Lists.newLinkedList();

	/**
	 * @return most urgent execution result.
	 */
	public SimpleState getState() {
		return state;
	}

	@VisibleForTesting
	void addResult(String commandName, ResultState resultState) {
		executedCommands.add(commandName);
		partialResults.add(resultState);
		setOverallStateRespectfully(resultState);
	}

	private void setOverallStateRespectfully(ResultState resultState) {
		if (resultState instanceof Warning && state.equals(SimpleState.SUCCESS)) {
			state = SimpleState.WARNING;
		} else if (resultState instanceof Failure
				&& (state.equals(SimpleState.SUCCESS) || state.equals(SimpleState.WARNING))) {
			state = SimpleState.FAILURE;
		}
	}

	/**
	 * 
	 * @return all {@linkplan ResultState}s that were obtained while executing given {@link Command}s. Order of this
	 *         result corresponds to the sequence of the execution. Result will be immutable.
	 */
	public List<ResultState> getPartialResults() {
		return ImmutableList.copyOf(partialResults);
	}

	/**
	 * 
	 * @return all names of the {@link Command}s that were just executed. Order of this result corresponds to the
	 *         sequence of the execution. Result will be immutable.
	 */
	public List<String> getExecutedCommandNames() {
		return ImmutableList.copyOf(executedCommands);
	}

	/**
	 * @return {@link String} representation of the given state. Result will look like <br>
	 *         <code>Composed execution result: SUCCESS. Partial execution results: {NameOfCommand: Execution completed successfully!}</code>
	 */
	@Override
	public String toString() {
		String message = "Composed execution result: " + state + ". Partial execution results: {";
		Iterator<ResultState> result = partialResults.iterator();
		for (String command : executedCommands) {
			message += command + ": " + result.next();
			message += result.hasNext() ? ", " : "}";
		}
		return message;
	}

	public static enum SimpleState {
		SUCCESS, WARNING, FAILURE
	}

}
