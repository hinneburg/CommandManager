package cc.commandmanager.core.commandimplementations;

import cc.commandmanager.core.Context;
import cc.commandmanager.core.ResultState;
import cc.commandmanager.core.SimpleCommand;

/**
 * Dummy {@link cc.commandmanager.core.Command} implementation with no dependencies.
 */
public final class CommandStillWithoutAnyDependencies extends SimpleCommand {

	@Override
	public ResultState execute(Context context) {
		return ResultState.success();
	}

}
