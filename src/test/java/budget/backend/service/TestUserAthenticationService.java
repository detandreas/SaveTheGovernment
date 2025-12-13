package budget.backend.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import budget.backend.exceptions.UserNotAuthorizedException;
import budget.backend.exceptions.ValidationException;
import budget.backend.model.domain.user.GovernmentMember;
import budget.backend.model.enums.Ministry;
import budget.backend.model.enums.UserRole;
import budget.backend.repository.UserRepository;
import budget.backend.util.PasswordUtils;

class TestUserAthenticationService {

    private String originalDataDir;
    private Path usersJson;
    private UserAuthenticationService userAuthenticationService;
    UserRepository userRepository;
    private String username;  
    private String password; 
    private String fullName;
    private UserRole role;
    private Ministry ministry;
    private GovernmentMember gm;

    @BeforeEach
    void setUp(@TempDir Path tempDir) throws IOException {
        // backup and set data dir so PathsUtil resolves files from tempDir
        originalDataDir = System.getProperty("budget.data.dir");
        System.setProperty("budget.data.dir", tempDir.toString());

        usersJson = tempDir.resolve("users.json");
        // create minimal files (empty JSON structures) so repository.load doesn't
        // return early due to missing resources
        Files.writeString(usersJson, "{}", StandardCharsets.UTF_8);

        userRepository = new UserRepository();
        this.userAuthenticationService = new UserAuthenticationService(userRepository);
        this.username = "Cristopher";     
        this.password = "password123";
        this.fullName = "Cristopher Gogolos";
        this.role = UserRole.GOVERNMENT_MEMBER;
        this.ministry = Ministry.FINANCE;
    }

    @AfterEach
    void tearDown() {
        if (originalDataDir == null) {
            System.clearProperty("budget.data.dir");
        } else {
            System.setProperty("budget.data.dir", originalDataDir);
        }    
    }

    @Test
    //Test for a null password
    void  testLoginNullPassword() {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userAuthenticationService.login(username, null);
        }, "Failure - null password should throw ValidationException");
        assertEquals("Ο κωδικός πρόσβασης είναι υποχρεωτικός.", exception.getMessage(),
                    "Failure - exception message should match");
    }

    @Test
    //Test for a null username 
    void  testLoginNullUsername() {
        UserNotAuthorizedException exception = assertThrows(UserNotAuthorizedException.class, () -> {
            userAuthenticationService.login(null, password);
        }, "Failure - null username should throw UserNotAuthorizedException");
        assertEquals("Λάθος στοιχεία.", exception.getMessage(),
                    "Failure - exception message should match");
    }

    @Test
    //Test for valid username but invalid password
    void testLoginInvalidPassword() {
        userAuthenticationService.signUp(username, password, fullName, role, ministry);
        String password2 = "wrongpassword";

        UserNotAuthorizedException exception = assertThrows(UserNotAuthorizedException.class, () -> {
            userAuthenticationService.login(username, password2);
        }, "Failure - invalid password should throw UserNotAuthorizedException");
        assertEquals("Λάθος στοιχεία.", exception.getMessage(),
                    "Failure - exception message should match");
    }

    @Test
    //Test for valid username and valid password
    void testLogInvalidCredentials() {
        userAuthenticationService.signUp(username, password, fullName, role, ministry);
        userAuthenticationService.login(username, password);
        assertNotNull(userAuthenticationService.getCurrentUser(), 
                    "Failure - valid userName and valid Password should set current user");
    }

    @Test
    void testForInvalidHash() {
        gm = new GovernmentMember(username, fullName, password, ministry);
        userRepository.save(gm); // password is not hashed before stored -> this will throw IllegalArgumentException when parsing HEX
        UserNotAuthorizedException exception = assertThrows(UserNotAuthorizedException.class, () -> {
            userAuthenticationService.login(username, password);
        }, "Failure - invalid HEX should throw UserNotAuthorizedException");
        assertEquals("Λάθος στοιχεία.", exception.getMessage(),
                    "Failure - exception message should match");
    }

    @Test
    //Test for correct logout functionality
    void testLogout() {
        userAuthenticationService.signUp(username, password, fullName, role, ministry);
        userAuthenticationService.login(username, password);
        assertNotNull(userAuthenticationService.getCurrentUser(),
                                    "Failure - current user should not be null right after he logged in");
        userAuthenticationService.logout();
        assertNull(userAuthenticationService.getCurrentUser(),
                                    "Failure - current user should be null right after he logged out");
    }

    @Test
    void testUserIsAuthenticated() {
        userAuthenticationService.signUp(username, password, fullName, role, ministry);
        userAuthenticationService.login(username, password);
        assertNotNull(userAuthenticationService.getCurrentUser(),
                                    "Failure - current user should not be null right after he logged in");
        assertTrue(userAuthenticationService.isAuthenticated(),
                                    "Failure - logged in user should be authenticated");
        userAuthenticationService.logout();
        assertFalse(userAuthenticationService.isAuthenticated(),
                                    "Failure - logged out user shouldn't be authenticated");
    }

    //Tests for signup method

    @Test
    //Test for empty username during signup
    void testSignUpEmptyUsername() {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userAuthenticationService.signUp("", password, fullName, role, ministry);
        }, "Failure - empty userName should throw ValidationException");
        assertEquals("Το όνομα χρήστη είναι υποχρεωτικό.", exception.getMessage(),
                    "Failure - exception message should match");
    }

    @Test
    //Test for null username during signup
    void testSignUpNullUserName() {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userAuthenticationService.signUp(null, password, fullName, role, ministry);
        }, "Failure - null userName should throw ValidationException");
        assertEquals("Το όνομα χρήστη είναι υποχρεωτικό.", exception.getMessage(),
                    "Failure - exception message should match");
    }

    @Test
    //Test for null password during signup
    void testSignUpNullPassword() {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userAuthenticationService.signUp(username, null, fullName, role, ministry);
        }, "Failure - null password should throw ValidationException");
        assertEquals("Ο κωδικός πρόσβασης είναι υποχρεωτικός.", exception.getMessage(),
                    "Failure - exception message should match");
    }

    @Test
    //Test for empty password during signup
    void testSignUpEmptyPassword() {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userAuthenticationService.signUp(username, "", fullName, role, ministry);
        }, "Failure - empty password should throw ValidationException");
        assertEquals("Ο κωδικός πρόσβασης είναι υποχρεωτικός.", exception.getMessage(),
                    "Failure - exception message should match");
    }

    @Test
    //Test for empty full name during signup
    void testSignUpEmptyFullname() {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userAuthenticationService.signUp(username, password, "  ", role, ministry);
        }, "Failure - empty Full Name should throw ValidationException");
        assertEquals("Το πλήρες όνομα είναι υποχρεωτικό.", exception.getMessage(),
                    "Failure - exception message should match");
    }

    @Test
    //Test for null full name during signup
    void testSignUpNullFullName() {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userAuthenticationService.signUp(username, password, null, role, ministry);
        }, "Failure - null Full Name should throw ValidationException");
        assertEquals("Το πλήρες όνομα είναι υποχρεωτικό.", exception.getMessage(),
                    "Failure - exception message should match");
    }

    @Test
    //Test for null role during signup
    void testSignUpNullRole() {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userAuthenticationService.signUp(username, password, fullName, null, ministry);
        }, "Failure - null role should throw ValidationException");
        assertEquals("Ο ρόλος χρήστη είναι υποχρεωτικός.", exception.getMessage(),
                    "Failure - exception message should match");
    }

    @Test
    //Test for null ministry during signup
    void testSignUpNullMinistry() {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userAuthenticationService.signUp(username, password, fullName, role, null);
        }, "Failure - null ministry should throw ValidationException");
        assertEquals("Το υπουργείο είναι υποχρεωτικό για μέλη κυβέρνησης.", exception.getMessage(),
                    "Failure - exception message should match");
    }

    @Test
    //Test for taken username during signup
    void testSignUpTakenUsername() {
        String username2 = "andreas1";
        String pw = "test";
        userAuthenticationService.signUp(username2, pw, fullName, role, ministry);
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userAuthenticationService.signUp("andreas1", password, fullName, role, ministry);
        }, "Failure - signing up with already taken userName should throw ValidationException");
        assertEquals("Το όνομα χρήστη υπάρχει ήδη.", exception.getMessage(),
                    "Failure - exception message should match");
    }

    @Test
    //Test for corect password hashing during signup
    void testSignUpCorrectPasswordHashing() {
        userAuthenticationService.signUp(username, password, fullName, role, ministry);
        assertNotEquals(password, PasswordUtils.hashPassword(password), "Failure - hashing not working properly");

    }

    @Test
    //Test for unsucessfull signup (Prime Minister already exists)
    void testSignUpUnsucessfulPm() {
        userAuthenticationService.signUp(username, password, fullName, UserRole.PRIME_MINISTER, null);
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userAuthenticationService.signUp("newPM", "newpassword", "New PM", UserRole.PRIME_MINISTER, null);
        }, "Failure - only one Prime Minister allowed");
        assertEquals("Υπάρχει ήδη Πρωθυπουργός στο σύστημα.", exception.getMessage(),
                    "Failure - exception message should match"); 
    }

    @Test
    //Test for sucessfull signup (Citizen)
       void testSignUpSucessfulCitizen() {
           userAuthenticationService.logout();
           // If no exception is thrown, the signup was successful
           userAuthenticationService.signUp("newCitizen_1", password, fullName, UserRole.CITIZEN, null);
    }

    @Test
    //Test for sucessfull signup (Government Member)
       void testSignupSucessfulGovernmentMember() {
           // If no exception is thrown, the signup was successful
           userAuthenticationService.signUp(username, password, fullName, role, ministry);
    }

}
