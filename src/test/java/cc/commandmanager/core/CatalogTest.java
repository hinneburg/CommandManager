package cc.commandmanager.core;

import static org.fest.assertions.Assertions.assertThat;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class CatalogTest {

	Catalog catalog = null;
	Document catalogDocument = createBaseCatalogDocument();

	@Test
	public void testGetNames() throws ParserConfigurationException {
		Element documentRoot = catalogDocument.createElement("catalog");

		Element command1 = catalogDocument.createElement("command");
		command1.setAttribute("className", "cc.commandmanager.core.Command1");
		command1.setAttribute("name", "Command1");
		documentRoot.appendChild(command1);

		Element command2 = catalogDocument.createElement("command");
		command2.setAttribute("className", "cc.commandmanager.core.Command2");
		command2.setAttribute("name", "Command2");
		documentRoot.appendChild(command2);

		catalogDocument.appendChild(documentRoot);
		catalog = Catalog.fromDomDocument(catalogDocument);

		assertThat(catalog.getCommandNames()).containsOnly("Command1", "Command2");
	}

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

	@Test(expected = MissingCatalogAttributeException.class)
	public void testCreateCatalogThrowsException_MissingCatalogAttributeException() {
		Element documentRoot = catalogDocument.createElement("catalog");
		Element commandWithoutNameAttribute = catalogDocument.createElement("command");
		documentRoot.appendChild(commandWithoutNameAttribute);
		catalogDocument.appendChild(documentRoot);

		catalog = Catalog.fromDomDocument(catalogDocument);
	}

	@Ignore
	@Test
	public void testGetCommand() {
		// TODO implement test case
	}

}
