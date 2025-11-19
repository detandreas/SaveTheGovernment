package budget.model.domain.user;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import budget.model.domain.BudgetItem;
import budget.model.enums.Ministry;

class TestBudgetItem {

    private BudgetItem item;
    private List<Ministry> ministries;

    @BeforeEach
    void setUp() {
        ministries = new ArrayList<>();
        ministries.add(Ministry.FINANCE);

        item = new BudgetItem(
            1,
            2025,
            "Taxes",
            500.0,
            true,
            ministries
        );
    }

    // ---------------------------------------------------------------
    // Constructor + Getters
    // ---------------------------------------------------------------

    @Test
    void testConstructorAndGetters() {
        assertEquals(1, item.getId());
        assertEquals(2024, item.getYear());
        assertEquals("Taxes", item.getName());
        assertEquals(100.0, item.getValue());
        assertTrue(item.getIsRevenue());
        assertEquals(1, item.getMinistries().size());
        assertEquals(Ministry.FINANCE, item.getMinistries().get(0));
    }

    // ---------------------------------------------------------------
    // Setters
    // ---------------------------------------------------------------

    @Test
    void testSetName() {
        item.setName("VAT");
        assertEquals("VAT", item.getName());
    }

    @Test
    void testSetValue() {
        item.setValue(999.99);
        assertEquals(999.99, item.getValue());
    }

    // ---------------------------------------------------------------
    // Defensive copy for ministries
    // ---------------------------------------------------------------

    @Test
    void testGetMinistriesReturnsCopy() {
        List<Ministry> copy = item.getMinistries();
        copy.add(Ministry.HEALTH);  // modify the copy

        // internal list must NOT change
        assertEquals(1, item.getMinistries().size());
        assertFalse(item.getMinistries().contains(Ministry.HEALTH));
    }

    // ---------------------------------------------------------------
    // toString() formatting
    // ---------------------------------------------------------------

    @Test
    void testToString() {
        String expected = String.format(
            Locale.US,
            "BudgetItem{id=%d, year=%d, name=%s, value=%.2f, isRevenue=%b}",
            1, 2025, "Taxes", 500.00, true
        );

        assertEquals(expected, item.toString());
    }
}
