package budget.service;

import java.io.IOException;

import budget.util.PasswordUtils;
import budget.repository.UserRepository;
import budget.model.enums.UserRole;
import budget.model.enums.Ministry;
import budget.model.domain.user.GovernmentMember;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

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
    //Test for a null password /
    void  testLoginNullPassword() {
        boolean user = userAuthenticationService.login(username, null);
        assertFalse(user, "Failure - null password should return false");
    }

    @Test
    //Test for a null username 
    void  testLoginNullUsername() {
        boolean user = userAuthenticationService.login(null, password);
        assertFalse(user, "Failure - null username should return false");
    }

    @Test
    //Test for valid username but invalid password
    void testLoginInvalidPassword() {
        userAuthenticationService.signUp(username, password, fullName, role, ministry);
        String password2 = "wrongpassword";

        boolean user = userAuthenticationService.login(username, password2);
        assertFalse(user, "Failure - invalid password should return false");
    }

    @Test
    //Test for valid username and valid password
    void testLogInvalidCredentials() {
        userAuthenticationService.signUp(username, password, fullName, role, ministry);
        boolean user = userAuthenticationService.login(username, password);
        assertTrue(user, "Failure - valid userName and valid Password should return true");
    }

    @Test
    void testForInvalidHash() {
        gm = new GovernmentMember(username, fullName, password, ministry);
        userRepository.save(gm); // password is not hashed before stored -> this will throw IllegalArgumentException when parsing HEX
        assertFalse(userAuthenticationService.login(username, password),
                                    "Failure - invalid HEX should return false");
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
        boolean result = userAuthenticationService.signUp("", password, fullName, role, ministry);
        assertFalse(result, "Failure - empty userName should return false");
    }

    @Test
    //Test for null username during signup
    void testSignUpNullUserName() {
        boolean result = userAuthenticationService.signUp(null, password, fullName, role, ministry);
        assertFalse(result, "Failure - null userName should return false");
    }

    @Test
    //Test for null password during signup
    void testSignUpNullPassword() {
        boolean result = userAuthenticationService.signUp(username, null, fullName, role, ministry);
        assertFalse(result, "Failure - null password should return false");
    }

    @Test
    //Test for empty password during signup
    void testSignUpEmptyPassword() {
        boolean result = userAuthenticationService.signUp(username, "", fullName, role, ministry);
        assertFalse(result, "Failure - empty password should return false");
    }

    @Test
    //Test for empty full name during signup
    void testSignUpEmptyFullname() {
        boolean result = userAuthenticationService.signUp(username, password, "  ", role, ministry);
        assertFalse(result, "Failure - empty Full Name should return false");
    }

    @Test
    //Test for null full name during signup
    void testSignUpNullFullName() {
        boolean result = userAuthenticationService.signUp(username, password, null, role, ministry);
        assertFalse(result, "Failure - null Full Name should return false");
    }

    @Test
    //Test for null role during signup
    void testSignUpNullRole() {
        boolean result = userAuthenticationService.signUp(username, password, fullName, null, ministry);
        assertFalse(result, "Failure - null role should return false");
    }

    @Test
    //Test for null ministry during signup
    void testSignUpNullMinistry() {
        boolean result = userAuthenticationService.signUp(username, password, fullName, role, null);
        assertFalse(result, "Failure - null ministry should return false");
    }

    @Test
    //Test for taken username during signup
    void testSignUpTakenUsername() {
        String username2 = "andreas1";
        String pw = "test";
        userAuthenticationService.signUp(username2, pw, fullName, role, ministry);
        boolean Signup = userAuthenticationService.signUp("andreas1", password, fullName, role, ministry);
        assertFalse(Signup, "Failure - signing up with already taken userName should return false");
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
        boolean Signup = userAuthenticationService.signUp("newPM", "newpassword", "New PM", UserRole.PRIME_MINISTER, null);
        assertFalse(Signup, "Failure - only one Prime Minister allowed"); 
    }

    @Test
    //Test for sucessfull signup (Citizen)
       void testSignUpSucessfulCitizen() {
           userAuthenticationService.logout();
           boolean Signup = userAuthenticationService.signUp("newCitizen_1", password, fullName, UserRole.CITIZEN, null);
           assertTrue(Signup, "Failure - valid sign up should return true"); 
    }

    @Test
    //Test for sucessfull signup (Government Member)
       void testSignupSucessfulGovernmentMember() {
           boolean Signup = userAuthenticationService.signUp(username, password, fullName, role, ministry);
           assertTrue(Signup, "Failure - valid sign up should return true"); 
    }

}
