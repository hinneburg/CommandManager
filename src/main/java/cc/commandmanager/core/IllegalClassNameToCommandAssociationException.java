package cc.commandmanager.core;

public class IllegalClassNameToCommandAssociationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public IllegalClassNameToCommandAssociationException(String commandAlias, String documentUri) {
		String.format(
				"Multiple command elements with same %s attribute \"%s\", but differing className in document %s",
				Catalog.NAME_ATTRIBUTE, commandAlias, documentUri);
	}

}
