package cc.commandmanager.core;

import java.util.Collections;
import java.util.Set;

/**
 * A simple {@linkplain Command} that does not have any dependencies.
 */
public abstract class SimpleCommand implements Command {

	@Override
	public Set<String> getAfterDependencies() {
		return Collections.emptySet();
	}

	@Override
	public Set<String> getBeforeDependencies() {
		return Collections.emptySet();
	}

	@Override
	public Set<String> getOptionalAfterDependencies() {
		return Collections.emptySet();
	}

	@Override
	public Set<String> getOptionalBeforeDependencies() {
		return Collections.emptySet();
	}

}
