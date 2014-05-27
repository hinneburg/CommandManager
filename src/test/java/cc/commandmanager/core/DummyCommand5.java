package cc.commandmanager.core;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Dummy {@link Command} implementation with no dependencies. {@link #execute(Context)} will bind a {@link Class} object
 * corresponding to this class to the context. A {@link ClassCastException} will be thrown if the context does not have
 * a {@link List<Class<? extends Command>>}. The list should be bound to the context with the key, specified in the
 * {@link CommandManagementIntegrationTest}.
 */
public final class DummyCommand5 implements Command {

	@SuppressWarnings("unchecked")
	@Override
	public void execute(Context context) {
		((List<Class<? extends Command>>) context.get(CommandManagementIntegrationTest.EXECUTED_COMMANDS)).add(this
				.getClass());
	}

	@Override
	public Set<String> getBeforeDependencies() {
		return new HashSet<String>();
	}

	@Override
	public Set<String> getAfterDependencies() {
		return new HashSet<String>();
	}

	@Override
	public Set<String> getOptionalBeforeDependencies() {
		return new HashSet<String>();
	}

	@Override
	public Set<String> getOptionalAfterDependencies() {
		return new HashSet<String>();
	}
}
