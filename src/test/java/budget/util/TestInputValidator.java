package budget.util;

import org.junit.jupiter.api.Test;
public class TestInputValidator {    
    
    @Test
    public static boolean isUUID(String s) {
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
     * Tests validation rules for usernames.
     * Valid usernames contain letters, digits or underscores.
     */
    @Test
    void testIsUserName() {
        assertTrue(InputValidator.isUserName("User_123"));
        assertTrue(InputValidator.isUserName("abcXYZ")); // both correct usernames

        assertFalse(InputValidator.isUserName("John Doe")); // space not allowed
        assertFalse(InputValidator.isUserName("john-doe")); // hyphen not allowed
        assertFalse(InputValidator.isUserName(null));       // null rejected
    }

     /**
     * Tests validation of full names.
     * Full names must:
     * - be at least 7 characters long
     * - contain at least one space
     * - consist of words starting with uppercase letters
     */
    @Test
    void testIsFullName() {
        assertTrue(InputValidator.isFullName("John Doe"));
        assertTrue(InputValidator.isFullName("Mary Jane Smith"));
        assertTrue(InputValidator.isFullName("Anna-Maria Johnson")); // all are correct

        assertFalse(InputValidator.isFullName("john doe")); // lowercase start
        assertFalse(InputValidator.isFullName("John"));      // only one word
        assertFalse(InputValidator.isFullName("J D"));       // too short
        assertFalse(InputValidator.isFullName(null));
    }


    /**
     * Tests strong password validation.
     * A strong password contains:
     * - lowercase
     * - uppercase
     * - digit
     * - special character
     * - minimum length 8
     */
    @Test
    void testIsPasswordStrong() {
        assertTrue(InputValidator.isPasswordStrong("Aa1@aaaa"));
        assertTrue(InputValidator.isPasswordStrong("Strong1!")); // both are strong passwords

        assertFalse(InputValidator.isPasswordStrong("weakpass")); // only lowercase, only letters
        assertFalse(InputValidator.isPasswordStrong("NOLOWER1!")); // only uppercase, no digits or special chars
        assertFalse(InputValidator.isPasswordStrong("NOLOWERNODIGIT")); // only uppercase, or special chars
        assertFalse(InputValidator.isPasswordStrong("noupper1!")); // no uppercase
        assertFalse(InputValidator.isPasswordStrong("NoDigit!!")); // no digits
        assertFalse(InputValidator.isPasswordStrong("NoSpec1")); // no special chars
        assertFalse(InputValidator.isPasswordStrong(null));
    }

     /**
     * Tests that non-null objects are considered valid.
     */
    @Test
    void testIsNonNull() {
        assertTrue(InputValidator.isNonNull("Hello"));
        assertTrue(InputValidator.isNonNull(123));
        assertFalse(InputValidator.isNonNull(null));
    }

     /**
     * Tests validation of UserRole values.
     * The method considers any non-null enum value as valid.
     */
    @Test
    void testIsValidUserRole() {
        assertTrue(InputValidator.isValidUserRole(budget.model.enums.UserRole.CITIZEN));
        assertFalse(InputValidator.isValidUserRole(null));
    }
}