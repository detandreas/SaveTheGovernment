package budget.model.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class TestMinistry {

    @Nested
    class TestEnumValues {

        @Test
        void allMinistriesAreDefined() {
            Ministry[] ministries = Ministry.values();

            assertNotNull(ministries, "Failure - values() returned null");
            assertTrue(ministries.length > 0, "Failure - no ministries defined");
        }

        @Test
        void enumContainsExpectedMinistries() {
            Ministry[] ministries = Ministry.values();

            assertTrue(arrayContains(ministries, Ministry.HEALTH), "Failure - HEALTH not present");
            assertTrue(arrayContains(ministries, Ministry.EDUCATION), "Failure - EDUCATION not present");
            assertTrue(arrayContains(ministries, Ministry.DEFENSE), "Failure - DEFENSE not present");
            assertTrue(arrayContains(ministries, Ministry.FINANCE), "Failure - FINANCE not present");
        }

        // Βοηθητική μέθοδος για έλεγχο ύπαρξης στοιχείου στο array
        private boolean arrayContains(Ministry[] array, Ministry ministry) {
            for (Ministry m : array) {
                if (m == ministry) {
                    return true;
                }
            }
            return false;
        }
    }

    @Nested
    class TestGetDisplayName {

        @Test
        void displayNameMatchesExpected() {
            assertEquals("Health", Ministry.HEALTH.getDisplayName(), "Failure - HEALTH displayName incorrect");
            assertEquals("Education", Ministry.EDUCATION.getDisplayName(), "Failure - EDUCATION displayName incorrect");
            assertEquals("Defense", Ministry.DEFENSE.getDisplayName(), "Failure - DEFENSE displayName incorrect");
        }
    }

    @Nested
    class TestToString {

        @Test
        void toStringReturnsDisplayName() {
            assertEquals("Finance", Ministry.FINANCE.toString(), "Failure - toString() incorrect for FINANCE");
            assertEquals("Infrastructure", Ministry.INFRASTRUCTURE.toString(), "Failure - toString() incorrect for INFRASTRUCTURE");
        }
    }
}
