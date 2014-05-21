package cc.commandmanager.core;

import static org.fest.assertions.Assertions.assertThat;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class CatalogTest {

	Catalog catalog = null;

	@Before
	public void setup() throws ParserConfigurationException {
		catalog = Catalog.fromDomDocument(createCatalogDocument());
	}

	private static Document createCatalogDocument() throws ParserConfigurationException {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document catalogDocument = dBuilder.newDocument();

		Element catalog = catalogDocument.createElement("catalog");

		Element command1 = catalogDocument.createElement("command");
		command1.setAttribute("className", "cc.commandmanager.core.Command1");
		command1.setAttribute("name", "Command1");
		catalog.appendChild(command1);

		Element command2 = catalogDocument.createElement("command");
		command2.setAttribute("className", "cc.commandmanager.core.Command2");
		command2.setAttribute("name", "Command2");
		catalog.appendChild(command2);

		catalogDocument.appendChild(catalog);
		return catalogDocument;
	}

	@Test
	public void testGetNames() {
		assertThat(catalog.getCommandNames()).containsOnly("Command1", "Command2");
	}

	@Ignore
	@Test(expected = MissingDomAttributeException.class)
	public void testGetNamesThrowsException() {
		// TODO implement test case
		// the document must therefore have a command element without the attribute "name"
	}

	@Ignore
	@Test
	public void testGetCommand() {
		// TODO implement test case
	}

}
