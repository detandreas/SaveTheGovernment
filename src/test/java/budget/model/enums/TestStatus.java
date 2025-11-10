package budget.model.enums;

import java.util.List;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class TestStatus {
    private static List<Status> enums;
    private static final String PENDING_STRING = "Change approval is still pending";
    private static final String APPROVED_STRING = "Change is approved";
    private static final String REJECTED_STRING = "Change is rejected";
    private static List<String> strings;

    @BeforeAll
    static void setUp() {
        enums = new ArrayList<>(List.of(
                    Status.PENDING,
                    Status.APPROVED,
                    Status.REJECTED
                    ));
        strings = new ArrayList<>(List.of(
                    PENDING_STRING,
                    APPROVED_STRING,
                    REJECTED_STRING
                    ));
    }

    @Test
    void testGetDescription() {
        for (int i = 0; i < enums.size(); i++) {
            assertEquals(strings.get(i), enums.get(i).getDescription(),
                        "Failure - wrong description");
        }
    }

    @Test 
    void testToString() {
        for (int i = 0; i < enums.size(); i++) {
            assertEquals(strings.get(i), enums.get(i).toString(),
                        "Failure - wrong toString");
        }
    }
}