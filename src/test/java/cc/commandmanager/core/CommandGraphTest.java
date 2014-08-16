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
import net.sf.qualitycheck.exception.IllegalNullElementsException;

import org.fest.assertions.Condition;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXParseException;

import cc.commandmanager.core.CommandGraph.CommandGraphBuilder;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class CommandGraphTest {

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

	@Test
	public void testContainsCommand() {
		assertThat(graph.containsCommand("A")).isTrue();
		assertThat(graph.containsCommand("not there")).isFalse();
	}

    @Test
    public void testCommandNamesAreCaseAndSpaceSensitive() {
        assertThat(graph.containsCommand("A")).isTrue();
        assertThat(graph.containsCommand("a")).isFalse();
        assertThat(graph.containsCommand(" A")).isFalse();
        assertThat(graph.containsCommand("A ")).isFalse();
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
	public void testGetConnectedComponents() {
		assertThat(graph.getConnectedComponents()).containsOnly(graph);

		CommandGraphBuilder smallerGraphBuilder = CommandGraph.builder();
		smallerGraphBuilder.addCommand("D", "className.D");
		CommandGraph smallerGraph = smallerGraphBuilder.build();

		builder.addCommand(smallerGraph.getCommandClass("D"));
		CommandGraph biggerGraph = builder.build();
		assertThat(biggerGraph.getConnectedComponents()).containsOnly(graph, smallerGraph);
	}

	@Test
	public void testGetConnectedComponents_emptyGraph() {
		CommandGraph graph = CommandGraph.builder().build();
		assertThat(graph.getConnectedComponents()).isEmpty();
	}

	@Test
	public void testToDot() {
		assertThat(graph.toDot())
				.isEqualTo(
						"digraph G {\n  labelloc = \"t\";\n  label = \"Command graph\";\n  rankdir = BT;\n  node [shape=record];\n  "
								+ "edge [arrowhead=vee];\n  \"A (className.A)\";\n  \"A (className.A)\" -> \"B (className.B)\";\n  "
								+ "\"A (className.A)\" -> \"C (className.C)\" [style = dotted] ;\n  \"B (className.B)\";\n  "
								+ "\"C (className.C)\";\n}");
	}

	@Test
	public void testDependenciesInvertEdgeDirection() {
		CommandGraphBuilder builder = new CommandGraphBuilder();
		builder.addCommand(commandA);
		builder.addCommand(commandB);
		builder.addMandatoryDependency(commandA, commandB);

		assertThat(builder.build().topologicalOrderOfAllCommands()).containsSequence(commandB, commandA);
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

	@Test(expected = IllegalNullArgumentException.class)
	public void testTopologicalOrderOfGivenCommands_nullArgument() {
		graph.topologicalOrderOf((Iterable<CommandClass>) null);
	}

	@Test(expected = IllegalNullElementsException.class)
	public void testTopologicalOrderOfGivenCommands_nullElement() {
		List<CommandClass> nullElement = Lists.newArrayList();
		nullElement.add(null);
		graph.topologicalOrderOf(nullElement);
	}

	@Test
	public void testTopologicalOrderOfAllCommands_shouldBeEmpty() {
		assertThat(CommandGraph.builder().build().topologicalOrderOfAllCommands()).isEmpty();
	}

	@Test
	public void testTopologicalOrderOfAllCommands() {
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

	/**
	 * Commands are dependent on each other as follows:
	 * <ul>
	 * <li>0 (mandatory) -> A
	 * <li>A (mandatory) -> B
	 * <li>A (optional) -> C
	 **/
	@Test
	public void testTopologicalOrderOfGivenCommands() {
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

		assertThat(graph.topologicalOrderOf(Lists.newArrayList(commandB, command0))).satisfies(
				new Condition<List<?>>() {

					@Override
					public boolean matches(List<?> topologicalOrder) {
						return topologicalOrder.indexOf(commandB) < topologicalOrder.indexOf(command0);
					}
				});
	}

	@Test
	public void testTopologicalOrderOfNames() {
		assertThat(graph.topologicalOrderOfNames(Lists.newArrayList("A", "B"))).containsExactly(commandB, commandA);
	}

	@Test(expected = CommandNotFoundException.class)
	public void testTopologicalOrderOfNames_commandDoesNotExist() {
		graph.topologicalOrderOfNames(Lists.newArrayList("Missing!"));
	}

	@Test(expected = IllegalNullArgumentException.class)
	public void testTopologicalOrderOfNames_nullArgument() {
		graph.topologicalOrderOfNames((Iterable<String>) null);
	}

	@Test(expected = IllegalNullElementsException.class)
	public void testTopologicalOrderOfNames_nullElement() {
		List<String> nullElement = Lists.newArrayList();
		nullElement.add(null);
		graph.topologicalOrderOfNames(nullElement);
	}

	@Test(expected = CommandNotFoundException.class)
	public void testTopologicalOrderOfGivenCommands_commandDoesNotExist() {
		final CommandClass command0 = new CommandClass("0", "className.0");
		CommandGraph graph = builder.build();
		graph.topologicalOrderOf(command0);
	}

	@Test
	public void testTopologicalOrderOfGivenCommands_variousConnectedComponents() {
		CommandGraphBuilder builder = new CommandGraphBuilder();
		builder.addCommand(commandA);
		builder.addCommand(commandB);
		builder.addCommand(commandC);

		assertThat(builder.build().topologicalOrderOf(commandA, commandB, commandC)).contains(commandA, commandB,
				commandC).doesNotHaveDuplicates();

		assertThat(builder.build().topologicalOrderOf(commandA)).containsExactly(commandA);
	}

	@Test
	public void testTopologicalOrderOfGivenCommands_iterableEqualsVarargs() {
		CommandGraph graph = builder.build();
		assertThat(graph.topologicalOrderOf(commandA, commandB, commandC)).isEqualTo(
				graph.topologicalOrderOf(ImmutableList.of(commandA, commandB, commandC)));
	}

	@Test
	public void testEquals_sameBuild() {
		assertThat(builder.build().equals(builder.build())).isTrue();
	}

	@Test
	public void testEquals_differentBuildSameGraph() {
		CommandGraphBuilder builder1 = CommandGraph.builder();
		builder1.addCommand("A", "A");
		builder1.addCommand("B", "B");
		builder1.addMandatoryDependency("A", "B");
		CommandGraphBuilder builder2 = CommandGraph.builder();
		builder2.addCommand("A", "A");
		builder2.addCommand("B", "B");
		builder2.addMandatoryDependency("A", "B");

		CommandGraph graph1 = builder1.build();
		CommandGraph graph2 = builder2.build();

		assertThat(graph1.equals(graph2)).isTrue();
		assertThat(graph1.hashCode()).isEqualTo(graph2.hashCode());
	}

	@Test
	public void testEquals_shouldBeDifferent() {
		CommandGraph smallGraph = builder.build();
		builder.addCommand("D", "class.D");
		CommandGraph bigGraph = builder.build();

		assertThat(smallGraph.equals(bigGraph)).isFalse();
		assertThat(smallGraph.hashCode()).isNotEqualTo(bigGraph.hashCode());
	}

}
