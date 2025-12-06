package budget.repository;

import budget.model.domain.Budget;
import budget.model.domain.BudgetItem;
import budget.model.enums.Ministry;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class TestBudgetRepository {

    private BudgetRepository repository;
    private String originalDataDir;

    private Path budgetJson;
    private Path ministryJson;

    @BeforeEach
    void setUp(@TempDir Path tempDir) throws IOException {
        // backup and set data dir so PathsUtil resolves files from tempDir
        originalDataDir = System.getProperty("budget.data.dir");
        System.setProperty("budget.data.dir", tempDir.toString());

        budgetJson = tempDir.resolve("budget.json");
        ministryJson = tempDir.resolve("bill-ministry-map.json");

        // create minimal files (empty JSON structures) so repository.load doesn't
        // return early due to missing resources
        Files.writeString(budgetJson, "{}", StandardCharsets.UTF_8);
        Files.writeString(ministryJson, "{ \"byId\": {}, \"byName\": {} }", StandardCharsets.UTF_8);

        repository = new BudgetRepository();
    }

    @AfterEach
    void tearDown() {
        if (originalDataDir == null) {
            System.clearProperty("budget.data.dir");
        } else {
            System.setProperty("budget.data.dir", originalDataDir);
        }
    }

    // helper: write budget.json content
    private void writeBudgetJson(String json) throws IOException {
        Files.writeString(budgetJson, json, StandardCharsets.UTF_8);
    }

    // helper: write bill-ministry-map.json content
    private void writeMinistryJson(String json) throws IOException {
        Files.writeString(ministryJson, json, StandardCharsets.UTF_8);
    }

    //load tests
    @Test
    void testLoadEmpty() {
        List<Budget> budgets = repository.load();
        assertNotNull(budgets, "load() should never return null");
        assertTrue(budgets.isEmpty(), "With empty JSON we expect empty list");
    }

    @Test
    void testLoadMalformedBudgetJson() throws IOException {
        writeBudgetJson("{ invalid json }");
        // ministry json remains valid (empty map)
        List<Budget> budgets = repository.load();
        assertNotNull(budgets);
        assertTrue(budgets.isEmpty(), "Malformed budget.json should cause empty result");
    }
    @Test
    void testLoadValidBudgetAndMinistriesByIdAndName() throws IOException {
        writeBudgetJson("""
            {
              "2025": {
                "esoda": [ { "ID": 1, "BILL": "TaxRevenue", "VALUE": 1500.0 } ],
                "eksoda": [ { "ID": 99, "BILL": "HealthItem", "VALUE": 700.0 } ]
              }
            }
            """);

        writeMinistryJson("""
            {
              "byId": { "1": ["FINANCE"] },
              "byName": { "HealthItem": ["HEALTH", "UNKNOWN_MINISTRY"] }
            }
            """);

        List<Budget> budgets = repository.load();
        assertEquals(1, budgets.size());

        Budget b = budgets.get(0);
        assertEquals(2025, b.getYear());
        assertEquals(1500.0, b.getTotalRevenue(), 0.0001);
        assertEquals(700.0, b.getTotalExpense(), 0.0001);
        assertEquals(800.0, b.getNetResult(), 0.0001);

        List<BudgetItem> items = b.getItems();
        assertEquals(2, items.size());

        BudgetItem revenueItem = items.stream().filter(BudgetItem::getIsRevenue).findFirst().orElseThrow();
        assertEquals(1, revenueItem.getId());
        assertTrue(revenueItem.getMinistries().contains(Ministry.FINANCE));

        BudgetItem expenseItem = items.stream().filter(i -> !i.getIsRevenue()).findFirst().orElseThrow();
        assertEquals(99, expenseItem.getId());
        assertTrue(expenseItem.getMinistries().contains(Ministry.HEALTH));
    }

}

