package cc.commandmanager.core;

import javax.annotation.Nullable;

import net.sf.qualitycheck.Check;
import net.sf.qualitycheck.exception.IllegalNullArgumentException;

import com.google.common.base.Objects;

/**
 * Optional instance of type <b>T</b>. If the instance is not present you may receive a note why by calling
 * {@linkplain Optional#getNote()}.
 * <p>
 * Mind that the stored value and note may not be immutable and thus {@linkplain Optional} is also not guaranteed to be
 * immutable.
 * 
 * @param <T>
 *            optional value type
 */
public class Optional<T> {

	private static final String EMPTY_MESSAGE = "";

	private final T value;
	private final Object note;

	/**
	 * Creates a new {@linkplain Optional} having the given value.
	 * 
	 * @param value
	 */
	public Optional(@Nullable T value) {
		this(value, null);
	}

	/**
	 * Creates a new {@linkplain Optional} having the given value and the given note. The note can be used to explain
	 * why the value is missing.
	 * 
	 * @param value
	 *            or null
	 * @param note
	 *            or null
	 */
	public Optional(@Nullable T value, @Nullable Object note) {
		this.value = value;
		this.note = note == null ? EMPTY_MESSAGE : note;
	}

	/**
	 * @return the stored non-null value
	 * 
	 * @throws IllegalNullArgumentException
	 *             if the value is not present
	 */
	public T get() {
		return Check.notNull(value, "value");
	}

	/**
	 * @return the stored value or null if no value is stored
	 */
	public T getOrNull() {
		return value;
	}

	/**
	 * @return whether the value is present (non-null)
	 */
	public boolean isPresent() {
		return getOrNull() != null;
	}

	/**
	 * @return the note that may explain why the value is missing
	 */
	public Object getNote() {
		return note;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("value", value).add("note", note).toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((note == null) ? 0 : note.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Optional)) {
			return false;
		}
		Optional other = (Optional) obj;
		if (note == null) {
			if (other.note != null) {
				return false;
			}
		} else if (!note.equals(other.note)) {
			return false;
		}
		if (value == null) {
			if (other.value != null) {
				return false;
			}
		} else if (!value.equals(other.value)) {
			return false;
		}
		return true;
	}

}
