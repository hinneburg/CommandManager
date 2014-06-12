package cc.commandmanager.core;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

public class CommandClassTest {

	private CommandClass command1;
	private CommandClass command2;

	@Before
	public void createCommands() {
		command1 = new CommandClass("Command1", "cc.commandmanager.core.Command1");
		command2 = new CommandClass("Command2", "cc.commandmanager.core.Command2");
	}

	@Test
	public void testEquals() {
		assertThat(command1.equals(command1)).isTrue();
		assertThat(command1.equals(command2)).isFalse();
		assertThat(command1.equals(null)).isFalse();
		assertThat(command1.equals(new CommandClass("Command1", "cc.commandmanager.core.Command1")));
	}

	@Test
	public void testHasNameNameAs() {
		assertThat(command1.hasSameSameAs(command2)).isFalse();
		assertThat(command1.hasSameSameAs(new CommandClass("Command1", "")));
		assertThat(command1.hasSameSameAs(null)).isFalse();
	}

}
