package budget.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertFalse;

import budget.util.PasswordUtils;
import budget.repository.UserRepository;
import budget.model.enums.UserRole;
import budget.model.enums.Ministry;

class TestUserAthenticationService {

    private UserAuthenticationService userAuthenticationService;
    private String username;  
    private String password; 
    private String fullName;
    private UserRole role;
    private Ministry ministry;

    @BeforeEach
    void setUp() {
        UserRepository userRepository = new UserRepository();
        this.userAuthenticationService = new UserAuthenticationService(userRepository);
        this.username = "Cristopher";     
        this.password = "password123";
        this.fullName = "Cristopher Gogolos";
        this.role = UserRole.GOVERNMENT_MEMBER;
        this.ministry = Ministry.FINANCE;


    }

    @AfterEach
void tearDown() {
    UserRepository userRepository = new UserRepository();
    
    // Delete test users by username
    userRepository.findByUsername("Cristopher").ifPresent(userRepository::delete);
    userRepository.findByUsername("newCitizen_1").ifPresent(userRepository::delete);
    userRepository.findByUsername("newCitizen_2").ifPresent(userRepository::delete);
    userRepository.findByUsername("newPM").ifPresent(userRepository::delete);
}

    @Test
    //Test for a null password /

    void  testloginnullpassword() {
        String password = null;
boolean user = userAuthenticationService.login(username, password);
        assertFalse(user);
    }

@Test
//Test for a null username 

    void  testloginnullusername() {
        String username = null;

boolean user = userAuthenticationService.login(username, password);
        assertFalse(user);
    }

    @Test
    //Test for valid username but invalid password

    void testlogininvalidpassword() {
        String password = "wrongpassword";

        boolean user = userAuthenticationService.login(username, password);
        assertNotNull(username);
        assertFalse(user);
    }

    @Test
    //Test for valid username and valid password

    void testloginvalidcredentials() {

        userAuthenticationService.signUp(username, password, fullName, role, ministry);
        boolean user = userAuthenticationService.login(username, password);
        assertTrue(user);
    }

    @Test
    //Test for correct logout functionality

    void testlogout() {

        userAuthenticationService.signUp(username, password, fullName, role, ministry);
        userAuthenticationService.login(username, password);
        assertNotNull(userAuthenticationService.getCurrentUser());
        userAuthenticationService.logout();
        assertNull(userAuthenticationService.getCurrentUser());
    }

    //Tests for signup method


    @Test
    //Test for empty username during signup

    void testsignupemptyusername() {
        String username = "";

        boolean result = userAuthenticationService.signUp(username, password, fullName, role, ministry);
        assertFalse(result);
    }

    @Test
    //Test for null password during signup

    void testsignupnullpassword() {
        String password = null;

        boolean result = userAuthenticationService.signUp(username, password, fullName, role, ministry);
        assertFalse(result);
    }

    @Test
    //Test for empty full name during signup

    void testsignupemptyfullname() {
        String fullName = "   ";

        boolean result = userAuthenticationService.signUp(username, password, fullName, role, ministry);
        assertFalse(result);
    }

    @Test
    //Test for null role during signup

    void testsignupnullrole() {
        UserRole role = null;

        boolean result = userAuthenticationService.signUp(username, password, fullName, role, ministry);
        assertFalse(result);
    }

    @Test
    //Test for null ministry during signup

    void testsignupnullministry() {
        Ministry ministry = null;

        boolean result = userAuthenticationService.signUp(username, password, fullName, role, ministry);
        assertFalse(result);
    }

    @Test
    //Test for taken username during signup

    void testsignuptakenusername() {
      String username = "andreas1";

        boolean Signup = userAuthenticationService.signUp(username, password, fullName, role, ministry);
        assertFalse(Signup);
    }

    @Test
    //Test for corect password hashing during signup

    void testsignupcorrectpasswordhashing() {

        userAuthenticationService.signUp(username, password, fullName, role, ministry);
        assertNotEquals(password, PasswordUtils.hashPassword(password));

    }

       @Test
       //Test for sucessfull signup (Prime Minister)

       void testsignupsucessfulpm() {
    
           UserRole role = UserRole.PRIME_MINISTER;

           userAuthenticationService.logout();
           boolean Signup = userAuthenticationService.signUp(username, password, fullName, role, null);
           assertTrue(Signup); 
       }

       @Test
         //Test for unsucessfull signup (Prime Minister already exists)

         void testsignupunsucessfulpm() {
             UserRole role = UserRole.PRIME_MINISTER;
      
             userAuthenticationService.signUp(username, password, fullName, role, null);
             boolean Signup = userAuthenticationService.signUp("newPM", "newpassword", "New PM", role, null);
             assertFalse(Signup); 
         }

         @Test
         //Test for sucessfull signup (Citizen)

            void testsignupsucessfulcitizen() {
                UserRole role = UserRole.CITIZEN;
                String username = "newCitizen_1";

                userAuthenticationService.logout();
                boolean Signup = userAuthenticationService.signUp(username, password, fullName, role, null);
                assertTrue(Signup); 
            }

        @Test
        //Test for sucessfull signup (Government Member)

           void testsignupsucessfulgovernmentmember() {


               boolean Signup = userAuthenticationService.signUp(username, password, fullName, role, ministry);
               assertTrue(Signup); 
           }


}