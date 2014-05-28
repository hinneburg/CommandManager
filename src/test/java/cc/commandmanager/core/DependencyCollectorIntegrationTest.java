package cc.commandmanager.core;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Maps;

public class DependencyCollectorIntegrationTest {

	private static final String DOT_DIRECTORY = "./etc";
	private static final String DOT_FILE = "graph.dot";

	private DependencyCollector dependencyCollector;
	private Map<String, Class<? extends Command>> catalog;

	@Before
	public void init() {
		catalog = Maps.newHashMap();
	}

	@AfterClass
	public static void removeFileAndDirCreatedForTests() {
		if (dotFileExists()) {
			new File(DOT_DIRECTORY + "/" + DOT_FILE).delete();
			new File(DOT_DIRECTORY).delete();
		}
	}

	@Test
	public void testOrderCommands_commandAtTheEnd_mandatoryDependency() {
		catalog.put("command", DummyCommand1.class);
		catalog.put("before", DummyCommand3.class);
		dependencyCollector = new DependencyCollector(Catalog.fromMap(catalog));

		assertThat(dependencyCollector.getOrderedCommands()).containsExactly("before", "command");
	}

	@Test
	public void testOrderCommands_commandAtFirst_mandatoryDependency() {
		catalog.put("command", DummyCommand1.class);
		catalog.put("after", DummyCommand2.class);
		dependencyCollector = new DependencyCollector(Catalog.fromMap(catalog));

		assertThat(dependencyCollector.getOrderedCommands()).containsExactly("command", "after");
	}

	@Test
	public void testOrderCommands_commandAtTheEnd_optionalDependency() {
		catalog.put("command", DummyCommand1.class);
		catalog.put("before", DummyCommand5.class);
		dependencyCollector = new DependencyCollector(Catalog.fromMap(catalog));

		assertThat(dependencyCollector.getOrderedCommands()).containsExactly("before", "command");
	}

	@Test
	public void testOrderCommands_commandAtFirst_optionalDependency() {
		catalog.put("command", DummyCommand1.class);
		catalog.put("after", DummyCommand4.class);
		dependencyCollector = new DependencyCollector(Catalog.fromMap(catalog));

		assertThat(dependencyCollector.getOrderedCommands()).containsExactly("command", "after");
	}

	@Test
	public void testOrderCommands_commandInBetween_mandatoryDependencies() {
		catalog.put("before", DummyCommand3.class);
		catalog.put("command", DummyCommand1.class);
		catalog.put("after", DummyCommand2.class);
		dependencyCollector = new DependencyCollector(Catalog.fromMap(catalog));

		assertThat(dependencyCollector.getOrderedCommands()).containsExactly("before", "command", "after");
	}

	@Test
	public void testOrderCommands_commandInBetween_optionalDependencies() {
		catalog.put("before", DummyCommand5.class);
		catalog.put("command", DummyCommand1.class);
		catalog.put("after", DummyCommand4.class);
		dependencyCollector = new DependencyCollector(Catalog.fromMap(catalog));

		assertThat(dependencyCollector.getOrderedCommands()).containsExactly("before", "command", "after");
	}

	private static boolean dotFileExists() {
		return new File(DOT_DIRECTORY + "/" + DOT_FILE).exists();
	}

}
