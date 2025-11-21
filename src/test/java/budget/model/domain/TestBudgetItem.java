package budget.model.domain;

import budget.model.enums.Ministry;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

class TestBudgetItem {

    private BudgetItem item;
    private List<Ministry> ministries;

    @BeforeEach
    void setUp() {
        ministries = new ArrayList<>();
        ministries.add(Ministry.FINANCE);

        item = new BudgetItem(
            1,
            2024,
            "Taxes",
            100.0,
            true,
            ministries
        );
    }

    // Test Constructor + Getters

    @Test
    void testConstructorAndGetters() {
        assertEquals(1, item.getId(), "Failure - wrong id");
        assertEquals(2024, item.getYear(), "Failure - wrong year");
        assertEquals("Taxes", item.getName(), "Failure - wrong name");
        assertEquals(100.0, item.getValue(), "Failure - wrong value");
        assertTrue(item.getIsRevenue(), "Failure - wrong isRevenue");
        assertEquals(1, item.getMinistries().size(), "Failure - wrong ministries size");
        assertEquals(Ministry.FINANCE, item.getMinistries().get(0), "Failure - wrong ministry");
    }

    // Test Setters

    @Test
    void testSetName() {
        item.setName("VAT");
        assertEquals("VAT", item.getName(), "Failure - wrong name");
    }

    @Test
    void testSetValue() {
        item.setValue(999.99);
        assertEquals(999.99, item.getValue(), "Failure - wrong value");
    }

    // Test Defensive copy for ministries

    @Test
    void testGetMinistriesReturnsCopy() {
        List<Ministry> copy = item.getMinistries();
        copy.add(Ministry.HEALTH);  // modify the copy

        // internal list must NOT change
        assertEquals(1, item.getMinistries().size(), "Failure - wrong ministries size");
        assertFalse(item.getMinistries().contains(Ministry.HEALTH), "Failure - ministries list was modified");
    }

    // Test toString() formatting

    @Test
    void testToString() {
        String s = item.toString();

        assertTrue(s.contains("id=1"), "Failure - wrong toString");
        assertTrue(s.contains("year=2024"), "Failure - wrong toString");
        assertTrue(s.contains("name=Taxes"), "Failure - wrong toString");
        assertTrue(s.contains("value=100.00"), "Failure - wrong toString");
        assertTrue(s.contains("isRevenue=true"), "Failure - wrong toString");
    }
}
