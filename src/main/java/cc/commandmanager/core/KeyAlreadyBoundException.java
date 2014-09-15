package cc.commandmanager.core;

/**
 * {@linkplain RuntimeException} indicating that a {@linkplain Context} already contained the given key, so it must be
 * explicitly rebound using {@linkplain Context#rebind(Object, Object)}.
 */
public class KeyAlreadyBoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new {@linkplain KeyAlreadyBoundException} having a custom message that contains the given key, which
	 * was already used.
	 * 
	 * @param key
	 *            already bound
	 */
	public KeyAlreadyBoundException(Object key) {
		super("There is already a value bound to key " + key);
	}

}
