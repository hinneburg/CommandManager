package cc.commandmanager.core;

import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

public class CommandManagementIntegrationTest {

	public static final String EXECUTED_COMMANDS = "executedCommands";
	private Context context = new Context();
	private CommandManagement commandManagement = new CommandManagement(
			"src/test/resources/CommandManagementIntegrationTest-catalog.xml", context);;

	@Test
	public void test() {
		context.bind(EXECUTED_COMMANDS, Lists.newArrayList());

		commandManagement.executeAllCommands();

		@SuppressWarnings("unchecked")
		List<Class<? extends Command>> executedCommands = context.get(EXECUTED_COMMANDS, List.class);
		assertThat(executedCommands).containsExactly(DummyCommand1.class, DummyCommand2.class, DummyCommand3.class);
	}

}
