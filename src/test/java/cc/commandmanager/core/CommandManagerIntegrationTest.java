package cc.commandmanager.core;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;
import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

public class CommandManagerIntegrationTest {

	public static final String EXECUTED_COMMANDS = "executedCommands";

	private Context context = new Context();
	private CommandManager commandManagement = new CommandManager(new File(CommandManagerIntegrationTest.class
			.getClassLoader().getResource("CommandManagerIntegrationTest-catalog.xml").getFile()), context);

	@Test
	public void testExecuteAllCommands() {
		context.bind(EXECUTED_COMMANDS, Lists.newArrayList());

		commandManagement.executeAllCommands();

		@SuppressWarnings("unchecked")
		List<Class<? extends Command>> executedCommands = context.get(EXECUTED_COMMANDS, List.class);
		assertThat(executedCommands).containsExactly(DummyCommand3.class, DummyCommand1.class, DummyCommand2.class);
	}

}
