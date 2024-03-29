package cc.commandmanager.core;

import javax.annotation.Nullable;

import net.sf.qualitycheck.Check;
import net.sf.qualitycheck.exception.IllegalInstanceOfArgumentException;

/**
 * Result state of a {@linkplain Command} execution. It can either be {@linkplain Success}, {@linkplain Warning} or
 * {@linkplain Failure}.
 * <p>
 * If it is a {@linkplain Warning} or a {@linkplain Failure} it will provide a message and a cause.
 */
public abstract class ResultState {

	private static final Success SUCCESS = new Success();

	/**
	 * Returns a {@linkplain Success} result state.
	 */
	public static Success success() {
		return SUCCESS;
	}

	/**
	 * Returns a {@linkplain Warning} result state having the provided message and cause.
	 * 
	 * @param message
	 * @param cause
	 * @return {@linkplain Warning}
	 */
	public static Warning warning(String message, Throwable cause) {
		return new Warning(message, cause);
	}

	/**
	 * Returns a {@linkplain Warning} result state having the provided message and no cause.
	 * 
	 * @param message
	 * @return {@linkplain Warning}
	 */
	public static Warning warning(String message) {
		return new Warning(message, null);
	}

	/**
	 * Returns a {@linkplain Warning} result state having the provided cause. Its message will be set as the message of
	 * the cause.
	 * 
	 * @param cause
	 * @return {@linkplain Warning}
	 */
	public static Warning warning(Throwable cause) {
		return new Warning(cause.getMessage(), cause);
	}

	/**
	 * Returns a {@linkplain Failure} result state having the provided message and cause.
	 * 
	 * @param message
	 * @param cause
	 * @return {@linkplain Failure}
	 */
	public static Failure failure(String message, Throwable cause) {
		return new Failure(message, cause);
	}

	/**
	 * Returns a {@linkplain Failure} result state having the provided message and no cause.
	 * 
	 * @param message
	 * @return {@linkplain Failure}
	 */
	public static Failure failure(String message) {
		return new Failure(message, null);
	}

	/**
	 * Returns a {@linkplain Failure} result state having the provided cause. Its message will be set as the message of
	 * the cause.
	 * 
	 * @param cause
	 * @return {@linkplain Failure}
	 */
	public static Failure failure(Throwable cause) {
		return new Failure(cause.getMessage(), cause);
	}

	/**
	 * @return whether this is a {@linkplain Success}
	 */
	public abstract boolean isSuccess();

	/**
	 * @return whether this is a {@linkplain Warning}
	 */
	public abstract boolean isWarning();

	/**
	 * @return whether this is a {@linkplain Failure}
	 */
	public abstract boolean isFailure();

	/**
	 * @return {@code true} if this {@linkplain Warning} or {@linkplain Failure} has a cause, {@code false} otherwise.
	 * 
	 * @throws IllegalInstanceOfArgumentException
	 *             if this is not a {@linkplain Warning} or {@linkplain Failure}.
	 */
	public boolean hasCause() {
		return getCause() != null;
	}

	/**
	 * @return the message of this {@linkplain ResultState}.
	 * 
	 * @throws IllegalInstanceOfArgumentException
	 *             if this is a {@linkplain Success}.
	 */
	public abstract String getMessage();

	/**
	 * @return the cause of this {@linkplain ResultState}, or {@link null} if there is no cause.
	 * 
	 * @throws IllegalInstanceOfArgumentException
	 *             if this is {@linkplain Success}.
	 */
	@Nullable
	public abstract Throwable getCause();

	/**
	 * {@linkplain ResultState} of a {@linkplain Command} that has been executed successfully without any problems.
	 */
	public static final class Success extends ResultState {

		@Override
		public String toString() {
			return "Execution completed successfully!";
		}

		@Override
		public boolean isSuccess() {
			return true;
		}

		@Override
		public boolean isWarning() {
			return false;
		}

		@Override
		public boolean isFailure() {
			return false;
		}

		@Override
		public String getMessage() {
			throw new IllegalStateException("Success states do not have a message.");
		}

		@Override
		@Nullable
		public Throwable getCause() {
			throw new IllegalStateException("Success states do not have a cause.");
		}

	}

	private static abstract class WarningOrFailure extends ResultState {

		protected final String message;
		protected final Throwable cause;

		protected WarningOrFailure(String message, @Nullable Throwable cause) {
			this.message = Check.notNull(message, "message");
			this.cause = cause;
		}

		@Override
		public String getMessage() {
			return message;
		}

		@Override
		@Nullable
		public Throwable getCause() {
			return cause;
		}

		@Override
		public int hashCode() {
			int result = 1;
			final int prime = 31;
			result = prime * result + getMessage().hashCode();
			result = hasCause() ? prime * result + getCause().hashCode() : result;
			return result;
		}

		/**
		 * @return {@code true} if the given {@linkplain Object} corresponds the same class. Furthermore
		 *         {@linkplain #getMessage()} and {@linkplain #getCause()} must return the same result.
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null || getClass() != obj.getClass()) {
				return false;
			}
			WarningOrFailure that = (WarningOrFailure) obj;
			final boolean hasSameMessage = getMessage().equals(that.getMessage());
			if (!hasCause()) {
				return hasSameMessage && !that.hasCause();
			} else {
				return hasSameMessage && getCause().equals(that.getCause());
			}
		}

	}

	/**
	 * {@linkplain ResultState} of a {@linkplain Command} that faced non-critical problems during its execution. It
	 * provides a message and a cause.
	 */
	public static final class Warning extends WarningOrFailure {

		private Warning(String message, Throwable cause) {
			super(message, cause);
		}

		@Override
		public String toString() {
			return "Execution completed with warnings: " + message;
		}

		@Override
		public boolean isSuccess() {
			return false;
		}

		@Override
		public boolean isWarning() {
			return true;
		}

		@Override
		public boolean isFailure() {
			return false;
		}

	}

	/**
	 * {@linkplain ResultState} of a {@linkplain Command} that faced critical problems and aborted its execution. It
	 * provides a message and a cause.
	 */
	public static final class Failure extends WarningOrFailure {

		public Failure(String message, Throwable cause) {
			super(message, cause);
		}

		@Override
		public String toString() {
			return "Execution failed: " + message;
		}

		@Override
		public boolean isSuccess() {
			return false;
		}

		@Override
		public boolean isWarning() {
			return false;
		}

		@Override
		public boolean isFailure() {
			return true;
		}

	}

}
