package budget.backend.model.enums;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TestUserRole {
    private static List<UserRole> enums;
    private static final String CITIZEN_DISPLAY_NAME = "Citizen";
    private static final String CITIZEN_DESCRIPTION = "Regular citizen with view-only access";
    private static final String GOVERNMENT_MEMBER_DISPLAY_NAME = "Government Member";
    private static final String GOVERNMENT_MEMBER_DESCRIPTION = "Government official who can propose changes";
    private static final String PRIME_MINISTER_DISPLAY_NAME = "Prime Minister";
    private static final String PRIME_MINISTER_DESCRIPTION = "Highest Authority accepts/declines proposed changes";
    private static List<List<String>> strings;

    @BeforeAll
    static void setUp() {
        enums = new ArrayList<>(List.of(
                    UserRole.CITIZEN,
                    UserRole.GOVERNMENT_MEMBER,
                    UserRole.PRIME_MINISTER
                    ));
        strings = new ArrayList<>(List.of(
                    List.of(CITIZEN_DISPLAY_NAME, CITIZEN_DESCRIPTION),
                    List.of(GOVERNMENT_MEMBER_DISPLAY_NAME, GOVERNMENT_MEMBER_DESCRIPTION),
                    List.of(PRIME_MINISTER_DISPLAY_NAME, PRIME_MINISTER_DESCRIPTION)
                    ));
    }

    @Test
    void testGetDisplayName() {
        for (int i = 0; i < enums.size(); i++) {
            assertEquals(strings.get(i).get(0), enums.get(i).getDisplayName(),
                        "Failure - wrong displayName");
        }
    }

    @Test
    void testGetDescription() {
        for (int i = 0; i < enums.size(); i++) {
            assertEquals(strings.get(i).get(1), enums.get(i).getDescription(),
                        "Failure - wrong description");
        }
    }

    @Test
    void testToString() {
        for (int i = 0; i < enums.size(); i++) {
            assertEquals(strings.get(i).get(0), enums.get(i).toString(),
                        "Failure - wrong toString");
        }
    }
}
