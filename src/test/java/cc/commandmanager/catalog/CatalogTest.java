package cc.commandmanager.catalog;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.sf.qualitycheck.exception.IllegalEmptyArgumentException;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cc.commandmanager.catalog.Catalog;
import cc.commandmanager.catalog.IllegalClassNameToCommandAssociationException;
import cc.commandmanager.catalog.MissingElementAttributeException;
import cc.commandmanager.core.Command;
import cc.commandmanager.core.CommandNotFoundException;
import cc.commandmanager.core.CommandNotInstantiableException;
import cc.commandmanager.core.Context;

import com.google.common.collect.Maps;

public class CatalogTest {

	private Catalog catalog;
	private Document catalogDocument;
	private Map<String, Class<? extends Command>> catalogMap;

	@Before
	public void setup() {
		catalogMap = Maps.newHashMap();
		catalogDocument = createBaseCatalogDocument();
	}

	private static Document createBaseCatalogDocument() {
		try {
			return DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException e) {
			fail("Could not create base catalog document", e);
			return null;
		}
	}

	@Test
	public void testCreateCatalogFromMap() {
		Map<String, Class<? extends Command>> commands = Maps.newHashMap();
		commands.put("command1", Command1.class);
		commands.put("command2", Command2.class);

		Catalog catalog = Catalog.fromMap(commands);

		assertThat(catalog.getCommandNames()).containsOnly("command1", "command2");
		assertThat(catalog.getCommand("command1")).isInstanceOf(Command1.class);
		assertThat(catalog.getCommand("command2")).isInstanceOf(Command2.class);
	}

	@Test
	public void testGetCommandsNames() {
		catalogMap.put("Command1", Command1.class);
		catalog = Catalog.fromMap(catalogMap);
		assertThat(catalog.getCommandNames()).containsOnly("Command1");

		catalogMap.put("Command2", Command2.class);
		catalog = Catalog.fromMap(catalogMap);
		assertThat(catalog.getCommandNames()).containsOnly("Command1", "Command2");
	}

	@Test
	public void testGetCommand() {
		catalogMap.put("Command1", Command1.class);
		catalog = Catalog.fromMap(catalogMap);

		assertThat(catalog.getCommand("Command1").getClass()).isEqualTo(Command1.class);
	}

	@Test(expected = CommandNotFoundException.class)
	public void testGetCommand_commandNotFound() {
		catalog = Catalog.fromMap(new HashMap<String, Class<? extends Command>>());
		catalog.getCommand("Command");
	}

	@Test(expected = CommandNotInstantiableException.class)
	public void testGetCommand_notInstantiableCommand() {
		catalogMap.put("NotInstantiableCommand", NotInstantiableCommand.class);
		catalog = Catalog.fromMap(catalogMap);

		catalog.getCommand("NotInstantiableCommand");
	}

	@Test(expected = CommandNotInstantiableException.class)
	public void testGetCommand_notAccessableCommand() {
		catalogMap.put("NotAccessableCommand", NotAccessableCommand.class);
		catalog = Catalog.fromMap(catalogMap);

		catalog.getCommand("NotAccessableCommand");
	}

	@Test
	public void testCreateCatalogAllowsMultipleCommandNamesForSameClassName() {
		catalogMap.put("abc", Command1.class);
		catalogMap.put("xyz", Command1.class);
		catalog = Catalog.fromMap(catalogMap);

		assertThat(catalog.getCommandNames()).containsOnly("abc", "xyz");
		assertThat(catalog.getCommand("abc")).isInstanceOf(Command1.class);
		assertThat(catalog.getCommand("xyz")).isInstanceOf(Command1.class);
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
	public void testCreateCatalogThrowsException_illegalMultipleClassNamesToOneCommandName() {
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
