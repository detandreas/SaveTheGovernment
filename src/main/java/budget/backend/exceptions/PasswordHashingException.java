package budget.backend.exceptions;


public class PasswordHashingException extends RuntimeException {

    /**
     * Constructor for PasswordHashingException.
     * @param message exception message
     */
    public PasswordHashingException(String message) {
        super(message);
    }

    /**
     * Constructor for PasswordHashingException.
     * @param message exception message
     * @param cause exception cause
     */
    public PasswordHashingException(String message, Throwable cause) {
        super(message, cause);
    }
}
