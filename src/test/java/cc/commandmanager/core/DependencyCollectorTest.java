package cc.commandmanager.core;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.chain.impl.CatalogBase;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Maps;

public class DependencyCollectorTest {

	private DependencyCollector dependencyCollector;
	private Map<String, Command> catalog;

	@Before
	public void init() {
		catalog = Maps.newHashMap();
	}

	@Test
	public void testUpdateDependencies() {
		catalog.put("DummyCommand1", new DummyCommand1());
		catalog.put("DummyCommand2", new DummyCommand2());
		catalog.put("DummyCommand3", new DummyCommand3());
		dependencyCollector = new DependencyCollector(new CatalogBase(catalog));
		Map<String, Set<String>> dependencies = new HashMap<String, Set<String>>();

		Set<String> afterDependencies = new HashSet<String>(Arrays.asList("after1", "after2"));
		Set<String> beforeDependencies = new HashSet<String>(Arrays.asList("before1", "before2"));
		DependencyCollector.updateDependencies("command", dependencies, afterDependencies, beforeDependencies);

		assertThat(dependencies.get("after1")).contains("command");
		assertThat(dependencies.get("after2")).contains("command");
		assertThat(dependencies.get("command")).contains("before1", "before2");
	}

	@Test
	public void testUpdateDependenciesWithExistingDependencies() {
		Map<String, Set<String>> dependencies = new HashMap<String, Set<String>>();
		dependencies.put("command_already1", new HashSet<String>());
		dependencies.put("command_already2", new HashSet<String>(Arrays.asList("before1")));
		dependencies.put("command_alreadyA",
				new HashSet<String>(Arrays.asList("command_already1", "command_already3", "command_already4")));

		Set<String> afterDependencies = new HashSet<String>(Arrays.asList("command_alreadyA"));
		Set<String> beforeDependencies = new HashSet<String>(Arrays.asList("before2", "before3"));

		DependencyCollector.updateDependencies("command_already2", dependencies, afterDependencies, beforeDependencies);

		assertThat(dependencies.get("command_already1")).contains();
		assertThat(dependencies.get("command_already2")).containsOnly("before1", "before2", "before3");
		assertThat(dependencies.get("command_alreadyA")).containsOnly("command_already1", "command_already2",
				"command_already3", "command_already4");
	}

	@Test
	public void testGetDependencies_beforeDependencies() {
		catalog.put("DummyCommand1", new DummyCommand1());
		catalog.put("DummyCommand2", new DummyCommand2());
		catalog.put("DummyCommand3", new DummyCommand3());

		dependencyCollector = new DependencyCollector(new CatalogBase(catalog));
		Map<String, Set<String>> dependencies = dependencyCollector.getDependencies();

		assertThat(dependencies.get("DummyCommand1")).contains();
		assertThat(dependencies.get("DummyCommand2")).containsOnly("DummyCommand1");
		assertThat(dependencies.get("DummyCommand3")).containsOnly("DummyCommand2", "DummyCommand1");
	}

	@Test
	public void testGetDependencies_optionalBeforeDependencies() {
		catalog.put("DummyCommand1", new DummyCommand1());
		catalog.put("DummyCommand2", new DummyCommand2());
		catalog.put("DummyCommand3", new DummyCommand3());

		dependencyCollector = new DependencyCollector(new CatalogBase(catalog));
		Map<String, Set<String>> dependencies = dependencyCollector.getDependencies();

		assertThat(dependencies.get("DummyCommand3")).contains("DummyCommand1", "DummyCommand2");
	}

	@Test
	public void testGetDependencies_afterDependencies() {
		catalog.put("DummyCommand1", new DummyCommand1());
		catalog.put("DummyCommand2", new DummyCommand2());
		catalog.put("DummyCommand4", new DummyCommand4());
		dependencyCollector = new DependencyCollector(new CatalogBase(catalog));
		assertThat(dependencyCollector.getDependencies().get("DummyCommand2")).containsOnly("DummyCommand1",
				"DummyCommand4");
	}

	@Test
	public void testGetDependencies_optionalAfterDependencies() {
		catalog.put("DummyCommand1", new DummyCommand1());
		catalog.put("DummyCommand2", new DummyCommand2());
		catalog.put("DummyCommand3", new DummyCommand3());
		catalog.put("DummyCommand4", new DummyCommand4());
		dependencyCollector = new DependencyCollector(new CatalogBase(catalog));
		assertThat(dependencyCollector.getDependencies().get("DummyCommand3")).contains("DummyCommand4");
	}

	@Test
	public void testOrderCommands() {
		catalog.put("DummyCommand1", new DummyCommand1());
		catalog.put("DummyCommand2", new DummyCommand2());
		catalog.put("DummyCommand3", new DummyCommand3());
		catalog.put("DummyCommand4", new DummyCommand4());
		catalog.put("DummyCommand5", new DummyCommand5());
		dependencyCollector = new DependencyCollector(new CatalogBase(catalog));
		final List<String> orderedCommands = dependencyCollector.orderCommands(dependencyCollector.getDependencies());
		orderedCommands.remove(("DummyCommand5"));
		assertThat(orderedCommands)
				.containsSequence("DummyCommand1", "DummyCommand4", "DummyCommand2", "DummyCommand3");
	}

	@AfterClass
	public static void removeFleAndDirCreatedForTests() {
		if (dotFileExists()) {
			new File("./etc/graph.dot").delete();
			new File("./etc").delete();
		}
	}

	private static boolean dotFileExists() {
		return new File("./etc/graph.dot").exists();
	}

}
