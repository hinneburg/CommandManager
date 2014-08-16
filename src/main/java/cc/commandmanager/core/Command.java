package cc.commandmanager.core;

import java.util.Set;

public interface Command {

	ResultState execute(Context context);

    /**
     * @return names of {@linkplain cc.commandmanager.core.Command}s that need to be executed before this
     * {@linkplain cc.commandmanager.core.Command} can be executed.
     */
	Set<String> getBeforeDependencies();

    /**
     * @return names of {@linkplain cc.commandmanager.core.Command}s can be executed after this
     * {@linkplain cc.commandmanager.core.Command} was executed.
     */
	Set<String> getAfterDependencies();

    // TODO change this description when optional dependency behavior changes. See #53.
    /**
     * @return names of {@linkplain cc.commandmanager.core.Command}s that need to be executed before this
     * {@linkplain cc.commandmanager.core.Command} can be executed. Optional means that building a
     * {@linkplain cc.commandmanager.core.CommandGraph} does not fail when a dependency that is returned by this method
     * cannot be found in the catalog.
     * In future releases optional behavior will be changed.
     */
	Set<String> getOptionalBeforeDependencies();

    // TODO change this description when optional dependency behavior changes. See #53.
    /**
     * @return names of {@linkplain cc.commandmanager.core.Command}s can be executed after this
     * {@linkplain cc.commandmanager.core.Command} was executed. Optional means that building a
     * {@linkplain cc.commandmanager.core.CommandGraph} does not fail when a dependency that is returned by this method
     * cannot be found in the catalog.
     * In future releases optional behavior will be changed.
     */
	Set<String> getOptionalAfterDependencies();

}
