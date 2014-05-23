package cc.commandmanager.core;

import javax.xml.parsers.ParserConfigurationException;

public class CatalogDomFileHandlingException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public CatalogDomFileHandlingException(String catalogFileUrl, Throwable throwable) {
		super("Handling catalog file with url " + catalogFileUrl + " failed: " + throwable.getMessage(), throwable);
	}

	public CatalogDomFileHandlingException(ParserConfigurationException throwable) {
		super("Required parser for handling a catalog file could not be created, due to configuration problems: "
				+ throwable.getMessage(), throwable);
	}

}
