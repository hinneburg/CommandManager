package cc.commandmanager.core;

/**
 * {@linkplain RuntimeException} thrown if a given command cannot be instantiated. It will wrap the exception that is
 * the reason for the error.
 */
public class CommandNotInstantiableException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new {@linkplain CommandNotInstantiableException} with a custom message containing the command class and
	 * the original reason for instantiation failure.
	 * 
	 * @param commandClassName
	 *            of the command that cannot be instantiated
	 * @param throwable
	 *            that is the cause of the instantiation failure
	 */
	public CommandNotInstantiableException(String commandClassName, Throwable throwable) {
		super("Command class " + commandClassName + " could not be instantiated: " + throwable.getMessage(), throwable);
	}

}
