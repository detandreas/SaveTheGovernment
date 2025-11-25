package budget.util;

import java.util.UUID;

import budget.model.enums.UserRole;


/**
 * Provides methods for validating user input.
 */
public final class InputValidator {

    private static final int MIN_FULL_NAME_LENGTH = 7;
    private static final int MIN_PASSWORD_LENGTH = 8;

    /**
     * Unaccessible constructor.
     * Prevents instances for this class
     */
    private InputValidator() { };
    /**
     * Checks if a string is a valid UUID.
     * @param s represents a UUID
     * @return True if a string matches UUID toString
     */
    public static boolean isUUID(String s)
    throws IllegalArgumentException {
        if (s == null) {
            return false;
        }
        try {
            UUID.fromString(s);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    /**
     * Checks if a string is a Valid UserName.
     * @param userName user name inside budget system
     * @return True if userName consists characters
     * from the alphabet,numbers,_
     */
    public static boolean isUserName(String userName) {
        return userName != null && userName.matches("^[A-Za-z0-9_]+$");
    }
    /**
    * Checks if a string is a valid Full Name.
    * @param fullName user Full Name
    * @return True if Full Name has more than 7 characters,
    * contains only Latin characters, spaces, and hyphens,
    * and each word starts with uppercase
    */
    public static boolean isFullName(String fullName) {
       if (fullName == null
       || fullName.trim().length() < MIN_FULL_NAME_LENGTH) {
           return false;
       }

       String trimmed = fullName.trim();

       // Check if it contains at least one space (first name + last name)
       if (!trimmed.contains(" ")) {
           return false;
       }

       // Allow multiple words, each starting with uppercase
       // Supports names like "John Doe", "Mary Jane Smith"
       return trimmed.matches("^[A-Z][a-z]+(?:[\\s-][A-Z][a-z]+)*$");
    }
    /**
     * Checks if user password meets strong password requirements.
     * A strong password must:
     * - Be at least 8 characters long
     * - Contain at least one lowercase letter (a-z)
     * - Contain at least one uppercase letter (A-Z)
     * - Contain at least one digit (0-9)
     * - Contain at least one special character (@$!%*?&)
     * - Only contain allowed characters (letters, digits,
     * and specified special characters)
     *
     * @param pw user password to validate
     * @return true if password meets all strong password requirements,
     * false otherwise
     */
    public static boolean isPasswordStrong(String pw) {
        if (pw == null || pw.length() < MIN_PASSWORD_LENGTH) {
            return false;
        }
        return pw.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)"
        + "(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");
    }
    /**
     * Checks if provided object is valid.
     * A valid object is a non null object
     * @param o the object to validate
     * @return true if object is not null
     */
    public static boolean isNonNull(Object o) {
        return o != null;
    }
    /**
     * Checks if the provided UserRole is valid.
     * A valid UserRole is a non-null enum value that exists
     * in the UserRole enumeration.
     *
     * @param role the UserRole to validate
     * @return true if role is a valid UserRole enum value, false otherwise
     */
    public static boolean isValidUserRole(UserRole role) {
        return role != null;
    }
}
