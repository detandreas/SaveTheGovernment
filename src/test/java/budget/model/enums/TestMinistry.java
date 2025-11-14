package budget.model.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class TestMinistry {

    @Nested
    class TestEnumValues {
        @Test
        void allMinistriesAreDefined() {
            Ministry[] ministries = Ministry.values();
            assertEquals(11, ministries.length, "Should have exactly 11 ministries");
        }

        @Test
        void enumContainsAllExpectedMinistries() {
            Set<Ministry> expectedMinistries = Set.of(
                Ministry.HEALTH, Ministry.EDUCATION, Ministry.DEFENSE,
                Ministry.FINANCE, Ministry.INFRASTRUCTURE, Ministry.FOREIGN_AFFAIRS,
                Ministry.INTERIOR, Ministry.DEVELOPMENT, Ministry.LABOUR,
                Ministry.JUSTICE, Ministry.AGRICULTURE
            );
            Set<Ministry> actualMinistries = Set.of(Ministry.values());
            assertEquals(expectedMinistries, actualMinistries);
        }
    }

    @Nested
    class TestGetDisplayName {
        @Test
        void allDisplayNamesMatchExpected() {
            Map<Ministry, String> expectedDisplayNames = Map.ofEntries(
                Map.entry(Ministry.HEALTH, "Health"),
                Map.entry(Ministry.EDUCATION, "Education"),
                Map.entry(Ministry.DEFENSE, "Defense"),
                Map.entry(Ministry.FINANCE, "Finance"),
                Map.entry(Ministry.INFRASTRUCTURE, "Infrastructure"),
                Map.entry(Ministry.FOREIGN_AFFAIRS, "Foreign Affairs"),
                Map.entry(Ministry.INTERIOR, "Interior"),
                Map.entry(Ministry.DEVELOPMENT, "Development"),
                Map.entry(Ministry.LABOUR, "Labour"),
                Map.entry(Ministry.JUSTICE, "Justice"),
                Map.entry(Ministry.AGRICULTURE, "Agriculture")
            );

            for (Ministry ministry : Ministry.values()) {
                assertEquals(expectedDisplayNames.get(ministry), 
                            ministry.getDisplayName(),
                            "Display name mismatch for " + ministry.name());
            }
        }
    }

    @Nested
    class TestToString {
        @Test
        void toStringReturnsDisplayNameForAll() {
            for (Ministry ministry : Ministry.values()) {
                assertEquals(ministry.getDisplayName(), ministry.toString(),
                            "toString() should return displayName for " + ministry.name());
            }
        }
    }
}
