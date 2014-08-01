package cc.commandmanager.testutils;

import static org.fest.assertions.Fail.fail;
import net.sf.qualitycheck.Check;
import cc.commandmanager.core.ResultState;

public class Assertions {

	public static ResultStateAssertion assertThatExecution(ResultState resultState) {
		return new ResultStateAssertion(resultState);
	}

	public static class ResultStateAssertion {

		private final ResultState resultState;

		public ResultStateAssertion(ResultState resultState) {
			this.resultState = Check.notNull(resultState, "resultState");
		}

		public void isCompletedSuccessfully() {
			if (!resultState.isSuccess()) {
				fail(String.format("%s should be success", resultState));
			}
		}
	}

}
