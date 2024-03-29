package cc.commandmanager.core;

import java.util.Set;

/**
 * A {@linkplain Command} is a single unit of an execution graph. It will be instantiated by the
 * {@linkplain CommandManager}, who calls the {@linkplain Command#execute(Context)} method.
 * <p>
 * A given context will be passed on throughout the execution. It can be used to interchange data between the commands.
 * Dependencies are specified through the {@code get*Dependencies()} methods.
 */
public interface Command {

	/**
	 * Method containing the actual operations that a command performs. The given context is used to read data from and
	 * store data for other commands.
	 * 
	 * @param context
	 *            to read from and write data to
	 * @return whether the execution was successful
	 */
	ResultState execute(Context context);

	/**
	 * @return names of {@linkplain Command}s that need to be executed before this {@linkplain Command} can be executed.
	 */
	Set<String> getBeforeDependencies();

	/**
	 * @return names of {@linkplain Command}s can be executed after this {@linkplain Command} was executed.
	 */
	Set<String> getAfterDependencies();

	// TODO change this description when optional dependency behavior changes. See #53.
	/**
	 * @return names of {@linkplain Command}s that need to be executed before this {@linkplain Command} can be executed.
	 *         Optional means that building a {@linkplain CommandGraph} does not fail when a dependency that is returned
	 *         by this method cannot be found in the catalog. In future releases optional behavior will be changed.
	 */
	Set<String> getOptionalBeforeDependencies();

	// TODO change this description when optional dependency behavior changes. See #53.
	/**
	 * @return names of {@linkplain Command}s can be executed after this {@linkplain Command} was executed. Optional
	 *         means that building a {@linkplain CommandGraph} does not fail when a dependency that is returned by this
	 *         method cannot be found in the catalog. In future releases optional behavior will be changed.
	 */
	Set<String> getOptionalAfterDependencies();

}
