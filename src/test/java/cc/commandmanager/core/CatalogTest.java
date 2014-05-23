package cc.commandmanager.core;

import static org.fest.assertions.Assertions.assertThat;

import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.sf.qualitycheck.exception.IllegalEmptyArgumentException;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class CatalogTest {

	private Catalog catalog = null;
	private Document catalogDocument = createBaseCatalogDocument();

	private static Document createBaseCatalogDocument() {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
		Document catalogDocument = dBuilder.newDocument();
		return catalogDocument;
	}

	@Test(expected = MissingElementAttributeException.class)
	public void testCreateCatalog_missingElementAttributeException() {
		Element documentRoot = catalogDocument.createElement("catalog");
		Element commandWithoutNameAttribute = catalogDocument.createElement("command");
		documentRoot.appendChild(commandWithoutNameAttribute);
		catalogDocument.appendChild(documentRoot);

		catalog = Catalog.fromDomDocument(catalogDocument);
	}

	@Test(expected = IllegalEmptyArgumentException.class)
	public void testCreateCatalog_illegalEmptyClassName() {
		Element documentRoot = catalogDocument.createElement("catalog");

		Element command1 = catalogDocument.createElement("command");
		command1.setAttribute("className", "");
		command1.setAttribute("name", "Command1");
		documentRoot.appendChild(command1);
		catalogDocument.appendChild(documentRoot);

		catalog = Catalog.fromDomDocument(catalogDocument);
	}

	@Test(expected = IllegalEmptyArgumentException.class)
	public void testCreateCatalog_illegalEmptyName() {
		Element documentRoot = catalogDocument.createElement("catalog");

		Element command1 = catalogDocument.createElement("command");
		command1.setAttribute("className", "cc.commandmanager.core.CatalogTest$Command1");
		command1.setAttribute("name", "");
		documentRoot.appendChild(command1);
		catalogDocument.appendChild(documentRoot);

		catalog = Catalog.fromDomDocument(catalogDocument);
	}

	@Test(expected = IllegalClassNameToCommandAssociationException.class)
	public void testCreateCatalogThrowsException_illegalClassNameToCommandAssociationException() {
		Element documentRoot = catalogDocument.createElement("catalog");

		Element command1 = catalogDocument.createElement("command");
		command1.setAttribute("className", "cc.commandmanager.core.CatalogTest$Command1");
		command1.setAttribute("name", "Command1");
		documentRoot.appendChild(command1);

		Element command2 = catalogDocument.createElement("command");
		command2.setAttribute("className", "cc.commandmanager.core.CatalogTest$Command2");
		command2.setAttribute("name", "Command1");
		documentRoot.appendChild(command2);

		catalogDocument.appendChild(documentRoot);

		catalog = Catalog.fromDomDocument(catalogDocument);
	}

	@Test
	public void testGetNames() throws ParserConfigurationException {
		Element documentRoot = catalogDocument.createElement("catalog");

		Element command1 = catalogDocument.createElement("command");
		command1.setAttribute("className", "cc.commandmanager.core.CatalogTest$Command1");
		command1.setAttribute("name", "Command1");
		documentRoot.appendChild(command1);
		catalogDocument.appendChild(documentRoot);

		catalog = Catalog.fromDomDocument(catalogDocument);
		assertThat(catalog.getCommandNames()).containsOnly("Command1");

		Element command2 = catalogDocument.createElement("command");
		command2.setAttribute("className", "cc.commandmanager.core.CatalogTest$Command2");
		command2.setAttribute("name", "Command2");
		documentRoot.appendChild(command2);

		catalog = Catalog.fromDomDocument(catalogDocument);
		assertThat(catalog.getCommandNames()).containsOnly("Command1", "Command2");
	}

	@Test
	public void testGetCommand() {
		Element documentRoot = catalogDocument.createElement("catalog");

		Element command1 = catalogDocument.createElement("command");
		command1.setAttribute("className", "cc.commandmanager.core.CatalogTest$Command1");
		command1.setAttribute("name", "Command1");
		documentRoot.appendChild(command1);

		catalogDocument.appendChild(documentRoot);
		catalog = Catalog.fromDomDocument(catalogDocument);

		String actual = catalog.getCommand("Command1").getClass().getName();
		assertThat(actual).isEqualTo("cc.commandmanager.core.CatalogTest$Command1");
	}

	@Test(expected = CommandNotFoundException.class)
	public void testGetCommand_commandNotFound() {
		Element documentRootWithoutAnyCommandChilds = catalogDocument.createElement("catalog");

		catalogDocument.appendChild(documentRootWithoutAnyCommandChilds);
		catalog = Catalog.fromDomDocument(catalogDocument);

		catalog.getCommand("Command");
	}

	@Test(expected = CommandNotInstantiableException.class)
	public void testGetCommand_notInstantiableCommand() {
		Element documentRoot = catalogDocument.createElement("catalog");

		Element command = catalogDocument.createElement("command");
		command.setAttribute("className", "cc.commandmanager.core.CatalogTest$NotInstantiableCommand");
		command.setAttribute("name", "NotInstantiableCommand");
		documentRoot.appendChild(command);

		catalogDocument.appendChild(documentRoot);
		catalog = Catalog.fromDomDocument(catalogDocument);

		catalog.getCommand("NotInstantiableCommand");
	}

	@Test(expected = CommandNotInstantiableException.class)
	public void testGetCommand_notAccessableCommand() {
		Element documentRoot = catalogDocument.createElement("catalog");

		Element command = catalogDocument.createElement("command");
		command.setAttribute("className", "cc.commandmanager.core.CatalogTest$NotAccessableCommand");
		command.setAttribute("name", "NotAccessableCommand");
		documentRoot.appendChild(command);

		catalogDocument.appendChild(documentRoot);
		catalog = Catalog.fromDomDocument(catalogDocument);

		catalog.getCommand("NotAccessableCommand");
	}

	public static class Command1 implements Command {

		@Override
		public void execute(Context context) {
		}

		@Override
		public Set<String> getBeforeDependencies() {
			return null;
		}

		@Override
		public Set<String> getAfterDependencies() {
			return null;
		}

		@Override
		public Set<String> getOptionalBeforeDependencies() {
			return null;
		}

		@Override
		public Set<String> getOptionalAfterDependencies() {
			return null;
		}

	}

	public static class Command2 implements Command {

		@Override
		public void execute(Context context) {
		}

		@Override
		public Set<String> getBeforeDependencies() {
			return null;
		}

		@Override
		public Set<String> getAfterDependencies() {
			return null;
		}

		@Override
		public Set<String> getOptionalBeforeDependencies() {
			return null;
		}

		@Override
		public Set<String> getOptionalAfterDependencies() {
			return null;
		}

	}

	public static abstract class NotInstantiableCommand implements Command {

	}

	public static class NotAccessableCommand implements Command {

		private NotAccessableCommand() {

		}

		@Override
		public void execute(Context context) {
		}

		@Override
		public Set<String> getBeforeDependencies() {
			return null;
		}

		@Override
		public Set<String> getAfterDependencies() {
			return null;
		}

		@Override
		public Set<String> getOptionalBeforeDependencies() {
			return null;
		}

		@Override
		public Set<String> getOptionalAfterDependencies() {
			return null;
		}

	}

}
