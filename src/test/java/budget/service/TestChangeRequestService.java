package budget.service;

import java.util.PrimitiveIterator;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import budget.constants.Message;
import budget.model.domain.BudgetItem;
import budget.model.domain.PendingChange;
import budget.model.domain.user.GovernmentMember;
import budget.model.domain.user.PrimeMinister;
import budget.model.domain.user.User;
import budget.model.enums.Ministry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestChangeRequestService{
    private static ChangeRequestService service;

    @BeforeAll
    static void setUp() {
        service = new ChangeRequestService(null, null, null, null, null, null );
        PrimeMinister.getInstance("UserName", "FULL NAME", "Pass123!");
    }

    // Testing SubmitChangeRequest

    @Test
    void ItemNull(){
        User user = new GovernmentMember("UserName", "FULL NAME", "Pass123!", Ministry.FINANCE);

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,()
             -> service.submitChangeRequest(user, null, 2000));
        
        assertEquals("Null BudgetItem", ex.getMessage());
    }

    // Testing ApproveRequest

    @Test
    void NullCahange() {
        PrimeMinister pm = PrimeMinister.getInstance();

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> service.approveRequest(pm, null));
        
        assertEquals(Message.REQUEST_DOES_NOT_EXIST_MESSAGE, ex.getMessage());
    }

    @Test
    void NullPrimeMinister() {
        User requester = new GovernmentMember("UserName", "FULL NAME", "Pass123!", Ministry.FINANCE);
        BudgetItem item = new BudgetItem(
            1, 2025, "Item", 1000.0, false,
            java.util.List.of(Ministry.FINANCE));

        PendingChange change = new PendingChange(
            item.getId(),
            item.getYear(),
            item.getName(),
            requester.getFullName(),
            requester.getId(),
            item.getValue(),
            2000.0);

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> service.approveRequest(null, change));

        assertEquals("Prime Minister cannot approve requests.", ex.getMessage());
    }

    // Testing rejectRequest

    @Test
    void NullChange() {
        PrimeMinister pm = PrimeMinister.getInstance();

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> service.rejectRequest(pm, null));

        assertEquals(Message.REQUEST_DOES_NOT_EXIST_MESSAGE, ex.getMessage());
    }

    @Test
    void Null_PrimeMinister() {
        User requester = new GovernmentMember("USER NAME", "FULL NAME", "Pass123!", Ministry.FINANCE);
        BudgetItem item = new BudgetItem(
            1, 2025, "Item", 1000.0, false,java.util.List.of(Ministry.FINANCE));

        PendingChange change = new PendingChange(
            item.getId(),
            item.getYear(),
            item.getName(),
            requester.getFullName(),
            requester.getId(),
            item.getValue(),
            2000.0);

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> service.rejectRequest(null, change));

        assertEquals("Prime Minister cannot approve requests.", ex.getMessage());
    }

    
}