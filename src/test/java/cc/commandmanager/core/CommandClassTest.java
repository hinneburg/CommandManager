package cc.commandmanager.core;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

public class CommandClassTest {

	private CommandClass command1;
	private CommandClass command2;

	@Before
	public void createCommands() {
		command1 = new CommandClass("Command1", "cc.commandmanager.core.CommandClassTest$Command1");
		command2 = new CommandClass("Command2", "cc.commandmanager.core.CommandClassTest$Command2");
	}

	@Test
	public void testEquals() {
		assertThat(command1.equals(command1)).isTrue();
		assertThat(command1.equals(command2)).isFalse();
		assertThat(command1.equals(null)).isFalse();
		assertThat(command1.equals(new CommandClass("Command1", "cc.commandmanager.core.CommandClassTest$Command1")))
				.isTrue();
	}

	@Test
	public void testHasNameNameAs() {
		assertThat(command1.hasSameNameAs(command1)).isTrue();
		assertThat(command1.hasSameNameAs(command2)).isFalse();
		assertThat(command1.hasSameNameAs(null)).isFalse();
		assertThat(command1.hasSameNameAs(new CommandClass("Command1", ""))).isTrue();
	}

	@Test
	public void testEqualObjectsHaveEqualHashcodes() {
		assertThat(command1.hashCode()).isEqualTo(
				(new CommandClass("Command1", "cc.commandmanager.core.CommandClassTest$Command1")).hashCode());
	}

	@Test
	public void testNewInstance() {
		assertThat(command1.newInstance()).isInstanceOf(Command1.class);
	}

	@Test(expected = CommandNotInstantiableException.class)
	public void testNewInstance_notAccessableCommand() {
		(new CommandClass("NotAccessableCommand", "cc.commandmanager.core.CommandClassTest$NotAccessableCommand"))
				.newInstance();
	}

	@Test(expected = CommandNotInstantiableException.class)
	public void testNewInstance_notInstantiableCommand() {
		(new CommandClass("NotInstantiableCommand", "cc.commandmanager.core.CommandClassTest$NotInstantiableCommand"))
				.newInstance();
	}

	@Test(expected = CommandNotInstantiableException.class)
	public void testNewInstance_classNotFound() {
		(new CommandClass("NotFound", "not.findable.Class")).newInstance();
	}

	@Test
	public void test() {
		System.out.println((new CommandClass("NotFound", "not.findable.Class")).newInstance());
	}

	public static class Command1 extends DummyCommand {

	}

	public static class Command2 extends DummyCommand {

	}

	public static abstract class NotInstantiableCommand implements Command {

	}

	public static class NotAccessableCommand extends DummyCommand {

		private NotAccessableCommand() {

		}

	}

}
