package budget.exceptions;

public class UserNotAuthorizedException extends RuntimeException {
    /**
     * Constructor for UserNotAuthorizedException.
     * @param message exception message
     */
    public UserNotAuthorizedException(String message) {
        super(message);
    }
}
