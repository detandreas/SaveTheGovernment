package budget.backend.model.enums;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


public class TestStatus {
    private static List<Status> enums;
    private static final String PENDING_STRING = "Pending";
    private static final String APPROVED_STRING = "Approved";
    private static final String REJECTED_STRING = "Rejected";
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