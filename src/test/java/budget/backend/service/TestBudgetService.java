package budget.backend.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import budget.backend.model.domain.Budget;
import budget.backend.model.domain.BudgetItem;
import budget.backend.model.enums.Ministry;
import budget.backend.repository.BudgetRepository;
import budget.frontend.constants.Constants;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart.Series;

public class TestBudgetService {
    private BudgetRepository repository;
    private BudgetService service;
    private Budget budget2024;
    private String originalDataDir;
    private Path budgetJson;

    @BeforeEach
    void setup(@TempDir Path tempDir) throws IOException {
        originalDataDir = System.getProperty("budget.data.dir");
        System.setProperty("budget.data.dir", tempDir.toString());

        //Δημιουργεια ενος αδειου αρχειου στο οποιο θα αποθηκευονται
        // τα προσωρινα αρχεια που δημιουργουμε για να μην πειραξουμε τα αρχικα 
        budgetJson = tempDir.resolve("budget.json");
        Files.writeString(budgetJson, "{}", StandardCharsets.UTF_8);

        // Files.writeString(tempDir.resolve("bill-ministry-map.json"), "{}", StandardCharsets.UTF_8);

        repository = new BudgetRepository();
        service = new BudgetService(repository);

        BudgetItem revenueItem24 = new BudgetItem(1, 2024, "revenueItem",
            2000.0, true, List.of(Ministry.FINANCE));
        BudgetItem expenseItem1 = new BudgetItem(2, 2024, "expenseItem1",
            800.0, false, List.of(Ministry.LABOUR));
        BudgetItem expenseItem2 = new BudgetItem(3, 2024, "expenseItem2",
            400.0, false, List.of(Ministry.FINANCE));
        BudgetItem loan = new BudgetItem(4, 2024, Constants.LOANS_ITEM_NAME,
            400.0, true, List.of(Ministry.FINANCE));

        budget2024 = new Budget(List.of(revenueItem24, expenseItem1, expenseItem2, loan),2024);
        service.recalculateBudgetTotals(budget2024);
        repository.save(budget2024);

        BudgetItem revenueItem25 = new BudgetItem(4, 2025, "revenueItem",
        2100.0, true, List.of(Ministry.FINANCE));

        Budget budget2025 = new Budget(List.of(revenueItem25), 2025);
        service.recalculateBudgetTotals(budget2025);
        repository.save(budget2025);
    }

    @AfterEach
    void tearDown() {
        if(originalDataDir == null) {
            System.clearProperty("budget.data.dir");
        } else {
            System.setProperty("budget.data.dir", originalDataDir);
        }
    }
    
    // recalculateBudgetTotals
    @Test
    void testRecalculateBudgetTotalsNullBudgetTrhows() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> service.recalculateBudgetTotals(null));
        assertEquals("Budget cannot be null", ex.getMessage());
    }

    @Test
    void testRecalculateBudgetTotalsValid() {
        service.recalculateBudgetTotals(budget2024);

        assertEquals(2400.0, budget2024.getTotalRevenue());
        assertEquals(1200, budget2024.getTotalExpense());
        assertEquals(1200.0, budget2024.getNetResult());
    }

    // getBudgetItemsForTable
    @Test
    void tesGetBudgetItemsForTableValidYear() {
        ObservableList<BudgetItem> item = service.getBudgetItemsForTable(2024);

        assertEquals(4,item.size());
    }

    //getBudgetItemsSortedByValue
    @Test 
    void testGetBudgetItemsSortedByValueDescending() {
        ObservableList<BudgetItem> item = service.getBudgetItemsSortedByValue(2024);

        assertEquals(2000, item.get(0).getValue());
        assertEquals(800, item.get(1).getValue());
        assertEquals(400, item.get(2).getValue());
    }

    //getLoansTrendSeries
    @Test
    void testGetLoansTrendSeries1DataPoint() {
        Series<Number, Number> series = service.getLoansTrendSeries(2024, 2025, true);
        assertEquals(1, series.getData().size());
    }

    @Test
    void testGetLoansTrendSeriesSumRevenueLoan() {
        Series<Number, Number> series = service.getLoansTrendSeries(2024, 2025, true);
        assertEquals(400.0, series.getData().get(0).getYValue());
    }

    @Test
    void testGetLoansTrendSeriesInvalidYearThrows() {
        assertThrows(IllegalArgumentException.class,
            () -> service.getLoansTrendSeries(2025, 2024, true));
    }

    //getNetResultSeries
    @Test
    void testGetNetResultSeriesInvalidYearThrows() {
        assertThrows(IllegalArgumentException.class,
            () -> service.getNetResultSeries(2025, 2024));
    }

    @Test
    void testGetNetResultSeriesHasCorrectName() {
        Series<Number, Number> series = service.getNetResultSeries(2024, 2025);
        assertEquals(Constants.NET_RESULT_LABEL, series.getName());
    }

    @Test
    void testGetNetResultSeriesSkipsMissingBudgets() {
        Series<Number, Number> series = service.getNetResultSeries(2023, 2025);

        assertEquals(1, series.getData().size());
        assertEquals(2024, series.getData().get(0).getXValue());
    }

    @Test
    void testGetNetResultSeriesReturnCorrectValue() {
        Series<Number, Number> series = service.getNetResultSeries(2024, 2025);
        // netResult = 2000.0 + 400.0 - 800.0 - 400.0 = 1200.0
        assertEquals(1200.0, series.getData().get(0).getYValue());
    }

    // getRevenueExpenseTrendSeries
    @Test
    void  testGetRevenueExpenseTrendSeriesInvalidYearThrows() {
        assertThrows(IllegalArgumentException.class,
            () -> service.getRevenueExpenseTrendSeries(2025, 2024));
    }

    @Test
    void testGetRevenueExpenseTrendSeriesHasCorrectName() {
        Map<String, Series<Number, Number>> result = service.getRevenueExpenseTrendSeries(2024, 2026);
        assertTrue(result.containsKey(Constants.REVENUE_LABEL));
        // σαν setName οριζεται ως Constants.EXPENSES_LABEL αλλα επιστρεφει Constants.EXPENSE_LABEL
        assertTrue(result.containsKey(Constants.EXPENSES_LABEL));
    }

    @Test
    void testGetRevenueExpenseTrendSeriesSkipsMissingBudgets() {
        Map<String, Series<Number, Number>> result = service.getRevenueExpenseTrendSeries(2024, 2026);

        assertEquals(2, result.get(Constants.REVENUE_LABEL).getData().size());
    }

    @Test
    void testGetRevenueExpenseTrendSeriesReturnCorrectValue() {
        Map<String, Series<Number, Number>> result = service.getRevenueExpenseTrendSeries(2024, 2025);

        assertEquals(2400.0, result.get(Constants.REVENUE_LABEL).getData().get(0).getYValue());
        assertEquals(1200.0, result.get(Constants.EXPENSE_LABEL).getData().get(0).getYValue());
    }

    //getTopBudgetItemsSeries
    @Test
    void testGetTopBudgetItemsSeriesInvalidYearThrows() {
        assertThrows(IllegalArgumentException.class, () ->
            service.getTopBudgetItemsSeries(2026, 2, true, true));
    }

    @Test
    void testGetTopBudgetItemsSeriesInvalidTopNThrows() {
        assertThrows(IllegalArgumentException.class,
            () -> service.getTopBudgetItemsSeries(2024, 0, true, true));
    }

    @Test
    void testGetTopBudgetItemsSeriesHasCorrectName() {
        Series<String, Number> series1 = service.getTopBudgetItemsSeries(2024, 2, true, true);
        Series<String, Number> series2 = service.getTopBudgetItemsSeries(2024, 2, false, true);

        assertEquals(Constants.TOP_REVENUE_LABEL, series1.getName());
        assertEquals(Constants.TOP_EXPENSE_LABEL, series2.getName());
    }

    @Test
    void testGetTopBudgetItemsSeriesExpensesTopOne() {
        Series<String, Number> series = service.getTopBudgetItemsSeries(2024, 1, false, true);

        assertEquals(1, series.getData().size());
        assertEquals("expenseItem1", series.getData().get(0).getXValue());

    }

    //getYearComparisonSeries
    @Test
    void testGetYearComparisonSeriesInvalidYearThrows() {
        assertThrows(IllegalArgumentException.class,
            () -> service.getYearComparisonSeries(2024, 2024));
    }

    @Test
    void testGetYearComparisonSeriesValid() {
        Map<String, Series<String, Number>> result = service.getYearComparisonSeries(2024, 2025);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey("2024"));
    }

    // getRevenueExpenseBarSeries
    @Test
    void testGetRevenueExpenseBarSeriesInvalidYearThrows() {
        assertThrows(IllegalArgumentException.class,
            () -> service.getRevenueExpenseBarSeries(2031));
    }

    @Test
    void testGetRevenueExpenseBarSeriesHasCorrectName() {
        Series<String, Number> series = service.getRevenueExpenseBarSeries(2024);

        assertEquals(Constants.BUDGET_OVERVIEW_LABEL, series.getName());
    }

    @Test
    void testGetRevenueExpenseBarSeriesValid() {
        Series<String, Number> series = service.getRevenueExpenseBarSeries(2024);

        assertEquals(2, series.getData().size());
        assertEquals(2400.0, series.getData().get(0).getYValue());
        assertEquals(1200.0, series.getData().get(1).getYValue());
    }

    //getRevenueExpensePieData
    @Test
    void getRevenueExpensePieDataInvalidYearThrows() {
        assertThrows(IllegalArgumentException.class,
            () -> service.getRevenueExpensePieData(2030));
    }

    @Test
    void testGetRevenueExpensePieDataValid() {
        ObservableList<PieChart.Data> data = service.getRevenueExpensePieData(2024);

        assertEquals(2, data.size()); // Revenue and Expense

        assertEquals(2400.0, data.get(0).getPieValue()); // Revenue
        assertEquals(1200.0, data.get(1).getPieValue()); // Expense
    }

    //getBudgetItemsforPie
    @Test
    void testGetBudgetItemsforPieInvalidYearThrows() {
        assertThrows(IllegalArgumentException.class,
            () -> service.getBudgetItemsforPie(2030, true));
    }

    @Test
    void testGetBudgetItemsforPieNoOtherWhenTopCoverAll() {
        ObservableList<PieChart.Data> data = service.getBudgetItemsforPie(2025, true);

        boolean hasOther = data.stream()
                                .anyMatch(d -> d.getName().contains(Constants.OTHERS_LABEL));
        //Δεν πρεπει να υπαρχει κατηγορια other για ενα μονο στοιχειο
        assertFalse(hasOther);
    }

    @Test
    void testGetBudgetItemsforPieValid() {
        BudgetItem bigRevenueItem = new BudgetItem(1, 2026, "Revenue",
        10000.0, true, List.of(Ministry.FINANCE));

        List<BudgetItem> items2026 = new ArrayList<>();
        items2026.add(bigRevenueItem);

        for (int i = 0; i < 10; i++) {
            items2026.add(new BudgetItem(2 + i, 2026, "Revenue",
                5.0, true, List.of(Ministry.FINANCE)));
        }
        Budget budgetWithOthers = new Budget(items2026, 2026);
        service.recalculateBudgetTotals(budgetWithOthers);
        repository.save(budgetWithOthers);

        ObservableList<PieChart.Data> data = service.getBudgetItemsforPie(2026, true);
        boolean hasOther = data.stream()
                                .anyMatch(d -> d.getName().contains(Constants.OTHERS_LABEL));
        
        assertTrue(hasOther);
    }

    //getTopItemsTrendSeries
    @Test
    void getTopItemsTrendSeriesInvalidYearRangeThrows() {
        assertThrows(IllegalArgumentException.class,
            () -> service.getTopItemsTrendSeries(2024, 2025, 2024, 2, true));
    }

    @Test
    void getTopItemsTrendSeriesInvalidYearThrows() {
        assertThrows(IllegalArgumentException.class,
            () -> service.getTopItemsTrendSeries(2027, 2027, 2030, 2, true));
    }

    @Test
    void getTopItemsTrendSeriesInvalidTopNThrows() {
    assertThrows(IllegalArgumentException.class,
        () -> service.getTopItemsTrendSeries(2024, 2023, 2024, 0, true));
    }

    @Test
    void testGetTopItemsTrendSeriesLogic() {
        Map<String, Series<Number, Number>> result = 
            service.getTopItemsTrendSeries(2024, 2024, 2026, 1, true);

        assertTrue(result.containsKey("revenueItem"));
        // Το 2025 το revenueItem έχει τιμή 2100.0
        assertEquals(2100.0, result.get("revenueItem").getData().get(1).getYValue());
    }
}
