package budget.backend.model.domain;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import budget.backend.model.enums.Status;

public class TestPendingChange {

    private static final int BUDGET_ITEM_ID = 10;
    private static final int BUDGET_ITEM_YEAR = 2024;
    private static final String BUDGET_ITEM_NAME = "Test Budget Item";
    private static final String REQUESTER_NAME = "USERNAME";
    private static final UUID REQUESTER_ID = UUID.randomUUID();
    private static final double OLD_VALUE = 500;
    private static final double NEW_VALUE = 1000;

    @Test
    void testConstructorAndGetters() {
        PendingChange pc = new PendingChange(
            1,
                BUDGET_ITEM_ID,
                BUDGET_ITEM_YEAR,
                BUDGET_ITEM_NAME,
                REQUESTER_NAME,
                REQUESTER_ID,
                OLD_VALUE,
                NEW_VALUE
        );

        assertEquals(1, pc.getId(), "Failure - wrong id");
        assertEquals(BUDGET_ITEM_ID, pc.getBudgetItemId(), "Failure - wrong budgetItemId");
        assertEquals(BUDGET_ITEM_YEAR, pc.getBudgetItemYear(), "Failure - wrong budgetItemYear");
        assertEquals(BUDGET_ITEM_NAME, pc.getBudgetItemName(), "Failure - wrong budgetItemName");
        assertEquals(REQUESTER_NAME, pc.getRequestByName(), "Failure - wrong requestByName");
        assertEquals(REQUESTER_ID, pc.getRequestById(), "Failure - wrong requestById");
        assertEquals(OLD_VALUE, pc.getOldValue(), "Failure - wrong oldValue");
        assertEquals(NEW_VALUE, pc.getNewValue(), "Failure - wrong newValue");
        assertEquals(Status.PENDING, pc.getStatus(), "Failure - wrong status");

        // Test submittedDate format
        assertDoesNotThrow(() ->
                LocalDateTime.parse(pc.getSubmittedDate(), DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                "Failure - submittedDate must be a valid ISO_LOCAL_DATE_TIME string"
        );
    }

    @Test
    void testAutoIncrementIds() {
        PendingChange pc1 = new PendingChange(1, BUDGET_ITEM_ID, BUDGET_ITEM_YEAR, BUDGET_ITEM_NAME, REQUESTER_NAME, REQUESTER_ID, OLD_VALUE, NEW_VALUE);
        PendingChange pc2 = new PendingChange(2, BUDGET_ITEM_ID, BUDGET_ITEM_YEAR, BUDGET_ITEM_NAME, REQUESTER_NAME, REQUESTER_ID, OLD_VALUE, NEW_VALUE);

        assertEquals(1, pc1.getId(), "Failure - wrong first id");
        assertEquals(2, pc2.getId(), "Failure - wrong second id");
    }

    @Test
    void testApprove() {
        PendingChange pc = new PendingChange(1, BUDGET_ITEM_ID, BUDGET_ITEM_YEAR, BUDGET_ITEM_NAME, REQUESTER_NAME, REQUESTER_ID, OLD_VALUE, NEW_VALUE);

        pc.approve();
        assertEquals(Status.APPROVED, pc.getStatus(), "Failure - wrong status after approve");
    }

    @Test
    void testReject() {
        PendingChange pc = new PendingChange(1, BUDGET_ITEM_ID, BUDGET_ITEM_YEAR, BUDGET_ITEM_NAME, REQUESTER_NAME, REQUESTER_ID, OLD_VALUE, NEW_VALUE);

        pc.reject();
        assertEquals(Status.REJECTED, pc.getStatus(), "Failure - wrong status after reject");
    }

    @Test
    void testToStringContainsAllFields() {
        PendingChange pc = new PendingChange(1, BUDGET_ITEM_ID, BUDGET_ITEM_YEAR, BUDGET_ITEM_NAME, REQUESTER_NAME, REQUESTER_ID, OLD_VALUE, NEW_VALUE);
        String out = pc.toString();

        assertTrue(out.contains("id=1"), "Failure - wrong toString");
        assertTrue(out.contains("budgetItemId=10"), "Failure - wrong toString");
        assertTrue(out.contains("budgetItemYear=2024"), "Failure - wrong toString");
        assertTrue(out.contains("budgetItemName=Test Budget Item"), "Failure - wrong toString");
        assertTrue(out.contains("requestByName=USERNAME"), "Failure - wrong toString");
        assertTrue(out.contains("requestById=" + REQUESTER_ID.toString()), "Failure - wrong toString");
        assertTrue(out.contains(String.format("oldValue=500.00")), "Failure - wrong toString");
        assertTrue(out.contains(String.format("newValue=1000.00")), "Failure - wrong toString");
        assertTrue(out.contains("status=Pending"), "Failure - wrong toString");
        assertTrue(out.contains(pc.getSubmittedDate()), "Failure - wrong toString");
    }
}
