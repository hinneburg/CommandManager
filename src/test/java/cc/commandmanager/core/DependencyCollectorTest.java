package cc.commandmanager.core;

import static org.fest.assertions.Assertions.assertThat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class DependencyCollectorTest {

	@Test
	public void testUpdateDependencies_onEmptyDependencies() {
		Map<String, Set<String>> dependencies = Maps.newHashMap();

		DependencyCollector.updateDependencies("command", dependencies, new HashSet<String>(Arrays.asList("after")),
				new HashSet<String>(Arrays.asList("before")));

		assertThat(dependencies.keySet()).containsOnly("command", "after");
		assertThat(dependencies.get("after")).contains("command");
		assertThat(dependencies.get("command")).contains("before");
	}

	@Test
	public void testUpdateDependencies_onNonEmptyDependencies() {
		Map<String, Set<String>> dependencies = Maps.newHashMap();
		dependencies.put("command", Sets.newHashSet("before"));
		dependencies.put("after", Sets.newHashSet("command"));

		DependencyCollector.updateDependencies("command", dependencies,
				new HashSet<String>(Arrays.asList("additionalAfter")),
				new HashSet<String>(Arrays.asList("additionalBefore")));

		assertThat(dependencies.keySet()).contains("additionalAfter");
		assertThat(dependencies.get("command")).contains("additionalBefore");
	}

}
