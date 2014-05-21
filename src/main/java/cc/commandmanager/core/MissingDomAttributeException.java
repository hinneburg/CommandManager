package cc.commandmanager.core;


public final class MissingDomAttributeException extends RuntimeException {

	private static final long serialVersionUID = -8375366289082678959L;

	public MissingDomAttributeException(String documentUri, String elementName, int attributePosition,
			String attributeName) {
		super(String.format("Missing attribute %s in DOM document %s. Missing at the &d. element with tag %s node",
				attributeName, documentUri, attributePosition + 1, elementName));
	}
}
