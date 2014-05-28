package cc.commandmanager.core;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class DependencyCollectorTest {

	private DependencyCollector dependencyCollector;
	private Map<String, Class<? extends Command>> catalog;

	@Before
	public void init() {
		catalog = Maps.newHashMap();
	}

	@Test
	public void testUpdateDependencies_onEmptyDependencies() {
		Map<String, Set<String>> dependencies = new HashMap<String, Set<String>>();

		DependencyCollector.updateDependencies("command", dependencies, new HashSet<String>(Arrays.asList("after")),
				new HashSet<String>(Arrays.asList("before")));

		assertThat(dependencies.keySet()).containsOnly("command", "after");
		assertThat(dependencies.get("after")).contains("command");
		assertThat(dependencies.get("command")).contains("before");
	}

	@Test
	public void testUpdateDependencies_onNonEmptyDependencies() {
		Map<String, Set<String>> dependencies = new HashMap<String, Set<String>>();
		dependencies.put("command", Sets.newHashSet("before"));
		dependencies.put("after", Sets.newHashSet("command"));

		DependencyCollector.updateDependencies("command", dependencies,
				new HashSet<String>(Arrays.asList("additionalAfter")),
				new HashSet<String>(Arrays.asList("additionalBefore")));

		assertThat(dependencies.keySet()).contains("additionalAfter");
		assertThat(dependencies.get("command")).contains("additionalBefore");
	}

	@Test
	public void testGetDependencies_beforeDependencies() {
		catalog.put("DummyCommand1", DummyCommand1.class);
		catalog.put("DummyCommand2", DummyCommand2.class);
		catalog.put("DummyCommand3", DummyCommand3.class);

		dependencyCollector = new DependencyCollector(Catalog.fromMap(catalog));
		Map<String, Set<String>> dependencies = dependencyCollector.getDependencies();

		assertThat(dependencies.get("DummyCommand1")).contains();
		assertThat(dependencies.get("DummyCommand2")).containsOnly("DummyCommand1");
		assertThat(dependencies.get("DummyCommand3")).containsOnly("DummyCommand2", "DummyCommand1");
	}

	@Test
	public void testGetDependencies_afterDependencies() {
		catalog.put("DummyCommand1", DummyCommand1.class);
		catalog.put("DummyCommand2", DummyCommand2.class);
		catalog.put("DummyCommand4", DummyCommand4.class);

		dependencyCollector = new DependencyCollector(Catalog.fromMap(catalog));
		assertThat(dependencyCollector.getDependencies().get("DummyCommand2")).containsOnly("DummyCommand1",
				"DummyCommand4");
	}

	@Test
	public void testGetDependencies_optionalBeforeDependencies() {
		catalog.put("DummyCommand1", DummyCommand1.class);
		catalog.put("DummyCommand2", DummyCommand2.class);
		catalog.put("DummyCommand3", DummyCommand3.class);

		dependencyCollector = new DependencyCollector(Catalog.fromMap(catalog));
		Map<String, Set<String>> dependencies = dependencyCollector.getDependencies();

		assertThat(dependencies.get("DummyCommand3")).contains("DummyCommand1", "DummyCommand2");
	}

	@Test
	public void testGetDependencies_optionalAfterDependencies() {
		catalog.put("DummyCommand1", DummyCommand1.class);
		catalog.put("DummyCommand2", DummyCommand2.class);
		catalog.put("DummyCommand3", DummyCommand3.class);
		catalog.put("DummyCommand4", DummyCommand4.class);
		dependencyCollector = new DependencyCollector(Catalog.fromMap(catalog));

		assertThat(dependencyCollector.getDependencies().get("DummyCommand3")).contains("DummyCommand4");
	}

	@Test
	public void testOrderCommands() {
		catalog.put("DummyCommand1", DummyCommand1.class);
		catalog.put("DummyCommand2", DummyCommand2.class);
		catalog.put("DummyCommand3", DummyCommand3.class);
		catalog.put("DummyCommand4", DummyCommand4.class);
		catalog.put("DummyCommand5", DummyCommand5.class);
		dependencyCollector = new DependencyCollector(Catalog.fromMap(catalog));
		final List<String> orderedCommands = dependencyCollector.orderCommands(dependencyCollector.getDependencies());
		orderedCommands.remove(("DummyCommand5"));
		assertThat(orderedCommands)
				.containsSequence("DummyCommand1", "DummyCommand4", "DummyCommand2", "DummyCommand3");
	}

	private static final String DOT_DIRECTORY = "./etc";
	private static final String DOT_FILE = "graph.dot";

	@AfterClass
	public static void removeFileAndDirCreatedForTests() {
		if (dotFileExists()) {
			new File(DOT_DIRECTORY + "/" + DOT_FILE).delete();
			new File(DOT_DIRECTORY).delete();
		}
	}

	private static boolean dotFileExists() {
		return new File(DOT_DIRECTORY + "/" + DOT_FILE).exists();
	}

}
