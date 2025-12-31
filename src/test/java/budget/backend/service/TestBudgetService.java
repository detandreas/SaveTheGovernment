package budget.backend.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
import javafx.scene.chart.XYChart.Data;
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
    void testRecalculateBudgetTotalsNullBudgetThrows() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> service.recalculateBudgetTotals(null));
        assertEquals("Budget cannot be null", ex.getMessage());
    }

    @Test
    void testRecalculateBudgetTotalsValid() {
        service.recalculateBudgetTotals(budget2024);

        assertEquals(2400.0, budget2024.getTotalRevenue(), 
            "Failure - Total revenue should be 2400.0");
        assertEquals(1200, budget2024.getTotalExpense(), 
            "Failure - Total expense should be 1200.0");
        assertEquals(1200.0, budget2024.getNetResult(), 
            "Failure - Net result should be 1200.0");
    }

    // getBudgetItemsForTable
    @Test
    void tesGetBudgetItemsForTableValidYear() {
        ObservableList<BudgetItem> item = service.getBudgetItemsForTable(2024);

        assertEquals(4, item.size(), 
            "Failure - Budget 2024 should have 4 items");
    }

    //getBudgetItemsSortedByValue
    @Test 
    void testGetBudgetItemsSortedByValueDescending() {
        ObservableList<BudgetItem> item = service.getBudgetItemsSortedByValue(2024);

        assertEquals(2000, item.get(0).getValue(), 
            "Failure - First item should have value 2000");
        assertEquals(800, item.get(1).getValue(), 
            "Failure - Second item should have value 800");
        assertEquals(400, item.get(2).getValue(), 
            "Failure - Third item should have value 400");
    }

    //getLoansTrendSeries
    @Test
    void testGetLoansTrendSeries1DataPoint() {
        Series<Number, Number> series = service.getLoansTrendSeries(2024, 2025, true);
        assertEquals(1, series.getData().size(), 
            "Failure - Series should contain 1 data point");
    }

    @Test
    void testGetLoansTrendSeriesSumRevenueLoan() {
        Series<Number, Number> series = service.getLoansTrendSeries(2024, 2025, true);
        assertEquals(400.0, series.getData().get(0).getYValue(), 
            "Failure - Revenue loan should be 400.0");
    }

    @Test
    void testGetLoansTrendSeriesSumExpenseLoan() {
        Series<Number, Number> series = service.getLoansTrendSeries(2024, 2025, false);
        assertEquals(0.0, series.getData().get(0).getYValue(), 
            "Failure - Expense loan should be 0.0");
    }

    @Test
    void testGetLoansTrendSeriesInvalidYearThrows() {
        assertThrows(IllegalArgumentException.class,
            () -> service.getLoansTrendSeries(2025, 2024, true));
    }

    @Test
    void testGetLoansTrendSeriesInvalidYear() {
        assertDoesNotThrow(() -> service.getLoansTrendSeries(2022, 2023, true), 
            "Failure - Should not throw for non-existent year");
        var series = service.getLoansTrendSeries(2022, 2023, true);
        assertEquals(0.0, series.getData().get(0).getYValue(), 
            "Failure - Non-existent year should return 0.0");
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
        assertEquals(Constants.NET_RESULT_LABEL, series.getName(), 
            "Failure - Series name should be NET_RESULT_LABEL");
    }

    @Test
    void testGetNetResultSeriesSkipsMissingBudgets() {
        Series<Number, Number> series = service.getNetResultSeries(2023, 2025);

        assertEquals(1, series.getData().size(), 
            "Failure - Should only include existing budgets");
        assertEquals(2024, series.getData().get(0).getXValue(), 
            "Failure - Data point should be for year 2024");
    }

    @Test
    void testGetNetResultSeriesReturnCorrectValue() {
        Series<Number, Number> series = service.getNetResultSeries(2024, 2025);
        // netResult = 2000.0 + 400.0 - 800.0 - 400.0 = 1200.0
        assertEquals(1200.0, series.getData().get(0).getYValue(), 
            "Failure - Net result for 2024 should be 1200.0");
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
        assertTrue(result.containsKey(Constants.REVENUE_LABEL), 
            "Failure - Result should contain REVENUE_LABEL key");
        // σαν setName οριζεται ως Constants.EXPENSES_LABEL αλλα επιστρεφει Constants.EXPENSE_LABEL
        assertTrue(result.containsKey(Constants.EXPENSE_LABEL), 
            "Failure - Result should contain EXPENSE_LABEL key");
    }

    @Test
    void testGetRevenueExpenseTrendSeriesSkipsMissingBudgets() {
        Map<String, Series<Number, Number>> result = service.getRevenueExpenseTrendSeries(2024, 2026);

        assertEquals(2, result.get(Constants.REVENUE_LABEL).getData().size(), 
            "Failure - Should only include 2 existing budgets (2024, 2025)");
    }

    @Test
    void testGetRevenueExpenseTrendSeriesReturnCorrectValue() {
        Map<String, Series<Number, Number>> result = service.getRevenueExpenseTrendSeries(2024, 2025);

        assertEquals(2400.0, result.get(Constants.REVENUE_LABEL).getData().get(0).getYValue(), 
            "Failure - Revenue for 2024 should be 2400.0");
        assertEquals(1200.0, result.get(Constants.EXPENSE_LABEL).getData().get(0).getYValue(), 
            "Failure - Expense for 2024 should be 1200.0");
    }

    @Test
    void testGetRevenueExpenseTrendSeriesYearDoesntExist() {
        assertDoesNotThrow(() -> service.getRevenueExpenseTrendSeries(2022, 2025), 
            "Failure - Should not throw for non-existent years in range");
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

        assertEquals(Constants.TOP_REVENUE_LABEL, series1.getName(), 
            "Failure - Revenue series should have TOP_REVENUE_LABEL name");
        assertEquals(Constants.TOP_EXPENSE_LABEL, series2.getName(), 
            "Failure - Expense series should have TOP_EXPENSE_LABEL name");
    }

    @Test
    void testGetTopBudgetItemsSeriesExpensesTopOne() {
        Series<String, Number> series = service.getTopBudgetItemsSeries(2024, 1, false, true);

        assertEquals(1, series.getData().size(), 
            "Failure - Should return only top 1 expense item");
        assertEquals("expenseItem1", series.getData().get(0).getXValue(), 
            "Failure - Top expense item should be expenseItem1");

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

        assertNotNull(result, 
            "Failure - Result should not be null");
        assertEquals(2, result.size(), 
            "Failure - Result should contain 2 year series");
        assertTrue(result.containsKey("2024"), 
            "Failure - Result should contain year 2024");
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

        assertEquals(Constants.BUDGET_OVERVIEW_LABEL, series.getName(), 
            "Failure - Series name should be BUDGET_OVERVIEW_LABEL");
    }

    @Test
    void testGetRevenueExpenseBarSeriesValid() {
        Series<String, Number> series = service.getRevenueExpenseBarSeries(2024);

        assertEquals(2, series.getData().size(), 
            "Failure - Series should contain 2 data points (revenue and expense)");
        assertEquals(2400.0, series.getData().get(0).getYValue(), 
            "Failure - First data point (revenue) should be 2400.0");
        assertEquals(1200.0, series.getData().get(1).getYValue(), 
            "Failure - Second data point (expense) should be 1200.0");
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

        assertEquals(2, data.size(), 
            "Failure - Pie data should contain 2 slices (Revenue and Expense)");

        assertEquals(2400.0, data.get(0).getPieValue(), 
            "Failure - Revenue pie value should be 2400.0");
        assertEquals(1200.0, data.get(1).getPieValue(), 
            "Failure - Expense pie value should be 1200.0");
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
        assertFalse(hasOther, 
            "Failure - Should not have 'Others' category when only one item exists");
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
        
        assertTrue(hasOther, 
            "Failure - Should have 'Others' category when multiple small items exist");
    }

    //getTopItemsTrendSeries
    @Test
    void testGetTopItemsTrendSeriesInvalidYearRangeThrows() {
        assertThrows(IllegalArgumentException.class,
            () -> service.getTopItemsTrendSeries(2024, 2025, 2024, 2, true));
    }

    @Test
    void testGetTopItemsTrendSeriesInvalidYearThrows() {
        assertThrows(IllegalArgumentException.class,
            () -> service.getTopItemsTrendSeries(2027, 2027, 2030, 2, true));
    }

    @Test
    void testGetTopItemsTrendSeriesInvalidTopNThrows() {
    assertThrows(IllegalArgumentException.class,
        () -> service.getTopItemsTrendSeries(2024, 2023, 2024, 0, true));
    }

    @Test
    void testGetTopItemsTrendSeriesLogic() {
        Map<String, Series<Number, Number>> result = 
            service.getTopItemsTrendSeries(2024, 2024, 2026, 1, true);

        assertTrue(result.containsKey("revenueItem"), 
            "Failure - Result should contain 'revenueItem' series");
        // Το 2025 το revenueItem έχει τιμή 2100.0
        assertEquals(2100.0, result.get("revenueItem").getData().get(1).getYValue(), 
            "Failure - revenueItem value for 2025 should be 2100.0");
    }

    //creatRegressionSeries
    @Test
    void creatRegressionSeriesLogic() {
        Series<Number, Number> dataSeries = new Series<>();
        dataSeries.getData().add(new Data<> (2020, 1000.0));
        dataSeries.getData().add(new Data<> (2021, 2000.0));

        Series<Number, Number> regression = service.createRegressionSeries(dataSeries);

        assertFalse(regression.getData().isEmpty(), 
            "Failure - Regression series should not be empty");
        assertEquals(11, regression.getData().size(), 
            "Failure - Regression series should contain 11 data points");
    }

    //updateItemValue
    @Test
    void testUpdateItemYearDoesntExistThrows() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                                                () -> service.updateItemValue(1, 2027, 2000.0));
        assertEquals(String.format(
            "Cannot update item. Budget for year %d not found.", 2027),
            ex.getMessage());
    }

    @Test
    void testUpdateItemValid() {
        assertDoesNotThrow(() -> service.updateItemValue(1, 2024, 2000.0), 
            "Failure - Should not throw when updating valid item");
    }

    @Test
    void testUpdateItemButItDoesntExist() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> service.updateItemValue(11, 2024, 2000.0));
        assertEquals(String.format(
            "Item with ID %d not found in budget year %d",
            11, 2024),
                    ex.getMessage(),
                    "Failure - Should throw exception with correct message for non-existent item");
    }

}
