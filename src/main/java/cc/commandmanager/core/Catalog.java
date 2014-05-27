package cc.commandmanager.core;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.sf.qualitycheck.Check;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class Catalog {

	static final String NAME_ATTRIBUTE = "name";
	static final String CLASS_NAME_ATTRIBUTE = "className";

	private static final String COMMAND_TAG = "command";
	private final Map<String, Class<? extends Command>> commands;

	/**
	 * Create a new Catalog. Parse the XML document at the specified URL, registering named commands as they are
	 * encountered in the XML catalog file. Required node tag in the XML file is "command". Under those nodes the
	 * attributes "name" and "className" are required. "name" attribute represents the String alias under which a
	 * command can be found via {@link #getCommand(String)}. "className" attribute is the class name under which the
	 * class loader will look for the given class. Both text nodes must not be empty.
	 * 
	 * @param url
	 *            URL of the XML document to be parsed
	 * @throws MissingElementAttributeException
	 *             if the name or the className attribute are missing in the given DOM document.
	 * @throws IllegalClassNameToCommandAssociationException
	 *             if the catalog document contains multiple different class names for the same command name.
	 * @throws CatalogDomFileHandlingException
	 *             if a {@linkplain javax.xml.parsers.DocumentBuilder} cannot be created which satisfies the
	 *             configuration requested (see com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl for
	 *             more details); or If any parse or IO errors occur
	 */
	public static Catalog fromXmlFile(String fileUrl) {
		Check.notEmpty(fileUrl, "file url");
		return fromDomDocument(tryToParseDomDocumentFromFile(fileUrl));
	}

	private static Document tryToParseDomDocumentFromFile(String fileUrl) {
		try {
			DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			return documentBuilder.parse(new File(fileUrl));
		} catch (SAXException e) {
			throw new CatalogDomFileHandlingException(fileUrl, e);
		} catch (IOException e) {
			throw new CatalogDomFileHandlingException(fileUrl, e);
		} catch (ParserConfigurationException e) {
			throw new CatalogDomFileHandlingException(e);
		}
	}

	/**
	 * Create a new Catalog. Register named commands as they are encountered in the catalogDocument. Required node tag
	 * in the XML file is "command". Under those nodes the attributes "name" and "className" are required. "name"
	 * attribute represents the String alias under which a command can be found via {@link #getCommand(String)}.
	 * "className" attribute is the class name under which the class loader will look for the given class. Both text
	 * nodes must not be empty.
	 * 
	 * @throws MissingElementAttributeException
	 *             if the name or the className attribute are missing in the given DOM document.
	 * @throws IllegalClassNameToCommandAssociationException
	 *             if the catalog document contains multiple different class names for the same command name.
	 */
	public static Catalog fromDomDocument(Document catalogDocument) {
		Check.notNull(catalogDocument);
		return fromMap(getCommandsFromCatalogDocument(catalogDocument));
	}

	/**
	 * Create a new Catalog. Register named commands as they are encountered in {@linkplain commands}.Any key represents
	 * the String alias under which a command can be found via {@link #getCommand(String)}. The corresponding values are
	 * the class names under which the class loader will look for a given class. No key nor any value must be empty.
	 * 
	 * @param commands
	 *            maps command names to their corresponding class name. The class to which the command name maps must
	 *            implement {@link Command}. Neither of them must be null or empty. The command names correspond to the
	 *            parameter of {@link #getCommand()}.
	 * @throws MissingElementAttributeException
	 *             if the name or the className attribute are missing in the given DOM document.
	 * @throws IllegalClassNameToCommandAssociationException
	 *             if the catalog document contains multiple different class names for the same command name.
	 */
	public static Catalog fromMap(Map<String, Class<? extends Command>> commands) {
		Check.notNull(commands, "commands");
		return new Catalog(commands);
	}

	private Catalog(Map<String, Class<? extends Command>> commands) {
		this.commands = commands;
	}

	private static Map<String, Class<? extends Command>> getCommandsFromCatalogDocument(Document catalogDocument) {
		final Map<String, Class<? extends Command>> commands = Maps.newHashMap();

		Iterable<Element> commandElements = getDomCommandElementsFromCatalogDocument(catalogDocument);
		int commandElementIndex = 0;
		for (Element commandElement : commandElements) {
			commandElementIndex++;

			checkCatalogElementHasNameAttribute(commandElement, commandElementIndex, catalogDocument);
			checkCatalogElementHasClassNameAttribute(commandElement, commandElementIndex, catalogDocument);
			String commandAlias = Check.notEmpty(commandElement.getAttribute(NAME_ATTRIBUTE), NAME_ATTRIBUTE);
			String commandClassName = Check.notEmpty(commandElement.getAttribute(CLASS_NAME_ATTRIBUTE),
					CLASS_NAME_ATTRIBUTE);

			Class<? extends Command> commandClass = tryToGetClassForName(commandClassName);
			if (isMappingAnotherClassAlready(commands, commandAlias, commandClass)) {
				throw new IllegalClassNameToCommandAssociationException(commandAlias, catalogDocument.getDocumentURI());
			}
			commands.put(commandAlias, commandClass);
		}
		return commands;
	}

	private static Iterable<Element> getDomCommandElementsFromCatalogDocument(Document catalogDocument) {
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

	private static void checkCatalogElementHasNameAttribute(Element commandElement, int positionOfElementInElementList,
			Document catalogDocument) {
		if (!commandElement.hasAttribute(NAME_ATTRIBUTE)) {
			throw new MissingElementAttributeException(catalogDocument.getDocumentURI(), COMMAND_TAG,
					positionOfElementInElementList, NAME_ATTRIBUTE);
		}
	}

	private static void checkCatalogElementHasClassNameAttribute(Element commandElement, int commandElementIndex,
			Document catalogDocument) {
		if (!commandElement.hasAttribute(CLASS_NAME_ATTRIBUTE)) {
			throw new MissingElementAttributeException(catalogDocument.getDocumentURI(), COMMAND_TAG,
					commandElementIndex, CLASS_NAME_ATTRIBUTE);
		}
	}

	private static Class<? extends Command> tryToGetClassForName(String commandClassName) {
		try {
			// TODO check for accordance explicitly
			return (Class<? extends Command>) Class.forName(commandClassName);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Cannot locate command class for name " + commandClassName, e);
		}
	}

	private static boolean isMappingAnotherClassAlready(Map<String, Class<? extends Command>> commands,
			String commandAlias, Class<? extends Command> commandClass) {
		return commands.containsKey(commandAlias) && !commands.get(commandAlias).equals(commandClass);
	}

	/**
	 * Return an {@link Iterable} over the set of named commands known to this Catalog. If there are no known commands,
	 * an empty {@link Iterable} is returned.
	 * 
	 * @return an {@link Iterable} of the names in this Catalog.
	 * 
	 */
	public Set<String> getCommandNames() {
		return Sets.newHashSet(commands.keySet());
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
		Check.notEmpty(commandName, "commandName");

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
