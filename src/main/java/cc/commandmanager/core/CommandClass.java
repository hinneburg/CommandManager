package cc.commandmanager.core;

import javax.annotation.concurrent.Immutable;

import net.sf.qualitycheck.Check;

/**
 * Abstraction of a command type. A {@linkplain CommandClass} contains a descriptive name as well as a class name of a
 * {@linkplain Command} class.
 * <p>
 * The class name needs to be a fully qualified name for a Java class as obtained by
 * {@linkplain Class#getCanonicalName()}.
 */
@Immutable
public class CommandClass {

	private final String name;
	private final String className;

	/**
	 * Creates a new {@linkplain CommandClass} object having the given name and fully qualified class name.
	 * 
	 * @param name
	 *            of the command
	 * @param className
	 *            of the command
	 */
	public CommandClass(String name, String className) {
		this.name = Check.notNull(name, "name");
		this.className = Check.notNull(className, "className");
	}

	/**
	 * @return name of the command
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return fully qualified class name of the command
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * Checks whether this command has the same name as the other command.
	 * 
	 * @param other
	 *            command
	 * @return if this command has the same name as the other one
	 */
	public boolean hasSameSameAs(CommandClass other) {
		if (other == null) {
			return false;
		}
		return name.equals(other.name);
	}

	@Override
	public String toString() {
		return name + " (" + className + ")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + className.hashCode();
		result = prime * result + name.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		CommandClass other = (CommandClass) obj;
		return (name.equals(other.name) && className.equals(other.className));
	}

}
