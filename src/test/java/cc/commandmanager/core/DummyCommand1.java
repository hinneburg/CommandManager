package cc.commandmanager.core;

import java.util.List;

/**
 * Dummy {@link Command} implementation with no dependencies. {@link #execute(Context)} will bind a {@link Class} object
 * corresponding to this class to the context. A {@link ClassCastException} will be thrown if the context does not have
 * a {@link List<Class<? extends Command>>}. The list should be bound to the context with the key, specified in the
 * {@link CommandManagerIntegrationTest}.
 */
public final class DummyCommand1 extends SimpleCommand {

	@SuppressWarnings("unchecked")
	@Override
	public ResultState execute(Context context) {
		((List<Class<? extends Command>>) context.get(CommandManagerIntegrationTest.EXECUTED_COMMANDS)).add(this
				.getClass());
		return ResultState.success();
	}

}
