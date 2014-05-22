package cc.commandmanager.core;

import java.util.Collection;

import net.sf.qualitycheck.Check;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.collect.Sets;

public class Catalog {

	private final Document catalogDocument;
	private final Iterable<String> commandNames;

	/**
	 * Create a new Catalog. Parse the XML document at the specified URL, registering named commands as they are
	 * encountered in the XML catalog file. Required node tag in the XML file is "command". Under those nodes the
	 * attributes "name" and "className" are required. "name" attribute represents the String alias under which a
	 * command can be found via {@link #getCommand(String)}. "className" attribute is the canonical name under which the
	 * class loader will look for the given class.
	 * 
	 * @param url
	 *            URL of the XML document to be parsed
	 * @throws MissingElementAttributeException
	 *             if the name or the className attribute are missing in the given DOM document.
	 */
	public static Catalog fromXmlFile(String fileUrl) {
		Check.notEmpty(fileUrl, "file url");
		// TODO Parse fileUrl to XML Document
		Document catalogDocument = null;
		return new Catalog(catalogDocument);
	}

	/**
	 * Create a new Catalog. Register named commands as they are encountered in the catalogDocument. Required node tag
	 * in the XML file is "command". Under those nodes the attributes "name" and "className" are required. "name"
	 * attribute represents the String alias under which a command can be found via {@link #getCommand(String)}.
	 * "className" attribute is the canonical name under which the class loader will look for the given class.
	 * 
	 * @throws MissingElementAttributeException
	 *             if the name or the className attribute are missing in the given DOM document.
	 */
	public static Catalog fromDomDocument(Document catalogDocument) {
		Check.notNull(catalogDocument);
		return new Catalog(catalogDocument);
	}

	private Catalog(Document catalogDocument) {
		this.catalogDocument = catalogDocument;
		commandNames = getCommandNamesFromDocument(this.catalogDocument);
		commands = getClassNamesFromDocument(this.catalogDocument);
	}

	private static Iterable<String> getCommandNamesFromDocument(Document catalogDocument) {
		Collection<String> commandNames = Sets.newHashSet();
		final String nameAttribute = "name";
		final String commandTag = "command";

		NodeList nodes = catalogDocument.getElementsByTagName(commandTag);
		for (int currentNode = 0; currentNode < nodes.getLength(); currentNode++) {
			Node node = nodes.item(currentNode);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element) node;
				if (element.hasAttribute(nameAttribute)) {
					commandNames.add(element.getAttribute(nameAttribute));
				} else {
					throw new MissingElementAttributeException(catalogDocument.getDocumentURI(), commandTag,
							currentNode, nameAttribute);
				}
			}
		}
		return commandNames;
	}

	private Map<String, Class<? extends Command>> getClassNamesFromDocument(Document catalogDocument2) {
		// TODO Implement method
		// TODO prevent multiple classNames for the same name
		return Maps.newHashMap();
	}

	/**
	 * Return an {@link Iterable} over the set of named commands known to this Catalog. If there are no known commands,
	 * an empty {@link Iterable} is returned.
	 * 
	 * @return an {@link Iterable} of the names in this Catalog.
	 * 
	 */
	public Iterable<String> getCommandNames() {
		return commandNames;
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
