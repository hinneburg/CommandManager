package cc.commandmanager.core.commandimplementations;

import cc.commandmanager.core.*;

import java.util.List;

/**
 * Dummy {@link cc.commandmanager.core.Command} implementation with no dependencies.
 */
public final class CommandWithoutAnyDependencies extends SimpleCommand {

	@Override
	public ResultState execute(Context context) {
		return ResultState.success();
	}

}
