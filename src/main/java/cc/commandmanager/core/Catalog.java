package cc.commandmanager.core;

import java.util.Collection;
import java.util.Map;

import net.sf.qualitycheck.Check;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class Catalog {

	static final String NAME_ATTRIBUTE = "name";
	static final String CLASS_NAME_ATTRIBUTE = "className";

	private static final String COMMAND_TAG = "command";
	private final Document catalogDocument;
	private final Map<String, Class<? extends Command>> commands;

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
		commands = getCommandClassesAndNamesFromDocument(this.catalogDocument);
	}

	private static Map<String, Class<? extends Command>> getCommandClassesAndNamesFromDocument(Document catalogDocument) {
		final Map<String, Class<? extends Command>> commands = Maps.newHashMap();

		Iterable<Element> commandElements = getCommandElements(catalogDocument);
		int elementCount = 0;
		for (Element commandElement : commandElements) {
			elementCount++;
			if (!commandElement.hasAttribute(NAME_ATTRIBUTE)) {
				throw new MissingElementAttributeException(catalogDocument.getDocumentURI(), COMMAND_TAG, elementCount,
						NAME_ATTRIBUTE);
			}
			String commandAlias = commandElement.getAttribute(NAME_ATTRIBUTE);

			if (!commandElement.hasAttribute(CLASS_NAME_ATTRIBUTE)) {
				throw new MissingElementAttributeException(catalogDocument.getDocumentURI(), COMMAND_TAG, elementCount,
						CLASS_NAME_ATTRIBUTE);
			}
			String commandClassName = commandElement.getAttribute(CLASS_NAME_ATTRIBUTE);

			Class<? extends Command> commandClass;
			try {
				// TODO check for accordance explicitly
				commandClass = (Class<? extends Command>) Class.forName(commandClassName);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException("Cannot locate command class for name " + commandClassName, e);
			}

			if (commands.containsKey(commandAlias) && !commands.get(commandAlias).equals(commandClass)) {
				throw new IllegalMultipleClassNamesInCatalogException(commandAlias, catalogDocument.getDocumentURI());
			}
			commands.put(commandAlias, commandClass);
		}
		return commands;
	}

	private static Iterable<Element> getCommandElements(Document catalogDocument) {
		Collection<Element> commandElements = Lists.newArrayList();

		NodeList nodes = catalogDocument.getElementsByTagName(COMMAND_TAG);
		for (int currentNode = 0; currentNode < nodes.getLength(); currentNode++) {
			Node node = nodes.item(currentNode);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				commandElements.add((Element) node);
			}
		}

		return commandElements;
	}

	/**
	 * Return an {@link Iterable} over the set of named commands known to this Catalog. If there are no known commands,
	 * an empty {@link Iterable} is returned.
	 * 
	 * @return an {@link Iterable} of the names in this Catalog.
	 * 
	 */
	public Iterable<String> getCommandNames() {
		return commands.keySet();
	}

	/**
	 * 
	 * @param commandName
	 *            Name for which a Command or Chain should be retrieved
	 * @return Command associated with the specified name, if any; otherwise, a {@link CommandNotFoundException} will be
	 *         thrown.
	 * @throws CommandNotFoundException
	 *             if no command can be found in this catalog for the given command name.
	 * @throws CommandNotInstantiableException
	 *             if the command class or its nullary constructor is not accessible; or if the command class represents
	 *             an abstract class, an interface, an array class, a primitive type, or void; or if the command class
	 *             has no nullary constructor; or if the instantiation fails for some other reason.
	 */
	public Command getCommand(String commandName) {
		Check.notEmpty(commandName, "command name");

		if (!commands.containsKey(commandName)) {
			throw new CommandNotFoundException(commandName);
		}

		try {
			return commands.get(commandName).newInstance();
		} catch (IllegalAccessException exception) {
			throw new CommandNotInstantiableException(commands.get(commandName), exception);
		} catch (InstantiationException exception) {
			throw new CommandNotInstantiableException(commands.get(commandName), exception);
		}
	}

}
