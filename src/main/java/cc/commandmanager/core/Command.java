package cc.commandmanager.core;

import java.util.Set;

/**
 * This class extends a commands functionality by adding dependencies. Every command extending this class needs to
 * implement a specialExecute, which will act as the normal execute of a org.apache.commons.chain.Command.
 * <p>
 * If the execute is invoked with a DependencyContext the addDependencies method is called and the dependencies are set,
 * otherwise the specialExecute is invoked.
 * <p>
 * If a command has dependencies to other commands, those can be specified in the afterDependencies, beforeDependencies,
 * optionalAfterDependencies and optionalBeforeDependencies variables. For doing this the addDependencies method should
 * be overwritten. Then a sample addDependencies could contain the following lines:
 * <p>
 * beforeDependencies.add("A"); afterDependencies.add("C");
 * <p>
 * This means that the actual command needs command A to be executed, and command C needs the actual command to be
 * executed before itself.
 */

public interface Command {

	void execute(Context context);

	Set<String> getBeforeDependencies();

	Set<String> getAfterDependencies();

	Set<String> getOptionalBeforeDependencies();

	Set<String> getOptionalAfterDependencies();

}
