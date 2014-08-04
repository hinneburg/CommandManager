package cc.commandmanager.core.integrationtests;

import cc.commandmanager.core.CommandClass;
import cc.commandmanager.core.CommandGraph;
import cc.commandmanager.core.DependencyAdded;
import cc.commandmanager.core.Optional;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;

public class CommandGraphFactoryIntegrationTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private static Document createBaseCatalogDocument() {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            fail("Could not create base catalog document", e);
            return null;
        }
    }

    /**
     * Commands are dependent on each other as follows:
     * <ul>
     * <li>DummyCommand2 (mandatory before) -> 1
     * <li>3 (mandatory after) -> 1
     * <li>4 (optional before) -> 1
     * <li>5 (optional after) -> 1
     * </ul>
     */
    @Test
    public void testFromDocument() {
        Document catalogDocument = createBaseCatalogDocument();
        Element documentRoot = catalogDocument.createElement("catalog");

        Element command1 = catalogDocument.createElement("command");
        command1.setAttribute("className", "cc.commandmanager.core.integrationtests.DummyCommand1");
        command1.setAttribute("name", "command");
        documentRoot.appendChild(command1);

        Element command2 = catalogDocument.createElement("command");
        command2.setAttribute("className", "cc.commandmanager.core.integrationtests.DummyCommand2");
        command2.setAttribute("name", "command2");
        documentRoot.appendChild(command2);

        Element command3 = catalogDocument.createElement("command");
        command3.setAttribute("className", "cc.commandmanager.core.integrationtests.DummyCommand3");
        command3.setAttribute("name", "command3");
        documentRoot.appendChild(command3);

        Element command4 = catalogDocument.createElement("command");
        command4.setAttribute("className", "cc.commandmanager.core.integrationtests.DummyCommand4");
        command4.setAttribute("name", "command4");
        documentRoot.appendChild(command4);

        Element command5 = catalogDocument.createElement("command");
        command5.setAttribute("className", "cc.commandmanager.core.integrationtests.DummyCommand5");
        command5.setAttribute("name", "command5");
        documentRoot.appendChild(command5);
        catalogDocument.appendChild(documentRoot);

        CommandGraph graph = CommandGraph.fromDocument(catalogDocument).get();
        assertThat(graph.containsCommand("command")).isTrue();
        assertThat(graph.containsCommand("command2")).isTrue();
        assertThat(graph.containsCommand("command3")).isTrue();
        assertThat(graph.containsCommand("command4")).isTrue();
        assertThat(graph.containsCommand("command5")).isTrue();

        assertThat(graph.getMandatoryDependencies("command")).containsOnly(
                new CommandClass("command3", "cc.commandmanager.core.integrationtests.DummyCommand3"));
        assertThat(graph.getOptionalDependencies("command")).containsOnly(
                new CommandClass("command5", "cc.commandmanager.core.integrationtests.DummyCommand5"));
        assertThat(graph.getMandatoryDependencies("command2")).containsOnly(
                new CommandClass("command", "cc.commandmanager.core.integrationtests.DummyCommand1"));
        assertThat(graph.getOptionalDependencies("command4")).containsOnly(
                new CommandClass("command", "cc.commandmanager.core.integrationtests.DummyCommand1"));
    }

    @Test
    public void testFromXmlFile() throws IOException {
        final File catalog = folder.newFile("catalog.xml");
        BufferedWriter output = new BufferedWriter(new FileWriter(catalog));
        output.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<catalog>\n"
                + "<command name=\"command\" className=\"cc.commandmanager.core.integrationtests.DummyCommand1\"/>\n	"
                + "<command name=\"command2\" className=\"cc.commandmanager.core.integrationtests.DummyCommand2\"/>\n"
                + "<command name=\"command3\" className=\"cc.commandmanager.core.integrationtests.DummyCommand3\"/>\n"
                + "<command name=\"command4\" className=\"cc.commandmanager.core.integrationtests.DummyCommand4\"/>\n"
                + "<command name=\"command5\" className=\"cc.commandmanager.core.integrationtests.DummyCommand5\"/>\n" + "</catalog>");
        output.close();

        CommandGraph graph = CommandGraph.fromXml(catalog).get();
        assertThat(graph.containsCommand("command")).isTrue();
        assertThat(graph.containsCommand("command2")).isTrue();
        assertThat(graph.containsCommand("command3")).isTrue();
        assertThat(graph.containsCommand("command4")).isTrue();
        assertThat(graph.containsCommand("command5")).isTrue();

        assertThat(graph.getMandatoryDependencies("command")).containsOnly(
                new CommandClass("command3", "cc.commandmanager.core.integrationtests.DummyCommand3"));
        assertThat(graph.getOptionalDependencies("command")).containsOnly(
                new CommandClass("command5", "cc.commandmanager.core.integrationtests.DummyCommand5"));
        assertThat(graph.getMandatoryDependencies("command2")).containsOnly(
                new CommandClass("command", "cc.commandmanager.core.integrationtests.DummyCommand1"));
        assertThat(graph.getOptionalDependencies("command4")).containsOnly(
                new CommandClass("command", "cc.commandmanager.core.integrationtests.DummyCommand1"));
    }

    @Test
    public void testFromXmlFile_invalidXmlFile() throws IOException {
        Optional<CommandGraph> graph = CommandGraph.fromXml(folder.newFile("invalidXmlFile.xml"));
        assertThat(graph.isPresent()).isFalse();
        assertThat(graph.getNote()).isInstanceOf(SAXParseException.class);
    }

    @Test
    public void testFromDocument_illegalEqualCommandName() {
        Document catalogDocument = createBaseCatalogDocument();
        Element documentRoot = catalogDocument.createElement("catalog");

        Element command1 = catalogDocument.createElement("command");
        command1.setAttribute("className", "cc.commandmanager.core.integrationtests.DummyCommand1");
        command1.setAttribute("name", "command");
        documentRoot.appendChild(command1);

        Element command2 = catalogDocument.createElement("command");
        command2.setAttribute("className", "cc.commandmanager.core.integrationtests.DummyCommand2");
        command2.setAttribute("name", "command");
        documentRoot.appendChild(command2);
        catalogDocument.appendChild(documentRoot);

        Optional<CommandGraph> graph = CommandGraph.fromDocument(catalogDocument);
        assertThat(graph.isPresent()).isFalse();
        assertThat(graph.getNote().toString()).contains("Duplicate command");
    }

    @Test
    public void testFromDocument_illegalEqualCommandClass() {
        Document catalogDocument = createBaseCatalogDocument();
        Element documentRoot = catalogDocument.createElement("catalog");

        Element command1 = catalogDocument.createElement("command");
        command1.setAttribute("className", "class.Name");
        command1.setAttribute("name", "command");
        documentRoot.appendChild(command1);

        Element command2 = catalogDocument.createElement("command");
        command2.setAttribute("className", "class.Name");
        command2.setAttribute("name", "command");
        documentRoot.appendChild(command2);
        catalogDocument.appendChild(documentRoot);

        Optional<CommandGraph> graph = CommandGraph.fromDocument(catalogDocument);
        assertThat(graph.isPresent()).isFalse();
        assertThat(graph.getNote().toString()).contains("Duplicate command:");
    }

    @Test
    public void testFromDocument_illegalDependencyToNowhere() {
        Document catalogDocument = createBaseCatalogDocument();
        Element documentRoot = catalogDocument.createElement("catalog");

        Element command2 = catalogDocument.createElement("command");
        command2.setAttribute("className", "cc.commandmanager.core.integrationtests.DummyCommand2");
        command2.setAttribute("name", "command with missing dependency");
        documentRoot.appendChild(command2);
        catalogDocument.appendChild(documentRoot);

        Optional<CommandGraph> graph = CommandGraph.fromDocument(catalogDocument);
        assertThat(graph.isPresent()).isFalse();
        assertThat(graph.getNote()).isEqualTo(DependencyAdded.COMMAND_MISSING);
    }

    @Test
    public void testFromDocument_missingNameAttribute() {
        Document catalogDocument = createBaseCatalogDocument();
        Element documentRoot = catalogDocument.createElement("catalog");

        Element commandWithoutName = catalogDocument.createElement("command");
        commandWithoutName.setAttribute("className", "class.Name");
        documentRoot.appendChild(commandWithoutName);
        catalogDocument.appendChild(documentRoot);

        Optional<CommandGraph> graph = CommandGraph.fromDocument(catalogDocument);
        assertThat(graph.isPresent()).isFalse();
        assertThat(graph.getNote().toString()).contains("Name or class name missing in element");
    }

    @Test
    public void testFromDocument_missingClassNameAttribute() {
        Document catalogDocument = createBaseCatalogDocument();
        Element documentRoot = catalogDocument.createElement("catalog");

        Element commandWithoutClassName = catalogDocument.createElement("command");
        commandWithoutClassName.setAttribute("name", "commandName");
        documentRoot.appendChild(commandWithoutClassName);
        catalogDocument.appendChild(documentRoot);

        Optional<CommandGraph> graph = CommandGraph.fromDocument(catalogDocument);
        assertThat(graph.isPresent()).isFalse();
        assertThat(graph.getNote().toString()).contains("Name or class name missing in element");
    }

}
