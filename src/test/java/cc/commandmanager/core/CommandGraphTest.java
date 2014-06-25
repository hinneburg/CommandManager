package cc.commandmanager.core;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.sf.qualitycheck.exception.IllegalNullArgumentException;

import org.fest.assertions.Condition;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXParseException;

import cc.commandmanager.core.CommandGraph.CommandGraphBuilder;

import com.google.common.collect.Lists;

public class CommandGraphTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	private CommandGraphBuilder builder;
	private CommandGraph graph;
	private CommandClass commandA;
	private CommandClass commandB;
	private CommandClass commandC;

	@Before
	public void setUp() {
		commandA = new CommandClass("A", "className.A");
		commandB = new CommandClass("B", "className.B");
		commandC = new CommandClass("C", "className.C");

		builder = new CommandGraphBuilder();
		builder.addCommand(commandA);
		builder.addCommand(commandB);
		builder.addCommand(commandC);
		builder.addMandatoryDependency("A", "B");
		builder.addOptionalDependency("A", "C");
		graph = builder.build();
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
		command1.setAttribute("className", "cc.commandmanager.core.DummyCommand1");
		command1.setAttribute("name", "command");
		documentRoot.appendChild(command1);

		Element command2 = catalogDocument.createElement("command");
		command2.setAttribute("className", "cc.commandmanager.core.DummyCommand2");
		command2.setAttribute("name", "command2");
		documentRoot.appendChild(command2);

		Element command3 = catalogDocument.createElement("command");
		command3.setAttribute("className", "cc.commandmanager.core.DummyCommand3");
		command3.setAttribute("name", "command3");
		documentRoot.appendChild(command3);

		Element command4 = catalogDocument.createElement("command");
		command4.setAttribute("className", "cc.commandmanager.core.DummyCommand4");
		command4.setAttribute("name", "command4");
		documentRoot.appendChild(command4);

		Element command5 = catalogDocument.createElement("command");
		command5.setAttribute("className", "cc.commandmanager.core.DummyCommand5");
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
				new CommandClass("command3", "cc.commandmanager.core.DummyCommand3"));
		assertThat(graph.getOptionalDependencies("command")).containsOnly(
				new CommandClass("command5", "cc.commandmanager.core.DummyCommand5"));
		assertThat(graph.getMandatoryDependencies("command2")).containsOnly(
				new CommandClass("command", "cc.commandmanager.core.DummyCommand1"));
		assertThat(graph.getOptionalDependencies("command4")).containsOnly(
				new CommandClass("command", "cc.commandmanager.core.DummyCommand1"));
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
	public void testFromXmlFile() throws IOException {
		final File catalog = folder.newFile("catalog.xml");
		BufferedWriter output = new BufferedWriter(new FileWriter(catalog));
		output.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<catalog>\n"
				+ "<command name=\"command\" className=\"cc.commandmanager.core.DummyCommand1\"/>\n	"
				+ "<command name=\"command2\"\nclassName=\"cc.commandmanager.core.DummyCommand2\"/>\n"
				+ "<command name=\"command3\"\nclassName=\"cc.commandmanager.core.DummyCommand3\"/>\n"
				+ "<command name=\"command4\"\nclassName=\"cc.commandmanager.core.DummyCommand4\"/>\n"
				+ "<command name=\"command5\"\nclassName=\"cc.commandmanager.core.DummyCommand5\"/>\n" + "</catalog>");
		output.close();

		CommandGraph graph = CommandGraph.fromXml(catalog).get();
		assertThat(graph.containsCommand("command")).isTrue();
		assertThat(graph.containsCommand("command2")).isTrue();
		assertThat(graph.containsCommand("command3")).isTrue();
		assertThat(graph.containsCommand("command4")).isTrue();
		assertThat(graph.containsCommand("command5")).isTrue();

		assertThat(graph.getMandatoryDependencies("command")).containsOnly(
				new CommandClass("command3", "cc.commandmanager.core.DummyCommand3"));
		assertThat(graph.getOptionalDependencies("command")).containsOnly(
				new CommandClass("command5", "cc.commandmanager.core.DummyCommand5"));
		assertThat(graph.getMandatoryDependencies("command2")).containsOnly(
				new CommandClass("command", "cc.commandmanager.core.DummyCommand1"));
		assertThat(graph.getOptionalDependencies("command4")).containsOnly(
				new CommandClass("command", "cc.commandmanager.core.DummyCommand1"));
	}

	@Test
	public void testFromXmlFile_invalidXmlFile() throws IOException {
		Optional<CommandGraph> graph = CommandGraph.fromXml(folder.newFile("invalidXmlFile.xml"));
		assertThat(graph.isPresent()).isFalse();
		assertThat(graph.getNote()).isInstanceOf(SAXParseException.class);
	}

	@Test
	public void testContainsCommand() {
		assertThat(graph.containsCommand("A")).isTrue();
		assertThat(graph.containsCommand("not there")).isFalse();
	}

	@Test
	public void testGetCommandClass() {
		assertThat(graph.getCommandClass("A")).isEqualTo(commandA);
	}

	@Test
	public void testGetDependencies() {
		assertThat(graph.getDependencies("A")).containsExactly(commandB, commandC);
		assertThat(graph.getDependencies("B")).isEmpty();
		assertThat(graph.getDependencies("C")).isEmpty();
	}

	@Test
	public void testGetMandatoryDependencies() {
		assertThat(graph.getMandatoryDependencies("A")).containsExactly(commandB);
		assertThat(graph.getMandatoryDependencies("B")).isEmpty();
		assertThat(graph.getMandatoryDependencies("C")).isEmpty();
	}

	@Test
	public void testGetOptionalDependencies() {
		assertThat(graph.getOptionalDependencies("A")).containsExactly(commandC);
		assertThat(graph.getOptionalDependencies("B")).isEmpty();
		assertThat(graph.getOptionalDependencies("C")).isEmpty();
	}

	@Test
	public void topologicalOrderOfAllCommands() {
		assertThat(graph.topologicalOrderOfAllCommands()).satisfies(new Condition<List<?>>() {
			@Override
			public boolean matches(List<?> topologicalOrder) {
				return topologicalOrder.indexOf(commandB) < topologicalOrder.indexOf(commandA)
						&& topologicalOrder.indexOf(commandC) < topologicalOrder.indexOf(commandA);
			}
		});

		builder.addMandatoryDependency(commandC, commandB);
		assertThat(builder.build().topologicalOrderOfAllCommands()).satisfies(new Condition<List<?>>() {
			@Override
			public boolean matches(List<?> topologicalOrder) {
				return topologicalOrder.indexOf(commandB) < topologicalOrder.indexOf(commandC);
			}
		});
	}

	@Test
	public void topologicalOrderOfGivenCommands() {
		final CommandClass command0 = new CommandClass("0", "className.0");
		builder.addCommand(command0);
		builder.addMandatoryDependency(command0, commandA);

		CommandGraph graph = builder.build();
		assertThat(graph.topologicalOrderOfAllCommands()).satisfies(new Condition<List<?>>() {
			@Override
			public boolean matches(List<?> topologicalOrder) {
				return topologicalOrder.indexOf(commandB) < topologicalOrder.indexOf(command0);
			}
		});

		assertThat(graph.topologicalOrderOfGivenCommands(Lists.newArrayList(commandB, command0))).satisfies(
				new Condition<List<?>>() {
					@Override
					public boolean matches(List<?> topologicalOrder) {
						return topologicalOrder.indexOf(command0) < topologicalOrder.indexOf(commandB)
								|| topologicalOrder.indexOf(commandB) < topologicalOrder.indexOf(command0);
					}
				});
	}

	@Test
	public void testToString() {
		assertThat(graph.toString())
				.isEqualTo(
						"digraph G {\n  labelloc = \"t\";\n  label = \"Command graph\";\n  rankdir = BT;\n  node [shape=record];\n  "
								+ "edge [arrowhead=vee];\n  \"A (className.A)\";\n  \"A (className.A)\" -> \"B (className.B)\";\n  "
								+ "\"A (className.A)\" -> \"C (className.C)\" [style = dotted] ;\n  \"B (className.B)\";\n  "
								+ "\"C (className.C)\";\n}");
	}

	@Test
	public void testFromDocument_illegalEqualCommandName() {
		Document catalogDocument = createBaseCatalogDocument();
		Element documentRoot = catalogDocument.createElement("catalog");

		Element command1 = catalogDocument.createElement("command");
		command1.setAttribute("className", "cc.commandmanager.core.DummyCommand1");
		command1.setAttribute("name", "command");
		documentRoot.appendChild(command1);

		Element command2 = catalogDocument.createElement("command");
		command2.setAttribute("className", "cc.commandmanager.core.DummyCommand2");
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
		command1.setAttribute("className", "cc.commandmanager.core.DummyCommand1");
		command1.setAttribute("name", "command");
		documentRoot.appendChild(command1);

		Element command2 = catalogDocument.createElement("command");
		command2.setAttribute("className", "cc.commandmanager.core.DummyCommand1");
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
		command2.setAttribute("className", "cc.commandmanager.core.DummyCommand2");
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

	@Test(expected = IllegalNullArgumentException.class)
	public void testContainsCommand_nullArgument() {
		graph.containsCommand(null);
	}

	@Test(expected = CommandNotFoundException.class)
	public void testGetCommandClass_commandNotFound() {
		graph.getCommandClass("X");
	}

	@Test(expected = IllegalNullArgumentException.class)
	public void testGetCommandClass_nullArgument() {
		graph.getCommandClass(null);
	}

	@Test(expected = CommandNotFoundException.class)
	public void testGetDependencies_commandNotFound() {
		graph.getDependencies("X");
	}

	@Test(expected = IllegalNullArgumentException.class)
	public void testGetDependencies_nullArgument() {
		graph.getDependencies(null);
	}

	@Test(expected = CommandNotFoundException.class)
	public void testGetMandatoryDependencies_commandNotFound() {
		graph.getMandatoryDependencies("X");
	}

	@Test(expected = IllegalNullArgumentException.class)
	public void testGetMandatoryDependencies_nullArgument() {
		graph.getMandatoryDependencies(null);
	}

	@Test(expected = CommandNotFoundException.class)
	public void testGetOptionalDependencies_commandNotFound() {
		graph.getOptionalDependencies("X");
	}

	@Test(expected = IllegalNullArgumentException.class)
	public void testGetOptionalDependencies_nullArgument() {
		graph.getOptionalDependencies(null);
	}

	@Test
	public void testTopologicalOrderOfAllCommands_noDuplicates() {
		assertThat(graph.topologicalOrderOfAllCommands()).doesNotHaveDuplicates();
	}

}
