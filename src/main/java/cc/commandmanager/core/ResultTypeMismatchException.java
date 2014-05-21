package cc.commandmanager.core;

public class ResultTypeMismatchException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ResultTypeMismatchException(Class<? extends Object> objectClass, Class<? extends Object> expectedClass) {
		super("Expected " + expectedClass + " but the context contained " + objectClass);
	}
}
