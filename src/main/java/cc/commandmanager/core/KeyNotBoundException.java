package cc.commandmanager.core;

public class KeyNotBoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public KeyNotBoundException(Object key) {
		super("There is no value bound to the key " + key);
	}
}
