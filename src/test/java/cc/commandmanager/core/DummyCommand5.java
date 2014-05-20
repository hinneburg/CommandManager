package cc.commandmanager.core;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class DummyCommand5 implements Command {

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
