package cc.commandmanager.core;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;

/**
 * Dummy {@link Command} implementation with an optional before dependency on {@link DummyCommand1}.
 * {@link #execute(Context)} will bind a {@link Class} object corresponding to this class to the context. A
 * {@link ClassCastException} will be thrown if the context does not have a {@link List<Class<? extends Command>>}. The
 * list should be bound to the context with the key, specified in the {@link CommandManagementIntegrationTest}.
 */
public final class DummyCommand4 extends SimpleCommand {

	@SuppressWarnings("unchecked")
	@Override
	public ResultState execute(Context context) {
		((List<Class<? extends Command>>) context.get(CommandManagementIntegrationTest.EXECUTED_COMMANDS)).add(this
				.getClass());
		return ResultState.success();
	}

	@Override
	public Set<String> getOptionalBeforeDependencies() {
		return Sets.newHashSet("command");
	}

}
