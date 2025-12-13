package budget.backend.model.domain.user;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import budget.backend.model.enums.UserRole;

/**
 * Tests for inherited methods from abstract User class.
 * Uses Citizen as a concrete implementation for testing.
 */
public class TestUser {
    private static User user1;
    private static User user2;
    private static final String USER_NAME = "TestUser";
    private static final String FULL_NAME = "Test Full Name";
    // is aldready hashed from service
    private static final String PASSWORD = "TestPassowrd";

    @BeforeAll
    static void setUp() {
        user1 = new Citizen(USER_NAME, FULL_NAME, PASSWORD);
        user2 = new Citizen(USER_NAME, FULL_NAME, PASSWORD);
    }

    @Test
    void testConstructor(){
        assertEquals(USER_NAME, user1.getUserName(), "Failure - wrong userName");
        assertEquals(FULL_NAME, user1.getFullName(), "Failure - wrong fullName");
        // User constructor should store whatever value it receives as password
        assertEquals(PASSWORD, user1.getHashPassword(), "Failure - wrong hashPassword");
        assertEquals(UserRole.CITIZEN, user1.getUserRole(), "Failure - wrong userRole");
        assertNotNull(user1.getId(), "Failure - Null id");
        // id should be unique for each user
        assertNotEquals(user2.getId(), user1.getId(), "Failure - same id");
    }

    // TESTS FOR GETTERS
    @Test
    void testGetId() {
        assertNotNull(user1.getId(), "Failure - Null id");
        assertInstanceOf(UUID.class, user1.getId(), "Failure - id should be UUID");
    }

    @Test
    void testGetUserName() {
        assertEquals(USER_NAME, user1.getUserName(), "Failure - wrong UserName");
    }

    @Test
    void testGetFullName() {
        assertEquals(FULL_NAME, user1.getFullName(), "Failure - wrong FullName");
    }

    @Test
    void testGetHashedPassword() {
        assertEquals(PASSWORD, user1.getHashPassword());
    }

    @Test 
    void testGetUserRole() {
        assertEquals(UserRole.CITIZEN, user1.getUserRole(), "Failure - wrong UserRole");
    }

    // TESTS FOR SETTERS
    @Test
    void testSetUserName() {
        String NEW_USER_NAME = "NewUserName";
        user2.setUserName(NEW_USER_NAME);
        assertEquals(NEW_USER_NAME, user2.getUserName(), "Failure - userName not updated");
    }

    @Test
    void testSetFullName() {
        String NEW_FULL_NAME = "New Full Name";
        user2.setFullName(NEW_FULL_NAME);
        assertEquals(NEW_FULL_NAME, user2.getFullName(), "Failure - fullName not updated");
    }

    @Test
    void testSetPassword() {
        //hashed by some service
        String NEW_PASSWORD = "NewPassword";
        user2.setPassword(NEW_PASSWORD);
        assertEquals(NEW_PASSWORD, user2.getHashPassword(), "Failure - password not updated");
    }

    @Test
    void testToString() {
        String s = user1.toString();
        assertTrue(s.contains("userName=TestUser"), "Failure - wrong toString");
        assertTrue(s.contains("fullName=Test Full Name"), "Failure - wrong toString");
        assertTrue(s.contains("userRole=Citizen"), "Failure - wrong toString");
    }
}
