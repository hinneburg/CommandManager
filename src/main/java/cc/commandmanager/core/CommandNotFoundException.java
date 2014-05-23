package cc.commandmanager.core;

public class CommandNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public CommandNotFoundException(String commandName) {
		super("There is no command \"" + commandName + "\" in this catalog.");
	}

}
