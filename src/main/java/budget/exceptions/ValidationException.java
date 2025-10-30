package budget.exceptions;

public class ValidationException extends RuntimeException {
    /**
     * Constructor for ValidationException.
     * @param msg exception message
     */
    public ValidationException(String msg) {
        super(msg);
    }

}
