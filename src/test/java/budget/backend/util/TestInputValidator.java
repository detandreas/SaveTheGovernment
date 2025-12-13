package budget.backend.util;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;


public class TestInputValidator {    

    @Test 
    void testIsUUID() {
        assertFalse(InputValidator.isUUID(null),
                                        "Failure - UUID can't be null");
        UUID random = UUID.randomUUID();
        assertTrue(InputValidator.isUUID(random.toString()),
                                    "Failure - Valid UUID returned false");
        assertFalse(InputValidator.isUUID("12983"),
                                    "Failure - invalid UUID returned true");
    }
    
    @Test
    void testIsUserName() {
        assertTrue(InputValidator.isUserName("User_123"),
                                    "Failure - correct userName returned false");
        assertTrue(InputValidator.isUserName("abcXYZ"),
                                    "Failure - correct userName returned false");
        assertFalse(InputValidator.isUserName("John Doe"),
                                    "Failure - space not allowed");
        assertFalse(InputValidator.isUserName("john-doe"),
                                    "Failure - hyphen not allowed");
        assertFalse(InputValidator.isUserName(null),
                                    "Failure - null not rejected");
    }

    @Test
    void testIsFullName() {
        assertTrue(InputValidator.isFullName("John Doe"),
                                    "Failure - correct FullName returned false");
        assertTrue(InputValidator.isFullName("Mary Jane Smith"),
                                    "Failure - correct FullName returned false");
        assertTrue(InputValidator.isFullName("Anna-Maria Johnson"),
                                    "Failure - correct FullName returned false");

        assertFalse(InputValidator.isFullName("john doe"),
                                    "Failure - lowercase start should return false");
        assertFalse(InputValidator.isFullName("John"),
                                    "Failure - only one word should return false");
        assertFalse(InputValidator.isFullName("J D"),
                                    "Failure - too shord should return false");
        assertFalse(InputValidator.isFullName(null),
                                    "Failure - null should return false");
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
        assertTrue(InputValidator.isPasswordStrong("Aa1@aaaa"),
                                    "Failure - valid strong password returned false");
        assertTrue(InputValidator.isPasswordStrong("Strong1!"),
                                    "Failure - valid strong password returned false"); // both are strong passwords

        assertFalse(InputValidator.isPasswordStrong("weakpass"),
                                    "Failure - only lowercase should return false"); // only lowercase, only letters
        assertFalse(InputValidator.isPasswordStrong("NOLOWER1!"),
                                    "Failure - no lowercase should return false"); // only uppercase, no digits or special chars
        assertFalse(InputValidator.isPasswordStrong("NOLOWERNODIGIT"),
                                    "Failure - no lowercase, digits or special chars should return false"); // only uppercase, or special chars
        assertFalse(InputValidator.isPasswordStrong("noupper1!"),
                                    "Failure - no uppercase should return false"); // no uppercase
        assertFalse(InputValidator.isPasswordStrong("NoDigit!!"),
                                    "Failure - no digits should return false"); // no digits
        assertFalse(InputValidator.isPasswordStrong("NoSpec1"),
                                    "Failure - no special chars should return false"); // no special chars
        assertFalse(InputValidator.isPasswordStrong(null),
                                    "Failure - null should return false");
    }

     /**
     * Tests that non-null objects are considered valid.
     */
    @Test
    void testIsNonNull() {
        assertTrue(InputValidator.isNonNull("Hello"),
                                    "Failure - non-null string should return true");
        assertTrue(InputValidator.isNonNull(123),
                                    "Failure - non-null integer should return true");
        assertFalse(InputValidator.isNonNull(null),
                                    "Failure - null should return false");
    }

     /**
     * Tests validation of UserRole values.
     * The method considers any non-null enum value as valid.
     */
    @Test
    void testIsValidUserRole() {
        assertTrue(InputValidator.isValidUserRole(budget.backend.model.enums.UserRole.CITIZEN),
                                    "Failure - valid UserRole should return true");
        assertFalse(InputValidator.isValidUserRole(null),
                                    "Failure - null UserRole should return false");
    }
}
