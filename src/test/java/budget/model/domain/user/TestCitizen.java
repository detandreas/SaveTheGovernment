package budget.model.domain.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import budget.model.domain.BudgetItem;
import budget.model.enums.UserRole;

public class TestCitizen {
    private static Citizen citizen;
    private static Citizen citizen2;
    private static BudgetItem item;
    private static final String USER_NAME = "USER_NAME";
    private static final String FULL_NAME = "FULL_NAME";
    private static final String PASSWORD = "hashed_password";

    @BeforeAll
    public static void setUp() {    
        citizen = new Citizen(USER_NAME, FULL_NAME, PASSWORD);
        citizen = new Citizen(USER_NAME, FULL_NAME, PASSWORD);
    }

    @Test
    void TestConstructor(){
        assertEquals(USER_NAME, citizen.getUserName(), "Failure - wrong userName");
        assertEquals(FULL_NAME, citizen.getFullName(), "Failure - wrong fullName");
        // Citizen should store whatever value it receives
        assertEquals(PASSWORD, citizen.getHashPassword(), "Failure - wrong hashPassword");
        assertEquals(UserRole.CITIZEN, citizen.getUserRole(), "Failure - wrong userRole");
        assertNotNull(citizen.getId(), "Failure - Null id");
        assertNotEquals(citizen.getId(), citizen2.getId(), "Failure - same id");
    }

    @Test 
    void TestCanEdit(){
        assertFalse(citizen.canEdit(item), "Failure - Citizen  edit");
    }

    @Test 
    void TestCanApprove(){
        assertFalse(citizen.canApprove(), "Failure - Citizen cant approve");
    }

    @Test
    void TestCanSubmitChangeRequest() {
        assertFalse(citizen.canSubmitChangeRequest(), "Failure - Citizen  submit change request");
    }

    @Test
    void TestToString() {
        String s = citizen.toString();

        assertTrue(s.contains("userName=USER_NAME"), "Failure wrong toString");
        assertTrue(s.contains("fullName=FULL_NAME"), "Failure wrong toString");
        assertTrue(s.contains("userRole=Citizen"), "Failure wrong toString");
    }
}

