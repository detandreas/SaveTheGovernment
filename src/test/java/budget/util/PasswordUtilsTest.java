package budget.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilsTest {

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
         assertNotNull(hash, "Hash should not be null");
    }

    @Test
    void testHashPassword_SamePasswordProducesSameHash() {
        String hash1 = PasswordUtils.hashPassword(password);
        String hash2 = PasswordUtils.hashPassword(password);
        
        assertEquals(hash1, hash2, "Same password should produce identical hashes");
    }

    @Test
    void testHashPassword_DifferentPasswordsProduceDifferentHashes() {
        String password1 = "password1";
        String password2 = "password2";
        
        String hash1 = PasswordUtils.hashPassword(password1);
        String hash2 = PasswordUtils.hashPassword(password2);
        
        assertNotEquals(hash1, hash2, "Different passwords should produce different hashes");
    }

    @Test
    void testHashPassword_EmptyString() {
        String password = "";
        String hash = PasswordUtils.hashPassword(password);
        
        assertNotNull(hash, "Hash of empty string should not be null");
        assertEquals(64, hash.length(), "Hash should still be 64 characters");
    }

    @Test
    void testHashPassword_SpecialCharacters() {
        String password = "p@s$%";
        String hash = PasswordUtils.hashPassword(password);
        
        assertNotNull(hash, "Hash with special characters should not be null");
        assertEquals(64, hash.length(), "Hash should be 64 characters long");
    }

}
