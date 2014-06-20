package cc.commandmanager.core;

import static org.fest.assertions.Assertions.assertThat;
import net.sf.qualitycheck.exception.IllegalNullArgumentException;

import org.junit.Before;
import org.junit.Test;

import cc.commandmanager.core.CommandGraph.CommandGraphBuilder;

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
	public void testHasCommand() {
		assertThat(graph.hasCommand("A")).isTrue();
		assertThat(graph.hasCommand("not there")).isFalse();
	}

	@Test(expected = IllegalNullArgumentException.class)
	public void testHasCommand_nullArgument() {
		graph.hasCommand(null);
	}

	@Test
	public void testGetCommandClass() {
		assertThat(graph.getCommandClass("A")).isEqualTo(commandA);
	}

	@Test(expected = CommandNotFoundException.class)
	public void testGetCommandClass_commandNotFound() {
		graph.getCommandClass("X");
	}

	@Test(expected = IllegalNullArgumentException.class)
	public void testGetCommandClass_nullArgument() {
		graph.getCommandClass(null);
	}

	@Test
	public void testGetDependencies() {
		assertThat(graph.getDependencies("A")).containsExactly(commandB, commandC);
		assertThat(graph.getDependencies("B")).isEmpty();
		assertThat(graph.getDependencies("C")).isEmpty();
	}

	@Test(expected = CommandNotFoundException.class)
	public void testGetDependencies_commandNotFound() {
		graph.getDependencies("X");
	}

	@Test(expected = IllegalNullArgumentException.class)
	public void testGetDependencies_nullArgument() {
		graph.getDependencies(null);
	}

	@Test
	public void testGetMandatoryDependencies() {
		assertThat(graph.getMandatoryDependencies("A")).containsExactly(commandB);
		assertThat(graph.getMandatoryDependencies("B")).isEmpty();
		assertThat(graph.getMandatoryDependencies("C")).isEmpty();
	}

	@Test(expected = CommandNotFoundException.class)
	public void testGetMandatoryDependencies_commandNotFound() {
		graph.getMandatoryDependencies("X");
	}

	@Test(expected = IllegalNullArgumentException.class)
	public void testGetMandatoryDependencies_nullArgument() {
		graph.getMandatoryDependencies(null);
	}

	@Test
	public void testGetOptionalDependencies() {
		assertThat(graph.getOptionalDependencies("A")).containsExactly(commandC);
		assertThat(graph.getOptionalDependencies("B")).isEmpty();
		assertThat(graph.getOptionalDependencies("C")).isEmpty();
	}

	@Test(expected = CommandNotFoundException.class)
	public void testGetOptionalDependencies_commandNotFound() {
		graph.getOptionalDependencies("X");
	}

	@Test(expected = IllegalNullArgumentException.class)
	public void testGetOptionalDependencies_nullArgument() {
		graph.getOptionalDependencies(null);
	}

}
