package cc.commandmanager.core;

import java.util.ArrayList;

import net.sf.qualitycheck.Check;

import org.w3c.dom.Document;

public class Catalog {

	private final Document catalogDocument;

	private Catalog(Document catalogDocument) {
		this.catalogDocument = catalogDocument;
	}

	/**
	 * Create a new Catalog. Parse the XML document at the specified URL, registering named commands as they are
	 * encountered in the XML catalog file.
	 * 
	 * @param url
	 *            URL of the XML document to be parsed
	 */
	public static Catalog fromXmlFile(String fileUrl) {
		Check.notNull(fileUrl);
		// TODO Parse fileUrl to XML Document
		Document catalogDocument = null;
		return new Catalog(catalogDocument);
	}

	/**
	 * Create a new Catalog. Register named commands as they are encountered in the catalogDocument.
	 */
	public static Catalog fromDomDocument(Document catalogDocument) {
		Check.notNull(catalogDocument);
		return new Catalog(catalogDocument);
	}

	/**
	 * Return an {@link Iterable} over the set of named commands known to this Catalog. If there are no known commands,
	 * an empty {@link Iterable} is returned.
	 * 
	 * @return an {@link Iterable} of the names in this Catalog.
	 * 
	 */
	public Iterable<String> getNames() {
		return new ArrayList<String>();
	}

	/**
	 * 
	 * @param name
	 *            Name for which a Command or Chain should be retrieved
	 * @return Command associated with the specified name, if any; otherwise, return null.
	 */
	public Command getCommand(String name) {
		return null;
	}
}
