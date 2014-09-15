package cc.commandmanager.core;

/**
 * {@linkplain RuntimeException} indicating that an expected type of a stored value in a {@linkplain Context} did not
 * match the actual type.
 */
public class ResultTypeMismatchException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new {@linkplain ResultTypeMismatchException} having a custom message. The message contains the key of
	 * the bound value, the actual and the expected class of the value.
	 * 
	 * @param key
	 *            of the bound value
	 * @param actualClass
	 *            of the value
	 * @param expectedClass
	 *            of the value
	 */
	public ResultTypeMismatchException(Object key, Class<? extends Object> actualClass,
			Class<? extends Object> expectedClass) {
		super("Expected " + expectedClass + " but the context contained " + actualClass + " at key " + key);
	}

}
