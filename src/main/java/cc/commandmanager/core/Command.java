package cc.commandmanager.core;

import java.util.Set;

public interface Command {

	ResultState execute(Context context);

	Set<String> getBeforeDependencies();

	Set<String> getAfterDependencies();

	Set<String> getOptionalBeforeDependencies();

	Set<String> getOptionalAfterDependencies();

}
