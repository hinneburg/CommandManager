package cc.commandmanager.core;

import static org.fest.assertions.Assertions.assertThat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class DependencyCollectorTest {

	Map<String, Set<String>> dependencies;

	@Before
	public void setup() {
		dependencies = Maps.newHashMap();
	}

	@Test
	public void testUpdateDependencies_onEmptyDependencies() {
		DependencyCollector.updateDependencies("command", dependencies, new HashSet<String>(Arrays.asList("after")),
				new HashSet<String>(Arrays.asList("before")));

		assertThat(dependencies.keySet()).containsOnly("command", "after");
		assertThat(dependencies.get("after")).contains("command");
		assertThat(dependencies.get("command")).contains("before");
	}

	@Test
	public void testUpdateDependencies_onNonEmptyDependencies() {
		dependencies.put("command", Sets.newHashSet("before"));
		dependencies.put("after", Sets.newHashSet("command"));

		DependencyCollector.updateDependencies("command", dependencies,
				new HashSet<String>(Arrays.asList("additionalAfter")),
				new HashSet<String>(Arrays.asList("additionalBefore")));

		assertThat(dependencies.keySet()).contains("additionalAfter");
		assertThat(dependencies.get("command")).contains("additionalBefore");
	}

	@Test(expected = IllegalStateException.class)
	public void testOrderCommands_illegalCircularDependency_twoCommands() {
		dependencies.put("A", Sets.newHashSet("B"));
		dependencies.put("B", Sets.newHashSet("A"));

		DependencyCollector.orderCommands(dependencies);
	}

	@Test(expected = IllegalStateException.class)
	public void testOrderCommands_illegalCircularDependency_threeCommands() {
		dependencies.put("A", Sets.newHashSet("B"));
		dependencies.put("B", Sets.newHashSet("C"));
		dependencies.put("C", Sets.newHashSet("A"));

		DependencyCollector.orderCommands(dependencies);
	}

}
