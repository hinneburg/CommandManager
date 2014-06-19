package cc.commandmanager.core;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import cc.commandmanager.core.CommandGraph.CommandGraphBuilder;

public class CommandGraphTest {

	CommandGraphBuilder builder;
	CommandGraph graph;

	@Before
	public void setUp() {
		builder = new CommandGraphBuilder();
		builder.addCommand(new CommandClass("A", "className.A"));
		graph = builder.build();
	}

	@Test
	public void testHasCommand() {
		assertThat(graph.hasCommand("A")).isTrue();
		assertThat(graph.hasCommand("not there")).isFalse();
	}

}
