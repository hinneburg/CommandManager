package cc.commandmanager.core;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import cc.commandmanager.core.CommandGraph.CommandGraphBuilder;

public class CommandGraphBuilderTest {

	CommandGraphBuilder builder;

	@Before
	public void setUp() {
		builder = new CommandGraphBuilder();
	}

	@Test
	public void testAddCommand_CommandClass() {
		assertThat(builder.addCommand(new CommandClass("A", "className.A"))).isTrue();
		CommandGraph graph = builder.build();
		assertThat(graph.hasCommand("A")).isTrue();
		assertThat(graph.getCommandClass("A")).isEqualTo(new CommandClass("A", "className.A"));
	}

	@Test
	public void testAddCommand_String() {
		assertThat(builder.addCommand("A", "className.A")).isTrue();
		CommandGraph graph = builder.build();
		assertThat(graph.hasCommand("A")).isTrue();
		assertThat(graph.getCommandClass("A")).isEqualTo(new CommandClass("A", "className.A"));
	}

	@Test
	public void testAddMandatoryDependency_CommandClass() {
		CommandClass source = new CommandClass("Source", "className.Source");
		CommandClass target = new CommandClass("Target", "className.Target");
		assertThat(builder.addCommand(source)).isTrue();
		assertThat(builder.addCommand(target)).isTrue();
		assertThat(builder.addMandatoryDependency(source, target)).isTrue();

		CommandGraph graph = builder.build();
		assertThat(graph.getDependencies("Source")).containsOnly(target);
		assertThat(graph.getMandatoryDependencies("Source")).containsOnly(target);
		assertThat(graph.getOptionalDependencies("Source")).isEmpty();
	}

	@Test
	public void testAddMandatoryDependency_String() {
		assertThat(builder.addCommand("Source", "className.Source")).isTrue();
		assertThat(builder.addCommand("Target", "className.Target")).isTrue();
		assertThat(builder.addMandatoryDependency("Source", "Target")).isTrue();

		CommandGraph graph = builder.build();
		assertThat(graph.getDependencies("Source")).containsOnly(new CommandClass("Target", "className.Target"));
		assertThat(graph.getMandatoryDependencies("Source")).containsOnly(
				new CommandClass("Target", "className.Target"));
		assertThat(graph.getOptionalDependencies("Source")).isEmpty();
	}

	@Test
	public void testAddOptionalDependency_CommandClass() {
		assertThat(builder.addCommand("Source", "className.Source")).isTrue();
		assertThat(builder.addCommand("Target", "className.Target")).isTrue();
		assertThat(
				builder.addOptionalDependency(new CommandClass("Source", "className.Source"), new CommandClass(
						"Target", "className.Target"))).isTrue();

		CommandGraph graph = builder.build();
		assertThat(graph.getDependencies("Source")).containsOnly(new CommandClass("Target", "className.Target"));
		assertThat(graph.getMandatoryDependencies("Source")).isEmpty();
		assertThat(graph.getOptionalDependencies("Source"))
				.containsOnly(new CommandClass("Target", "className.Target"));
	}

	@Test
	public void testAddOptionalDependency_String() {
		assertThat(builder.addCommand("Source", "className.Source")).isTrue();
		assertThat(builder.addCommand("Target", "className.Target")).isTrue();
		assertThat(builder.addOptionalDependency("Source", "Target")).isTrue();

		CommandGraph graph = builder.build();
		assertThat(graph.getDependencies("Source")).containsOnly(new CommandClass("Target", "className.Target"));
		assertThat(graph.getMandatoryDependencies("Source")).isEmpty();
		assertThat(graph.getOptionalDependencies("Source"))
				.containsOnly(new CommandClass("Target", "className.Target"));
	}

	@Test
	public void testAddCommand_sameClassNameTwice() {
		assertThat(builder.addCommand(new CommandClass("A", "className.A"))).isTrue();
		assertThat(builder.addCommand(new CommandClass("B", "className.A"))).isTrue();
	}

	@Test
	public void testAddCommand_sameCommandTwice() {
		assertThat(builder.addCommand(new CommandClass("A", "className.A"))).isTrue();
		assertThat(builder.addCommand(new CommandClass("A", "className.A"))).isFalse();
	}

	@Test
	public void testAddCommand_sameCommandNameTwice() {
		assertThat(builder.addCommand(new CommandClass("A", "className.A"))).isTrue();
		assertThat(builder.addCommand(new CommandClass("A", "otherClassName.A"))).isFalse();
	}

	@Test
	public void testAddMandatoryDependency_circularDependency() {
		assertThat(builder.addCommand(new CommandClass("source", "className.source"))).isTrue();
		assertThat(builder.addCommand(new CommandClass("target", "className.target"))).isTrue();
		assertThat(builder.addMandatoryDependency("source", "target")).isTrue();
		assertThat(builder.addMandatoryDependency("target", "source")).isFalse();
	}

	@Test
	public void testAddMandatoryDependency_dependencyWithoutCommand() {
		assertThat(builder.addCommand(new CommandClass("source", "className.source"))).isTrue();
		assertThat(builder.addMandatoryDependency("source", "nowhere")).isFalse();
		assertThat(builder.addMandatoryDependency("nowhere", "source")).isFalse();
	}

	@Test
	public void testAddMandatoryDependency_sameDependencyTwice() {
		assertThat(builder.addCommand(new CommandClass("source", "className.source"))).isTrue();
		assertThat(builder.addCommand(new CommandClass("target", "className.target"))).isTrue();
		assertThat(builder.addMandatoryDependency("source", "target")).isTrue();
		assertThat(builder.addMandatoryDependency("source", "target")).isFalse();
	}

}
