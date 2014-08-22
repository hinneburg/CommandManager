package cc.commandmanager.core;

import cc.commandmanager.core.commandimplementations.*;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;

/**
 * command classes used in the following: DummyCommand1 name="command" DummyCommand2 name="after"
 * DummyCommand3" name="before"
 */
public class CommandManagerIntegrationTest {

	public static final String EXECUTED_COMMANDS = "executedCommands";

	private Context context;
	private CommandManager commandManager;
	private File catalog;

	{
		URL catalogResource = CommandManagerIntegrationTest.class.getClassLoader().getResource(
				"catalog-with-three-commands.xml");
		if (catalogResource != null) {
			catalog = new File(catalogResource.getFile());
		} else {
			throw new RuntimeException("No resource for catalog-with-three-commands.xml could have been found.");
		}
	}

	@Before
	public void setUp() {
		context = new Context();
		context.bind(EXECUTED_COMMANDS, Lists.newArrayList());
		commandManager = new CommandManager(CommandGraph.fromXml(catalog).get());
	}

	@Test
	public void testExecuteAllCommandsExecutesInRightOrder() {
		commandManager.executeAllCommands(context);

		@SuppressWarnings("unchecked")
		List<Class<? extends Command>> executedCommands = context.get(EXECUTED_COMMANDS, List.class);
		assertThat(executedCommands).containsExactly(DummyCommand3.class, DummyCommand1.class, DummyCommand2.class);
	}

	@Test
	public void testExecuteCommandsExecutesInRightOrder() {
		commandManager.executeCommands(context, "after", "before");

		@SuppressWarnings("unchecked")
		List<Class<? extends Command>> executedCommands = context.get(EXECUTED_COMMANDS, List.class);
		assertThat(executedCommands).containsExactly(DummyCommand3.class, DummyCommand2.class);
	}

	@Test
	public void testExecuteCommandsGracefullyExecutesInRightOrder() {
		commandManager.executeCommandsGracefully(context, "command");

		@SuppressWarnings("unchecked")
		List<Class<? extends Command>> executedCommands = context.get(EXECUTED_COMMANDS, List.class);
		assertThat(executedCommands).containsExactly(DummyCommand3.class, DummyCommand1.class);
	}

	@Test
	public void testExecuteConnectedComponentsExecutesInRightOrder() {
		URL catalogResource = CommandManagerIntegrationTest.class.getClassLoader().getResource(
				"catalog-for-connected-component-testing.xml");
		if (catalogResource != null) {
			commandManager = new CommandManager(CommandGraph.fromXml(new File(catalogResource.getFile())).get());
		} else {
			fail("No resource for catalog-for-connected-component-testing.xml could have been found.");
		}
		commandManager.executeConnectedComponentsContaining(Lists.newArrayList("GroupAFirst", "GroupCSecond"), context);

		@SuppressWarnings("unchecked")
		List<Class<? extends Command>> executedCommands = context.get(EXECUTED_COMMANDS, List.class);
		assertThat(executedCommands).containsExactly(GroupAFirst.class, GroupASecond.class, GroupAThird.class,
				GroupCFirst.class, GroupCSecond.class, GroupCThird.class);
	}

	@Test
	public void testClassMethodExecutesInRightOrder() {
		CommandManager.executeCommands(CommandGraph.fromXml(catalog).get(), context);

		@SuppressWarnings("unchecked")
		List<Class<? extends Command>> executedCommands = context.get(EXECUTED_COMMANDS, List.class);
		assertThat(executedCommands).containsExactly(DummyCommand3.class, DummyCommand1.class, DummyCommand2.class);
	}

}
