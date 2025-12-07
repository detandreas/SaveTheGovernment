package budget.service;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import budget.exceptions.UserNotAuthorizedException;
import budget.model.domain.BudgetItem;
import budget.model.domain.user.Citizen;
import budget.model.domain.user.GovernmentMember;
import budget.model.domain.user.PrimeMinister;
import budget.model.domain.user.User;
import budget.model.enums.Ministry;

public class TestUserAuthorizationService {

    private final UserAuthorizationService service = new UserAuthorizationService();
    private static PrimeMinister pm;
    private static GovernmentMember gm;
    private static BudgetItem item;

    @BeforeAll
    static void setUp() {
        pm = PrimeMinister.getInstance("pm", "Prime Minister", "Pass123!");
        gm = new GovernmentMember("username", "Full Name", "Pass123!", Ministry.FINANCE);
        item = new BudgetItem(1, 2025, "Item", 1000, false, List.of(Ministry.FINANCE));
    }

    // TESTING checkCanUserSubmitRequests
    @Test
    void testNullUser(){
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
          () -> service.checkCanUserSubmitRequest(null, item));

        assertEquals("User cannot be null.",ex.getMessage(),
                "Failure - exception message should indicate user cannot be null");
    }

    @Test
    void testNullItem(){
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> service.checkCanUserSubmitRequest(gm, null));

        assertEquals("Budget item cannot be null.",ex.getMessage(),
                "Failure - exception message should indicate budget item cannot be null");
    }

    @Test
    void testNoMinistries(){
        BudgetItem wrong_item = new BudgetItem(1, 2025, "Item", 1000, false,List.of());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,() 
            -> service.checkCanUserSubmitRequest(gm, wrong_item));

        assertEquals("Budget item must be associated with at least one ministry.",ex.getMessage(),
                "Failure - exception message should indicate budget item must have at least one ministry");
    }

    @Test
    void testNotGovernmentMember() {
        UserNotAuthorizedException ex = assertThrows(UserNotAuthorizedException.class,
                () -> service.checkCanUserSubmitRequest(pm, item));

        assertEquals("Only government members can submit change requests.", ex.getMessage(),
                "Failure - exception message should indicate only government members can submit requests");
    }

    @Test
    void testWrongMinistry() {
        User wrong_gm = new GovernmentMember("u", "Full Name", "Pass123!", Ministry.HEALTH);

        UserNotAuthorizedException ex = assertThrows(UserNotAuthorizedException.class,
                () -> service.checkCanUserSubmitRequest(wrong_gm, item));

        assertTrue(ex.getMessage().contains("is not authorized to submit change requests"),
                "Failure - exception message should contain authorization error");
    }

    // Testing canUserSubmitRequests

    @Test 
    void testSubmitRequestValid() {
        assertDoesNotThrow(() -> service.canUserSubmitRequest(gm, item),
                "Failure - valid government member should be able to submit request without exception");
    }

    @Test 
    void testSubmitNullUser() {
        assertFalse(service.canUserSubmitRequest(null, item),
                "Failure - method should return false when user is null");
    }

    @Test 
    void testCheckCanUserApproveRequestsWithNullUser() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                            () -> service.checkCanUserApproveRequests(null));
    
        assertEquals("User cannot be null", ex.getMessage(),
                "Failure - exception message should indicate user cannot be null");
    }
    // Testing CanUserApproveRequests

    @Test
    void testApproveRequestsTrue(){
        assertTrue(service.canUserApproveRequests(pm),
                "Failure - Prime Minister should be able to approve requests");
    }

    @Test
    void testApproveRequestsFlase(){
        assertFalse(service.canUserApproveRequests(gm),
                "Failure - Government Member should not be able to approve requests");
    }

    // Testing checkCanUserEditBudgetItem

    @Test
    void testUserNull() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,() 
            -> service.checkCanUserEditBudgetItem(null, item));

        assertEquals("User cannot be null.",ex.getMessage(),
                "Failure - exception message should indicate user cannot be null");
    }

    @Test
    void testItemNull(){
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,() 
            -> service.checkCanUserEditBudgetItem(gm,null));

        assertEquals("Budget item cannot be null.",ex.getMessage(),
                "Failure - exception message should indicate budget item cannot be null");
    }

    @Test
    void test_NotGovernmentMember(){
        User ct = new Citizen("userName", "Full Name", "Pass123!");

        UserNotAuthorizedException ex = assertThrows(UserNotAuthorizedException.class,()
             -> service.checkCanUserEditBudgetItem(ct,item));

        assertEquals("Only government members can edit budget items.",ex.getMessage(),
                "Failure - exception message should indicate only government members can edit budget items");
    }

    @Test
    void testNotMinistry(){
        User wrong_gm = new GovernmentMember("u", "Full Name", "Pass123!", Ministry.HEALTH);

        UserNotAuthorizedException ex = assertThrows(UserNotAuthorizedException.class,()
             -> service.checkCanUserEditBudgetItem(wrong_gm,item));

        assertEquals("Only members of the Finance Ministry can directly edit this budget item.",ex.getMessage(),
                "Failure - exception message should indicate only Finance Ministry members can edit this item");
    }

    @Test 
    void testEditBudgetValid(){
        assertDoesNotThrow(() -> service.checkCanUserEditBudgetItem(gm, item),
                "Failure - valid government member should be able to edit budget item without exception");
    }

    // Testing canUserEditBudgetItem

    @Test
    void testCanditTrue(){
        assertTrue(service.canUserEditBudgetItem(gm, item),
                "Failure - government member from Finance Ministry should be able to edit budget item");
    }

    @Test
    void testCanEditFalsse(){
        User wrong_gm = new GovernmentMember("u", "Full Name", "Pass123!", Ministry.HEALTH);

        assertFalse(service.canUserEditBudgetItem(wrong_gm, item),
                "Failure - government member from different ministry should not be able to edit budget item");
    }

}