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
	public void testCommandGraphIsImmutable() {
		assertThat(builder.addCommand("A", "className.A")).isTrue();
		CommandGraph graph = builder.build();

		assertThat(builder.addCommand("B", "className.B")).isTrue();
		assertThat(builder.addMandatoryDependency("A", "B"));

		assertThat(graph.containsCommand("B")).isFalse();
		assertThat(graph.getDependencies("A")).isEmpty();
	}

	@Test
	public void testAddCommand_CommandClass() {
		assertThat(builder.addCommand(new CommandClass("A", "className.A"))).isTrue();
		CommandGraph graph = builder.build();
		assertThat(graph.containsCommand("A")).isTrue();
		assertThat(graph.getCommandClass("A")).isEqualTo(new CommandClass("A", "className.A"));
	}

	@Test
	public void testAddCommand_String() {
		assertThat(builder.addCommand("A", "className.A")).isTrue();
		CommandGraph graph = builder.build();
		assertThat(graph.containsCommand("A")).isTrue();
		assertThat(graph.getCommandClass("A")).isEqualTo(new CommandClass("A", "className.A"));
	}

	@Test
	public void testAddMandatoryDependency_CommandClass() {
		CommandClass source = new CommandClass("Source", "className.Source");
		CommandClass target = new CommandClass("Target", "className.Target");
		assertThat(builder.addCommand(source)).isTrue();
		assertThat(builder.addCommand(target)).isTrue();
		assertThat(builder.addMandatoryDependency(source, target)).isEqualTo(DependencyAdded.SUCCESSFUL);

		CommandGraph graph = builder.build();
		assertThat(graph.getDependencies("Source")).containsOnly(target);
		assertThat(graph.getMandatoryDependencies("Source")).containsOnly(target);
		assertThat(graph.getOptionalDependencies("Source")).isEmpty();
	}

	@Test
	public void testAddMandatoryDependency_String() {
		assertThat(builder.addCommand("Source", "className.Source")).isTrue();
		assertThat(builder.addCommand("Target", "className.Target")).isTrue();
		assertThat(builder.addMandatoryDependency("Source", "Target")).isEqualTo(DependencyAdded.SUCCESSFUL);

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
						"Target", "className.Target"))).isEqualTo(DependencyAdded.SUCCESSFUL);

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
		assertThat(builder.addOptionalDependency("Source", "Target")).isEqualTo(DependencyAdded.SUCCESSFUL);

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
		assertThat(builder.addMandatoryDependency("source", "target")).isEqualTo(DependencyAdded.SUCCESSFUL);
		assertThat(builder.addMandatoryDependency("target", "source")).isEqualTo(DependencyAdded.CYCLE_DETECTED);
	}

	@Test
	public void testAddMandatoryDependency_circularDependency_commandInTheMiddle() {
		assertThat(builder.addCommand(new CommandClass("source", "className.source"))).isTrue();
		assertThat(builder.addCommand(new CommandClass("middle", "className.target"))).isTrue();
		assertThat(builder.addCommand(new CommandClass("target", "className.target"))).isTrue();
		assertThat(builder.addMandatoryDependency("source", "middle")).isEqualTo(DependencyAdded.SUCCESSFUL);
		assertThat(builder.addMandatoryDependency("middle", "target")).isEqualTo(DependencyAdded.SUCCESSFUL);
		assertThat(builder.addMandatoryDependency("target", "source")).isEqualTo(DependencyAdded.CYCLE_DETECTED);
	}

	@Test
	public void testAddMandatoryDependency_dependencyWithoutCommand() {
		assertThat(builder.addCommand(new CommandClass("source", "className.source"))).isTrue();
		assertThat(builder.addMandatoryDependency("source", "nowhere")).isEqualTo(DependencyAdded.COMMAND_MISSING);
		assertThat(builder.addMandatoryDependency("nowhere", "source")).isEqualTo(DependencyAdded.COMMAND_MISSING);
	}

	@Test
	public void testAddMandatoryDependency_sameDependencyTwice() {
		assertThat(builder.addCommand(new CommandClass("source", "className.source"))).isTrue();
		assertThat(builder.addCommand(new CommandClass("target", "className.target"))).isTrue();
		assertThat(builder.addMandatoryDependency("source", "target")).isEqualTo(DependencyAdded.SUCCESSFUL);
		assertThat(builder.addMandatoryDependency("source", "target")).isEqualTo(DependencyAdded.ALREADY_PRESENT);
	}

	@Test
	public void testAddMandatoryDependency_overwritesOptionalDependency() {
		assertThat(builder.addCommand(new CommandClass("source", "className.source"))).isTrue();
		assertThat(builder.addCommand(new CommandClass("target", "className.target"))).isTrue();
		assertThat(builder.addOptionalDependency("source", "target")).isEqualTo(DependencyAdded.SUCCESSFUL);

		CommandGraph optional = builder.build();
		assertThat(optional.getMandatoryDependencies("source")).isEmpty();
		assertThat(optional.getOptionalDependencies("source")).containsOnly(
				new CommandClass("target", "className.target"));

		assertThat(builder.addMandatoryDependency("source", "target")).isEqualTo(DependencyAdded.OPTIONAL_OVERWRITTEN);
		CommandGraph mandatoryInsteadOfOptional = builder.build();
		assertThat(mandatoryInsteadOfOptional.getMandatoryDependencies("source")).containsOnly(
				new CommandClass("target", "className.target"));
		assertThat(mandatoryInsteadOfOptional.getOptionalDependencies("source")).isEmpty();
	}

	@Test
	public void testAddOptionalDependency_doesNotOverwriteMandatoryDependency() {
		assertThat(builder.addCommand(new CommandClass("source", "className.source"))).isTrue();
		assertThat(builder.addCommand(new CommandClass("target", "className.target"))).isTrue();
		assertThat(builder.addMandatoryDependency("source", "target")).isEqualTo(DependencyAdded.SUCCESSFUL);

		CommandGraph mandatory = builder.build();
		assertThat(mandatory.getMandatoryDependencies("source")).containsOnly(
				new CommandClass("target", "className.target"));
		assertThat(mandatory.getOptionalDependencies("source")).isEmpty();

		assertThat(builder.addOptionalDependency("source", "target")).isEqualTo(
				DependencyAdded.MANDATORY_NOT_OVERWRITTEN);
		CommandGraph stillOnlyMandatory = builder.build();
		assertThat(stillOnlyMandatory.getMandatoryDependencies("source")).containsOnly(
				new CommandClass("target", "className.target"));
		assertThat(stillOnlyMandatory.getOptionalDependencies("source")).isEmpty();

	}

}
