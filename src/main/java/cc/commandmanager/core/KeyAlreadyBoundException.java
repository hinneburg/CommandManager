package cc.commandmanager.core;

public class KeyAlreadyBoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public KeyAlreadyBoundException(Object key) {
		super("There is already a value bound to key " + key);
	}

}
