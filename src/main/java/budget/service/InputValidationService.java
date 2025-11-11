package budget.service;

import budget.model.enums.UserRole;
import budget.model.domain.user.User;
import budget.util.InputValidator;
import budget.exceptions.ValidationException;

/**
 * Service class that provides comprehensive user input validation.
 *
 * utilizing the InputValidator utility class
 * to perform various validation checks.
 * It provides business-logic-level validation methods
 * for user registration,
 * authentication, and user data modification operations.
 */
public final class InputValidationService {
    /**
     * Validates a new user object for registration.
     * Checks username, full name, user role, hashed password, id validity.
     *
     * @param u the User object to validate
     * @throws ValidationException if any validation check fails
     */
    public void validateNewUser(User u)
        throws ValidationException {
        if (!InputValidator.isNonNull(u)) {
            fail("User is null");
        }
        if (!InputValidator.isUserName(u.getUserName())) {
            fail("invalid username");
        }
        if (!InputValidator.isFullName(u.getFullName())) {
            fail("invalid fullname");
        }
        if (!InputValidator.isValidUserRole(u.getUserRole())) {
            fail("invalid user role");
        }
        if (!InputValidator.isNonNull(u.getHashPassword())) {
            fail("invalid hashed password");
        }
        if (!InputValidator.isNonNull(u.getId())) {
            fail("invalid user id");
        }
    }
    /**
     * Validates a user object for updating info.
     * Checks username, full name, user role, hashed password.
     * User id stays the same.
     * @param u user object to validate
     * @throws ValidationException if any validation checks fails
     */
    public void validateUserUpdate(User u)
        throws ValidationException {
        if (!InputValidator.isNonNull(u)) {
            fail("User is null");
        }
        if (!InputValidator.isUserName(u.getUserName())) {
            fail("invalid username");
        }
        if (!InputValidator.isFullName(u.getFullName())) {
            fail("invalid fullname");
        }
        if (!InputValidator.isValidUserRole(u.getUserRole())) {
            fail("invalid user role");
        }
        if (!InputValidator.isNonNull(u.getHashPassword())) {
            fail("invalid hashed password");
        }
    }
    /**
     * Validates a role change.
     * checks if old/new role is null
     * @param from  old user role
     * @param to new user role
     * @throws ValidationException if any role is null
     */
    public void validateRoleChange(UserRole from, UserRole to)
        throws ValidationException {
        if (from == null || to == null) {
            fail("user roles cannot be null");
        }
    }
    /**
     * Throws a ValidationException with the specified message.
     *
     * @param msg the error message
     * @throws ValidationException always thrown with the provided message
     */
    public void fail(String msg)
    throws ValidationException {
        throw new ValidationException(msg);
    }
}
