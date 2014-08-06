package cc.commandmanager.core;

import java.util.List;
import java.util.Set;

import cc.commandmanager.core.Command;
import cc.commandmanager.core.Context;
import cc.commandmanager.core.ResultState;
import cc.commandmanager.core.SimpleCommand;
import com.google.common.collect.Sets;

/**
 * Dummy {@link cc.commandmanager.core.Command} implementation with a before dependency on {@link DummyCommand1}. {@link #execute(cc.commandmanager.core.Context)}
 * will bind a {@link Class} object corresponding to this class to the context. A {@link ClassCastException} will be
 * thrown if the context does not have a {@link List<Class<? extends  cc.commandmanager.core.Command >>}. The list should be bound to the context
 * with the key, specified in the {@link cc.commandmanager.core.CommandManagerIntegrationTest}.
 */
public final class DummyCommand2 extends SimpleCommand {

	@SuppressWarnings("unchecked")
	@Override
	public ResultState execute(Context context) {
		((List<Class<? extends Command>>) context.get(CommandManagerIntegrationTest.EXECUTED_COMMANDS)).add(this
				.getClass());
		return ResultState.success();
	}

	@Override
	public Set<String> getBeforeDependencies() {
		return Sets.newHashSet("command");
	}

}
