package budget.backend.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import budget.backend.repository.BudgetRepository;
import budget.constants.Limits;

public class TestStatisticsService {

    private static StatisticsService service;
    private static final int DEFAULT_START_YEAR = 2019;
    private static final int DEFAULT_END_YEAR = 2027;
    private static final int DEFAULT_TOP_N = 5;
    
    @BeforeAll
    static void setUp() {
        service = new StatisticsService(new BudgetRepository());
    }

    /* Tests for getFormattedRevenueExpensePieData() */

    @Test
    void testgetFormattedRevenueExpensePieDataInvalidYear() {
        IllegalArgumentException ex =
            assertThrows(IllegalArgumentException.class, () -> service.getFormattedRevenueExpensePieData(1000),
        "Invalid year should throw");
        assertEquals("Year must be >= " + Limits.MIN_BUDGET_YEAR
                + ", but was: " + 1000, ex.getMessage(), "Failure - wrong message");
    }

    @Test
    void testgetFormattedRevenueExpensePieDataInvalidYear2() {
        IllegalArgumentException ex =
            assertThrows(IllegalArgumentException.class, () -> service.getFormattedRevenueExpensePieData(DEFAULT_END_YEAR),
            "Invalid year should throw");
        assertEquals(String.format("Budget for year %d doesn't exist", DEFAULT_END_YEAR), ex.getMessage(),
    "Failure - wrong message");
    }

    @Test
    void testgetFormattedRevenueExpensePieDataValid() {
        assertDoesNotThrow(() -> service.getFormattedRevenueExpensePieData(DEFAULT_START_YEAR),
    "Failure - valid year should not throw");
    }

    /* Tests for getTrendWithRegression() */

    @Test
    void testgetTrendWithRegressionInvalidYearRange() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> service.getTrendWithRegression(DEFAULT_END_YEAR, DEFAULT_START_YEAR, true),
        "Failure - Invalid year range should throw");
        assertEquals("startYear must be less than endYear, "
                + "but was: startYear=" + DEFAULT_END_YEAR
                + ", endYear=" + DEFAULT_START_YEAR, ex.getMessage());
    }

    @Test
    void testgetTrendWithRegressionValid() {
        assertDoesNotThrow(() -> service.getTrendWithRegression(DEFAULT_START_YEAR, DEFAULT_END_YEAR, true),
    "Failure - valid year range should not throw");
    }

    /* Tests for getTopItemsForComboBox() */

    @Test
    void testgetTopItemsForComboBoxInvalidYear() {
        assertDoesNotThrow(() -> service.getTopItemsForComboBox(1000, DEFAULT_TOP_N, true),
        "Failure - ivalid year should not throw");

        var topItems = service.getTopItemsForComboBox(1000, DEFAULT_TOP_N, true);
        assertEquals(1, topItems.size(),
        "Failure - invalid year should return list size=1");
        assertEquals("All", topItems.get(0),
        "Failure - invalid content");
    }

    @Test
    void testgetTopItemsForComboBoxInvalidTopN() {
        assertDoesNotThrow(() -> service.getTopItemsForComboBox(DEFAULT_START_YEAR, -1, true),
        "Failure - ivalid topN should not throw");

        var topItems = service.getTopItemsForComboBox(DEFAULT_START_YEAR, -1, true);
        assertEquals(1, topItems.size(),
        "Failure - invalid topN should return list size=1");
        assertEquals("All", topItems.get(0),
        "Failure - invalid content");
    }

    @Test
    void testgetTopItemsForComboBoxValid() {
        assertDoesNotThrow(() -> service.getTopItemsForComboBox(DEFAULT_START_YEAR, DEFAULT_TOP_N, true),
        "Failure - ivalid topN should not throw");

        var topItems = service.getTopItemsForComboBox(DEFAULT_START_YEAR, DEFAULT_TOP_N, true);
        assertEquals(6, topItems.size(),
        "Failure - topN=5 should return list size=6");
    }

    /* Tests for getSingleItemTrendWithRegression() */

    @Test
    void getSingleItemTrendWithRegressionInvalidYear() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> service.getSingleItemTrendWithRegression(1000, "Taxes", true),
    "Failure - invalid year should throw");
        assertEquals("Year must be >= " + Limits.MIN_BUDGET_YEAR
                + ", but was: " + 1000, ex.getMessage(),
    "Failure - invalid message");
    }

    @Test
    void getSingleItemTrendWithRegressionInvalidItemName() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> service.getSingleItemTrendWithRegression(DEFAULT_START_YEAR, "Invalid", true),
        "Failure - invalid name should throw");
        assertEquals("Item not found: Invalid" , ex.getMessage(),
        "Failure - invalid message");
    }

    @Test
    void getSingleItemTrendWithRegressionValid() {
        assertDoesNotThrow(() -> service.getSingleItemTrendWithRegression(DEFAULT_START_YEAR, "Taxes", true),
        "Failure - valid method call should not throw ");
        var map = service.getSingleItemTrendWithRegression(DEFAULT_START_YEAR, "Taxes", true);
        assertEquals(2, map.keySet().size(),
        "Failure - invalid content");
        assertEquals(2, map.values().size(),
        "Failure - invalid content");
    }

    /* Tests for getLoansTrendWithRegression() */

    @Test
    void testgetLoansTrendWithRegressionValids() {
        assertDoesNotThrow(() -> service.getLoansTrendWithRegression(true),
        "Failure - valid params should not throw");
        var map = service.getLoansTrendWithRegression(true);
        assertEquals(2, map.keySet().size(),
        "Failure - invalid content");
        assertEquals(2, map.values().size(),
        "Failure - invalid content");
    }

    /* Tests for getNetResultWithRegression() */

    @Test
    void testgetNetResultWithRegression() {
        assertDoesNotThrow(() -> service.getNetResultWithRegression(),
        "Failure - valid method call should not throw");
        var map = service.getNetResultWithRegression();
        assertEquals(2, map.keySet().size(),
        "Failure - invalid content");
        assertEquals(2, map.values().size(),
        "Failure - invalid content");
    }

    /* Tests for getBudgetService() */

    @Test
    void testGetBudgetService() {
        BudgetService serv = service.getBudgetService();
        assertNotNull(serv);
    }
}
