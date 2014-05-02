package cc.commandmanager.core;

import java.util.Set;

import net.sf.qualitycheck.Check;

import org.apache.commons.chain.impl.ContextBase;

public class DependencyContext extends ContextBase {

	private static final long serialVersionUID = 5551810374358381131L;

	protected Set<String> afterDependencies;
	protected Set<String> beforeDependencies;
	protected Set<String> optionalAfterDependencies;
	protected Set<String> optionalBeforeDependencies;

	public void setAfterDependencies(Set<String> dependencies) {
		afterDependencies = Check.notNull(dependencies, "dependencies");
	}

	public Set<String> getAfterDependencies() {
		return afterDependencies;
	}

	public void setBeforeDependencies(Set<String> dependencies) {
		beforeDependencies = Check.notNull(dependencies, "dependencies");
	}

	public Set<String> getBeforeDependencies() {
		return beforeDependencies;
	}

	public void setOptionalAfterDependencies(Set<String> dependencies) {
		optionalAfterDependencies = dependencies;
	}

	public Set<String> getOptionalAfterDependencies() {
		return optionalAfterDependencies;
	}

	public void setOptionalBeforeDependencies(Set<String> dependencies) {
		optionalBeforeDependencies = Check.notNull(dependencies, "dependencies");
	}

	public Set<String> getOptionalBeforeDependencies() {
		return optionalBeforeDependencies;
	}

}
