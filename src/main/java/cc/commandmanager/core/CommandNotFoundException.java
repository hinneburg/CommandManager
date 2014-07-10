package cc.commandmanager.core;

public class CommandNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public CommandNotFoundException(String commandName) {
		super("Command \"" + commandName + "\" cannot be found in the given command graph.");
	}

}
