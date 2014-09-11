package cc.commandmanager.core;

/**
 * {@linkplain RuntimeException} indicating that a given command expected to be in a {@linkplain CommandGraph} could not
 * be found.
 */
public class CommandNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new {@linkplain CommandNotFoundException} with a custom message containing the given command.
	 * 
	 * @param commandName
	 *            of the missing command
	 */
	public CommandNotFoundException(String commandName) {
		super("Command \"" + commandName + "\" cannot be found in the given command graph.");
	}

}
