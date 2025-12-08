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
            throw new ValidationException("User is null");
        }
        if (!InputValidator.isUserName(u.getUserName())) {
            throw new ValidationException("invalid username");
        }
        if (!InputValidator.isFullName(u.getFullName())) {
            throw new ValidationException("invalid fullname");
        }
        if (!InputValidator.isNonNull(u.getHashPassword())) {
            throw new ValidationException("invalid hashed password");
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
            throw new ValidationException("User is null");
        }
        if (!InputValidator.isUserName(u.getUserName())) {
            throw new ValidationException("invalid username");
        }
        if (!InputValidator.isFullName(u.getFullName())) {
            throw new ValidationException("invalid fullname");
        }
        if (!InputValidator.isNonNull(u.getHashPassword())) {
            throw new ValidationException("invalid hashed password");
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
            throw new ValidationException("user roles cannot be null");
        }
    }
}
