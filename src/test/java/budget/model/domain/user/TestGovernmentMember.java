package budget.model.domain.user;

import budget.model.enums.Ministry;
import budget.model.domain.BudgetItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class TestGovernmentMember {
    private static GovernmentMember gm;
    private static final String USER_NAME = "TestUser";
    private static final String FULL_NAME = "Test Full Name";
    // is aldready hashed from service
    private static final String PASSWORD = "TestPassword";
    private static final Ministry MINISTRY = Ministry.FINANCE;
    private static BudgetItem budgetItem;

    @BeforeEach
    void setUp() {
        gm = new GovernmentMember(USER_NAME, FULL_NAME, PASSWORD, MINISTRY);
    }

    @Test
    void testConstructor() {
        assertEquals(MINISTRY, gm.getMinistry(), "Failure - wrong ministry");
        // the other fields are handled by the User superclass
        // no reason to test again here
    }

    // TESTS FOR GETTER
    @Test
    void testGetMinistry() {
        assertEquals(MINISTRY, gm.getMinistry(), "Failure - wrong ministry");
    }

    //TESTS FOR SETTER
    @Test
    void testSetMinistry() {
        Ministry newMinistry = Ministry.AGRICULTURE;
        gm.setMinistry(newMinistry);
        assertEquals(newMinistry, gm.getMinistry(), "Failure - ministry not updated");
    }

    @Test
    void testCanEdit() {
        assertTrue(gm.canEdit(budgetItem), "Failure - member of Ministry.FINANCE should edit");
        Ministry newMinistry = Ministry.AGRICULTURE;
        gm.setMinistry(newMinistry);
        assertFalse(gm.canEdit(budgetItem), "Failure - only member of Ministry.FINANCE can approve");
    }

    @Test
    void testCanApprove() {
        assertFalse(gm.canApprove(), "Failure - GovernmentMember shouldn't approve");
    }

    @Test
    void testCanSubmitChangeRequest() {
        assertTrue(gm.canSubmitChangeRequest(), "Failure - GovernmentMember should request changes");
    }

    @Test
    void testToString() {
        String s = gm.toString();
        assertTrue(s.contains("userName=TestUser"), "Failure - wrong toString");
        assertTrue(s.contains("fullName=Test Full Name"), "Failure - wrong toString");
        assertTrue(s.contains("userRole=Government Member"), "Failure - wrong toString");
        assertTrue(s.contains("ministry=Finance"), "Failure - wrong toString");
    }
}
