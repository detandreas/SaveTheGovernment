package budget.backend.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import budget.backend.model.domain.Budget;
import budget.backend.model.domain.BudgetItem;
import budget.backend.model.enums.Ministry;
import budget.backend.repository.BudgetRepository;
import budget.frontend.constants.Constants;
import javafx.collections.ObservableList;
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

        assertEquals(2000.0, budget2024.getTotalRevenue());
        assertEquals(1200, budget2024.getTotalExpense());
        assertEquals(800.0, budget2024.getNetResult());
    }

    // getBudgetItemsForTable
    @Test
    void tesGetBudgetItemsForTableValidYear() {
        ObservableList<BudgetItem> item = service.getBudgetItemsForTable(2024);

        assertEquals(3,item.size());
    }

    //getBudgetItemsSortedByValue
    @Test 
    void testGetBudgetItemsSortedByValueDescending() {
        ObservableList<BudgetItem> item = service.getBudgetItemsForTable(2024);

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
}
