package budget.backend.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestPasswordUtils {

    private String hash;
    private String password;

    @BeforeEach
    void setUp() {
        password = "Password";
        hash = null;
    }

    @Test
    void testPassword_ReturnsNonNull() {
        hash = PasswordUtils.hashPassword(password);
         assertNotNull(hash, "Failure - Hash should not be null");
    }

    @Test
    void testHashPassword_SamePasswordProducesSameHash() {
        String hash1 = PasswordUtils.hashPassword(password);
        String hash2 = PasswordUtils.hashPassword(password);
        
        assertEquals(hash1, hash2,
            "Failure - Same password should produce identical hashes");
    }

    @Test
    void testHashPassword_DifferentPasswordsProduceDifferentHashes() {
        String password1 = "password1";
        String password2 = "password2";
        
        String hash1 = PasswordUtils.hashPassword(password1);
        String hash2 = PasswordUtils.hashPassword(password2);
        
        assertNotEquals(hash1, hash2,
            "Failure - Different passwords should produce different hashes");
    }

    @Test
    void testHashPassword_EmptyString() {
        password = "";
        hash = PasswordUtils.hashPassword(password);
        
        assertNotNull(hash,
            "Failure - Hash of empty string should not be null");
        assertEquals(64, hash.length(),
            "Failure - Hash should still be 64 characters");
    }

    @Test
    void testHashPassword_SpecialCharacters() {
        password = "p@s$%";
        hash = PasswordUtils.hashPassword(password);
        
        assertNotNull(hash,
            "Failure - Hash with special characters should not be null");
        assertEquals(64, hash.length(),
            "Failure - Hash should be 64 characters long");
    }
}
