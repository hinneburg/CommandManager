package cc.commandmanager.core;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.chain.Catalog;
import org.apache.commons.chain.config.ConfigParser;
import org.apache.commons.chain.impl.CatalogFactoryBase;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

public class DependencyCollectorTest {

	private DependencyCollector dependencyCollector;

	@Before
	public void init() {
		this.dependencyCollector = new DependencyCollector(initializeNewCatalog("/DummyCatalog1.xml"));
	}

	@Test
	public void testUpdateDependencies() {
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
		dependencies.put("command_allready1", new HashSet<String>());
		dependencies.put("command_allready2", new HashSet<String>(Arrays.asList("before1")));
		dependencies.put("command_allreadyA",
				new HashSet<String>(Arrays.asList("command_allready1", "command_allready3", "command_allready4")));

		Set<String> afterDependencies = new HashSet<String>(Arrays.asList("command_allreadyA"));
		Set<String> beforeDependencies = new HashSet<String>(Arrays.asList("before2", "before3"));

		DependencyCollector
				.updateDependencies("command_allready2", dependencies, afterDependencies, beforeDependencies);

		assertThat(dependencies.get("command_allready1")).contains();
		assertThat(dependencies.get("command_allready2")).containsOnly("before1", "before2", "before3");
		assertThat(dependencies.get("command_allreadyA")).containsOnly("command_allready1", "command_allready2",
				"command_allready3", "command_allready4");
	}

	@Test
	public void testGetDependencies_beforeDependencies() {
		Map<String, Set<String>> dependencies = this.dependencyCollector.getDependencies();
		assertThat(dependencies.get("DummyCommand1")).contains();
		assertThat(dependencies.get("DummyCommand2")).containsOnly("DummyCommand1");
		assertThat(dependencies.get("DummyCommand3")).containsOnly("DummyCommand2", "DummyCommand1");
	}

	@Test
	public void testGetDependencies_optionalBeforeDependencies() {
		this.dependencyCollector = new DependencyCollector(initializeNewCatalog("/DummyCatalog2.xml"));
		assertThat(this.dependencyCollector.getDependencies().get("DummyCommand3")).containsOnly("DummyCommand1");
	}

	@Test
	public void testGetDependencies_afterDependencies() {
		this.dependencyCollector = new DependencyCollector(initializeNewCatalog("/DummyCatalog3.xml"));
		assertThat(this.dependencyCollector.getDependencies().get("DummyCommand2")).containsOnly("DummyCommand1",
				"DummyCommand4");
	}

	@Test
	public void testGetDependencies_optionalAfterDependencies() {
		this.dependencyCollector = new DependencyCollector(initializeNewCatalog("/DummyCatalog4.xml"));
		assertThat(this.dependencyCollector.getDependencies().get("DummyCommand3")).contains("DummyCommand4");
	}

	@Test
	public void testOrderCommands() {
		this.dependencyCollector = new DependencyCollector(initializeNewCatalog("/DummyCatalog_for_ordering.xml"));
		final List<String> orderedCommands = this.dependencyCollector.orderCommands(this.dependencyCollector
				.getDependencies());
		orderedCommands.remove(("DummyCommand5"));
		assertThat(orderedCommands)
		.containsSequence("DummyCommand1", "DummyCommand4", "DummyCommand2", "DummyCommand3");
	}

	@AfterClass
	public static void removeDotFile() {
		// TODO configure path to fit relatively (before that the project has to
		// move to cvs)
		if (!(new File("/home/user/workspace-2014-02-17/command-manager/etc/graph.dot").delete() && new File(
				"/home/user/workspace-2014-02-17/command-manager/etc").delete())) {
			System.out.println("NOTE: A file or directory created for testing issues could not be removed.");
		}
	}

	private Catalog initializeNewCatalog(String url) {
		try {
			CatalogFactoryBase.clear();
			new ConfigParser().parse(DependencyCollectorTest.class.getResource(url));
		} catch (Exception e) {// Exception type cannot be more specified, due
			// to parse()-signature
			// TODO use logger
			e.printStackTrace();
		}
		return CatalogFactoryBase.getInstance().getCatalog();
	}

}
