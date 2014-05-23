package cc.commandmanager.core;

import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

public class CommandManagementIntegrationTest {

	public static final String EXECUTED_COMMANDS = "executedCommands";
	private CommandManagement _commandManagement;

	@Before
	public void setUp() {
		Context context = new Context();
		context.bind(EXECUTED_COMMANDS, Lists.newArrayList());
		_commandManagement = new CommandManagement(context,
				"src/test/resources/CommandManagementIntegrationTest-catalog.xml");
	}

	@Test
	public void test() {
		_commandManagement.executeAllCommands();
		@SuppressWarnings("unchecked")
		List<Class<? extends Command>> executedCommands = (List<Class<? extends Command>>) _commandManagement
				.getContext().get(EXECUTED_COMMANDS);
		assertThat(executedCommands).containsExactly(DummyCommand1.class, DummyCommand2.class, DummyCommand3.class);
	}

}
