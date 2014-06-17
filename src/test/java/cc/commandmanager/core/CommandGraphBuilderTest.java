package cc.commandmanager.core;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import cc.commandmanager.core.CommandGraph.CommandGraphBuilder;
import cc.commandmanager.core.CommandGraph.Dependencies;

public class CommandGraphBuilderTest {

	CommandGraphBuilder builder;

	@Before
	public void setUp() {
		builder = new CommandGraphBuilder();
	}

	@Test
	public void testHasCommand() {
		builder.addCommandWithDependencies(new CommandClass("command", "cc.commandmanager.core.DummyCommand1"),
				new Dependencies(), new Dependencies());
		CommandGraph graph = builder.build();
		assertThat(graph.hasCommand("command")).isTrue();
		assertThat(graph.hasCommand("not there")).isFalse();
	}

}
