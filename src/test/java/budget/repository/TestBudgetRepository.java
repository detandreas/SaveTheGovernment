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
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
        // budget.json: two items in 2025:
        //  - ID 1 (will map to ministry via byId)
        //  - ID 99 (will map via byName because not present in byId)
        writeBudgetJson("""
            {
              "2025": {
                "esoda": [ { "ID": 1, "BILL": "TaxRevenue", "VALUE": 1500.0 } ],
                "eksoda": [ { "ID": 99, "BILL": "HealthItem", "VALUE": 700.0 } ]
              }
            }
            """);

        // bill-ministry-map.json:
        // byId -> 1 : FINANCE
        // byName -> HealthItem : HEALTH
        // include also an invalid ministry name to ensure it's ignored (no exception)
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

        // confirm ministries mapping
        List<BudgetItem> items = b.getItems();
        assertEquals(2, items.size());

        BudgetItem revenueItem = items.stream().filter(BudgetItem::getIsRevenue).findFirst().orElseThrow();
        assertEquals(1, revenueItem.getId());
        assertTrue(revenueItem.getMinistries().contains(Ministry.FINANCE));

        BudgetItem expenseItem = items.stream().filter(i -> !i.getIsRevenue()).findFirst().orElseThrow();
        assertEquals(99, expenseItem.getId());
        assertTrue(expenseItem.getMinistries().contains(Ministry.HEALTH));
    }
    @Test
    void testLoadMissingMinistryFile() throws IOException {
        // write only budget.json and delete ministry file (to simulate missing)
        writeBudgetJson("""
            {
              "2026": {
                "esoda": [ { "ID": 2, "BILL": "R2", "VALUE": 100.0 } ]
              }
            }
            """);
        // delete ministry json to simulate missing resource
        Files.deleteIfExists(ministryJson);

        List<Budget> budgets = repository.load();
        assertNotNull(budgets);
        assertTrue(budgets.isEmpty());
    }

    // save tests

     @Test
    void testSaveAndReload() {
        BudgetItem r = new BudgetItem(10, 2030, "RevenueX", 500.0, true, List.of(Ministry.FINANCE));
        BudgetItem e = new BudgetItem(11, 2030, "ExpenseY", 200.0, false, List.of(Ministry.HEALTH));
        Budget b = new Budget(List.of(r, e), 2030, 500.0, 200.0, 300.0);

        repository.save(b);

        // after save, repository.load() should read back the persisted budget.json
        List<Budget> loaded = repository.load();
        assertEquals(1, loaded.size(), "After save we should have one budget");
        Budget read = loaded.get(0);
        assertEquals(2030, read.getYear());
        assertEquals(2, read.getItems().size());
    }

    @Test
    void testSaveReplaceExistingYear() {
        BudgetItem r1 = new BudgetItem(20, 2040, "R1", 100, true, List.of());
        Budget b1 = new Budget(List.of(r1), 2040, 100, 0, 100);
        repository.save(b1);

        // replace with different content for same year
        BudgetItem r2 = new BudgetItem(21, 2040, "R2", 200, true, List.of());
        Budget b2 = new Budget(List.of(r2), 2040, 200, 0, 200);
        repository.save(b2);

        List<Budget> loaded = repository.load();
        assertEquals(1, loaded.size(), "Year 2040 should be stored once");
        Budget read = loaded.get(0);
        assertEquals(1, read.getItems().size());
        assertEquals(21, read.getItems().get(0).getId());
    }

    @Test
    void testSaveNullDoesNotThrow() {
        assertDoesNotThrow(() -> repository.save(null), "Saving null must not throw");
        assertTrue(repository.load().isEmpty(), "No budgets should be present after saving null");
    }

}

