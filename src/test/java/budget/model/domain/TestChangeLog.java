package budget.model.domain;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestChangeLog {

    private static ChangeLog changeLog;

    // Test data
    private static final int ID = 1;
    private static final int BUDGET_ITEM_ID = 10;
    private static final double OLD_VALUE = 750.0;
    private static final double NEW_VALUE = 1000.0;
    private static final String SUBMITTED_DATE = "25-11-2025 12:00";
    private static final String ACTOR_USERNAME = "User";
    private static final UUID ACTOR_ID = UUID.randomUUID();

    @BeforeAll
    static void setUp() {
        changeLog = new ChangeLog(
                ID,
                BUDGET_ITEM_ID,
                OLD_VALUE,
                NEW_VALUE,
                SUBMITTED_DATE,
                ACTOR_USERNAME,
                ACTOR_ID
        );
    }

    // TEST CONSTRUCTOR & GETTERS
    @Test
    void testConstructorAndGetters() {
        assertEquals(ID, changeLog.id(), "Failure - wrong id");
        assertEquals(BUDGET_ITEM_ID, changeLog.budgetItemId(), "Failure - wrong budgetItemId");
        assertEquals(OLD_VALUE, changeLog.oldValue(), "Failure - wrong oldValue");
        assertEquals(NEW_VALUE, changeLog.newValue(), "Failure - wrong newValue");
        assertEquals(SUBMITTED_DATE, changeLog.submittedDate(), "Failure - wrong submittedDate");
        assertEquals(ACTOR_USERNAME, changeLog.actorUserName(), "Failure - wrong actorUserName");
        assertEquals(ACTOR_ID, changeLog.actorId(), "Failure - wrong actorId");
    }

    // TEST IMMUTABILITY
    @Test
    void testRecordIsImmutable() {
        ChangeLog modified = new ChangeLog(
                ID,
                BUDGET_ITEM_ID,
                OLD_VALUE,
                2000.0,
                SUBMITTED_DATE,
                ACTOR_USERNAME,
                ACTOR_ID
        );

        assertNotEquals(changeLog, modified, "Failure - record should be immutable");
    }

    // TEST toString()
    @Test
    void testToString() {
        String s = changeLog.toString();

        assertTrue(s.contains("id=1"), "Failure - toString missing id");
        assertTrue(s.contains("budgetItemId=10"), "Failure - toString missing budgetItemId");
        assertTrue(s.contains("oldValue=750.0"), "Failure - toString missing oldValue");
        assertTrue(s.contains("newValue=1000.0"), "Failure - toString missing newValue");
        assertTrue(s.contains("submittedDate=25-11-2025 12:00"), "Failure - toString missing submittedDate");
        assertTrue(s.contains("actorUserName=User"), "Failure - toString missing actorUserName");
        assertTrue(s.contains("actorId=" + changeLog.actorId()), "Failure - toString missing actorId");
    }
}
