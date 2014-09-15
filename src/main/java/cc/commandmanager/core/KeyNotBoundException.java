package cc.commandmanager.core;

/**
 * {@linkplain RuntimeException} indicating that there is no value bound to the given key.
 */
public class KeyNotBoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new {@linkplain KeyNotBoundException} having a custom message that contains the given key, to which no
	 * value is bound.
	 * 
	 * @param key
	 *            which has no value bound to
	 */
	public KeyNotBoundException(Object key) {
		super("There is no value bound to the key " + key);
	}
}
