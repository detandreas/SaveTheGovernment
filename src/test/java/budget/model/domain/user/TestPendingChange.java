package budget.model.domain;

import budget.model.enums.Status;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TestPendingChange {

    private static final int BUDGET_ITEM_ID = 10;
    private static final String REQUESTER_NAME = "USERNAME";
    private static final UUID REQUESTER_ID = UUID.randomUUID();
    private static final double OLD_VALUE = 500;
    private static final double NEW_VALUE = 1000;

    @BeforeEach
    void resetIdCounter() throws Exception {
        Field idField = PendingChange.class.getDeclaredField("NEXT_ID");
        idField.setAccessible(true);
        
        // Παίρνουμε το υπάρχον AtomicInteger και θέτουμε την τιμή του σε 1
        AtomicInteger currentCounter = (AtomicInteger) idField.get(null);
        currentCounter.set(1);
    }

    @Test
    void testConstructorAndGetters() {
        PendingChange pc = new PendingChange(
                BUDGET_ITEM_ID,
                REQUESTER_NAME,
                REQUESTER_ID,
                OLD_VALUE,
                NEW_VALUE
        );

        assertEquals(1, pc.getId(), "Failure - wrong id");
        assertEquals(BUDGET_ITEM_ID, pc.getBudgetItemId(), "Failure - wrong budgetItemId");
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
        PendingChange pc1 = new PendingChange(BUDGET_ITEM_ID, REQUESTER_NAME, REQUESTER_ID, OLD_VALUE, NEW_VALUE);
        PendingChange pc2 = new PendingChange(BUDGET_ITEM_ID, REQUESTER_NAME, REQUESTER_ID, OLD_VALUE, NEW_VALUE);

        assertEquals(1, pc1.getId(), "Failure - wrong first id");
        assertEquals(2, pc2.getId(), "Failure - wrong second id");
    }

    @Test
    void testApprove() {
        PendingChange pc = new PendingChange(BUDGET_ITEM_ID, REQUESTER_NAME, REQUESTER_ID, OLD_VALUE, NEW_VALUE);

        pc.approve();
        assertEquals(Status.APPROVED, pc.getStatus(), "Failure - wrong status after approve");
    }

    @Test
    void testReject() {
        PendingChange pc = new PendingChange(BUDGET_ITEM_ID, REQUESTER_NAME, REQUESTER_ID, OLD_VALUE, NEW_VALUE);

        pc.reject();
        assertEquals(Status.REJECTED, pc.getStatus(), "Failure - wrong status after reject");
    }

    @Test
    void testToStringContainsAllFields() {
        PendingChange pc = new PendingChange(BUDGET_ITEM_ID, REQUESTER_NAME, REQUESTER_ID, OLD_VALUE, NEW_VALUE);
        String out = pc.toString();

        assertTrue(out.contains("id=1"), "Failure - wrong toString");
        assertTrue(out.contains("budgetItemId=10"), "Failure - wrong toString");
        assertTrue(out.contains("requestByName=USERNAME"), "Failure - wrong toString");
        assertTrue(out.contains(String.format("oldValue=500.00")), "Failure - wrong toString");
        assertTrue(out.contains(String.format("newValue=1000.00")), "Failure - wrong toString");
        assertTrue(out.contains("status=Change approval is still pending"), "Failure - wrong toString");
        assertTrue(out.contains(pc.getSubmittedDate()), "Failure - wrong toString");
    }
}
