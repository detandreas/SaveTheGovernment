package budget.model.domain;

import budget.model.enums.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

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

        // Replace the AtomicInteger value with a new AtomicInteger(1)
        AtomicInteger newCounter = new AtomicInteger(1);
        idField.set(null, newCounter);
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

        assertEquals(1, pc.getId(), "First ID should be 1");
        assertEquals(BUDGET_ITEM_ID, pc.getBudgetItemId());
        assertEquals(REQUESTER_NAME, pc.getRequestByName());
        assertEquals(REQUESTER_ID, pc.getRequestById());
        assertEquals(OLD_VALUE, pc.getOldValue());
        assertEquals(NEW_VALUE, pc.getNewValue());
        assertEquals(Status.PENDING, pc.getStatus(), "Initial status must be PENDING");

        // Test submittedDate format
        assertDoesNotThrow(() ->
                LocalDateTime.parse(pc.getSubmittedDate(), DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                "submittedDate must be a valid ISO_LOCAL_DATE_TIME string"
        );
    }

    @Test
    void testAutoIncrementIds() {
        PendingChange pc1 = new PendingChange(BUDGET_ITEM_ID, REQUESTER_NAME, REQUESTER_ID, OLD_VALUE, NEW_VALUE);
        PendingChange pc2 = new PendingChange(BUDGET_ITEM_ID, REQUESTER_NAME, REQUESTER_ID, OLD_VALUE, NEW_VALUE);

        assertEquals(1, pc1.getId(), "First ID should be 1");
        assertEquals(2, pc2.getId(), "Second ID should be 2");
    }

    @Test
    void testApprove() {
        PendingChange pc = new PendingChange(BUDGET_ITEM_ID, REQUESTER_NAME, REQUESTER_ID, OLD_VALUE, NEW_VALUE);

        pc.approve();
        assertEquals(Status.APPROVED, pc.getStatus(), "Status should become APPROVED");
    }

    @Test
    void testReject() {
        PendingChange pc = new PendingChange(BUDGET_ITEM_ID, REQUESTER_NAME, REQUESTER_ID, OLD_VALUE, NEW_VALUE);

        pc.reject();
        assertEquals(Status.REJECTED, pc.getStatus(), "Status should become REJECTED");
    }

    @Test
    void testToStringContainsAllFields() {
        PendingChange pc = new PendingChange(BUDGET_ITEM_ID, REQUESTER_NAME, REQUESTER_ID, OLD_VALUE, NEW_VALUE);
        String out = pc.toString();

        assertTrue(out.contains("id=1"));
        assertTrue(out.contains("budgetItemId=" + BUDGET_ITEM_ID));
        assertTrue(out.contains("requestByName=" + REQUESTER_NAME));
        assertTrue(out.contains(String.format("oldValue=%.2f", OLD_VALUE)));
        assertTrue(out.contains(String.format("newValue=%.2f", NEW_VALUE)));
        assertTrue(out.contains("status=PENDING"));
        assertTrue(out.contains(pc.getSubmittedDate()));
    }
}
