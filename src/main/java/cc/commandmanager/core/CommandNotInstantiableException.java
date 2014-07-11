package cc.commandmanager.core;

public class CommandNotInstantiableException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public CommandNotInstantiableException(String commandClassName, Throwable throwable) {
		super("Command class " + commandClassName + " could not be instantiated: " + throwable.getMessage(), throwable);
	}

}
