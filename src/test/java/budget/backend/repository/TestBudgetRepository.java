package budget.backend.repository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import budget.backend.model.domain.Budget;
import budget.backend.model.domain.BudgetItem;
import budget.backend.model.enums.Ministry;
import budget.constants.Limits;

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
    void testLoadMultipleYears() throws IOException {
        writeBudgetJson("""
            {
              "2023": { "esoda": [ { "ID": 1, "BILL": "R1", "VALUE": 100.0 } ] },
              "2024": { "esoda": [ { "ID": 2, "BILL": "R2", "VALUE": 200.0 } ] },
              "2025": { "esoda": [ { "ID": 3, "BILL": "R3", "VALUE": 300.0 } ] }
            }
            """);

        List<Budget> budgets = repository.load();
        assertEquals(3, budgets.size());
        assertTrue(budgets.stream().anyMatch(b -> b.getYear() == 2023));
        assertTrue(budgets.stream().anyMatch(b -> b.getYear() == 2024));
        assertTrue(budgets.stream().anyMatch(b -> b.getYear() == 2025));
    }

    @Test
    void testLoadRevenueOnly() throws IOException {
        writeBudgetJson("""
            { "2025": { "esoda": [ { "ID": 1, "BILL": "Tax", "VALUE": 1000.0 } ] } }
            """);

        Budget b = repository.load().get(0);
        assertEquals(1000.0, b.getTotalRevenue(), 0.0001);
        assertEquals(0.0, b.getTotalExpense(), 0.0001);
        assertEquals(1000.0, b.getNetResult(), 0.0001);
    }

    @Test
    void testLoadExpenseOnly() throws IOException {
        writeBudgetJson("""
            { "2025": { "eksoda": [ { "ID": 1, "BILL": "Health", "VALUE": 500.0 } ] } }
            """);

        Budget b = repository.load().get(0);
        assertEquals(0.0, b.getTotalRevenue(), 0.0001);
        assertEquals(500.0, b.getTotalExpense(), 0.0001);
        assertEquals(-500.0, b.getNetResult(), 0.0001);
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

    @Test
    void testSaveAtMinYear() {
        BudgetItem item = new BudgetItem(1, Limits.MIN_BUDGET_YEAR, "MinYear", 100, true, List.of());
        Budget b = new Budget(List.of(item), Limits.MIN_BUDGET_YEAR, 100, 0, 100);

        assertDoesNotThrow(() -> repository.save(b));
        assertTrue(repository.existsById(Limits.MIN_BUDGET_YEAR));
    }

    //exist/find tests
    @Test
    void testExistsByIdAndFindById() {
        BudgetItem item = new BudgetItem(30, 2050, "X", 100, true, List.of());
        Budget b = new Budget(List.of(item), 2050, 100, 0, 100);
        repository.save(b);

        assertTrue(repository.existsById(2050));
        Optional<Budget> found = repository.findById(2050);
        assertTrue(found.isPresent());
        assertEquals(2050, found.get().getYear());
    }

    @Test
    void testExistsByIdNull() {
        assertFalse(repository.existsById(null));
    }

    @Test
    void testFindByIdNull() {
        Optional<Budget> found = repository.findById(null);
        assertTrue(found.isEmpty());
    }

    @Test
    void testFindByIdBelowMinYear() {
        Optional<Budget> found = repository.findById(Limits.MIN_BUDGET_YEAR - 1);
        assertTrue(found.isEmpty());

        Optional<Budget> found2 = repository.findById(1999);
        assertTrue(found2.isEmpty());
    }

    @Test
    void testExistsByNameAndExistsByItemIdFindItemById() {
        BudgetItem i1 = new BudgetItem(40, 2060, "IncomeA", 100, true, List.of());
        BudgetItem i2 = new BudgetItem(41, 2060, "ExpenseB", 50, false, List.of());
        Budget b = new Budget(List.of(i1, i2), 2060, 100, 50, 50);
        repository.save(b);

        assertTrue(repository.existsByName("IncomeA", 2060));
        assertFalse(repository.existsByName("NoSuch", 2060));

        assertTrue(repository.existsByItemId(40, 2060));
        assertFalse(repository.existsByItemId(999, 2060));

        Optional<BudgetItem> found = repository.findItemById(41, 2060, false);
        assertTrue(found.isPresent());
        assertEquals("ExpenseB", found.get().getName());
    }

    @Test
    void testExistsByNameWithNullName() {
        assertFalse(repository.existsByName(null, 2025),
                                "Failure - can't search with null name");
    }

    @Test
    void testExistsByNameWhenBudgetForThatYearDoesntExist() {
        BudgetItem i1 = new BudgetItem(40, 2060, "IncomeA", 100, true, List.of());
        Budget b = new Budget(List.of(i1), 2060, 100, 50, 50);
        repository.save(b);

        assertFalse(repository.existsByName("IncomeA", 2025),
                                "Failure - Item name doesn't exist for year 2025");
    }

    @Test
    void testExistsByItemIdInvalidParams() {
        // invalid id
        assertFalse(repository.existsByItemId(0, 2060));
        assertFalse(repository.existsByItemId(1, 2060));
        // invalid year (< limits) - repository uses Limits.MIN_BUDGET_YEAR (= 2000),
        // pass a year earlier than that to assert false behavior
        assertFalse(repository.existsByItemId(1, 1900));
    }

    @Test
    void testFindItemById() {
        List<Optional<BudgetItem>> listOfBudgetItems = new ArrayList<>();
        var b1 = repository.findItemById(-1, 2025, true);
        var b2 = repository.findItemById(1, 1999, false);
        var b3 = repository.findItemById(1, 2060, true);

        listOfBudgetItems.addAll(List.of(b1,b2,b3));

        for (var b : listOfBudgetItems) {
            assertTrue(b.isEmpty(), "Failure - optional should be empty");
        }
    }

    @Test
    void testFindItemByIdWithBudgetFail() {
        var opt1 = repository.findItemById(-1, new Budget(List.of(), 2025, 0, 0, 0), true);
        assertTrue(opt1.isEmpty(),"Failure - negative id should return empty optional");
        var opt2 = repository.findItemById(1,null, true);
        assertTrue(opt1.isEmpty(), "Failure - null budget should return empty optional");
    }

    @Test
    void testFindItemByIdWithBudget() {
        BudgetItem r = new BudgetItem(10, 2025, "RevenueX", 500.0, true, List.of(Ministry.FINANCE));
        BudgetItem e = new BudgetItem(11, 2025, "ExpenseY", 200.0, false, List.of(Ministry.HEALTH));
        Budget b = new Budget(List.of(r, e), 2025, 500.0, 200.0, 300.0);

        repository.save(b);

        var itemOpt = repository.findItemById(r.getId(), b, r.getIsRevenue());
        assertTrue(itemOpt.isPresent(), "Failure - findItemById not working properly");
    }

    @Test
    void testDeleteExistingAndNonExistingAndNull() {
        BudgetItem item = new BudgetItem(50, 2050, "D", 10, true, List.of());
        Budget b = new Budget(List.of(item), 2050, 10, 0, 10);
        repository.save(b);

        // delete existing
        repository.delete(b);
        assertFalse(repository.existsById(2050));

        // delete non-existing (should not throw)
        assertDoesNotThrow(() -> repository.delete(new Budget(List.of(), 2000, 0, 0, 0)));

        // delete null should not throw
        assertDoesNotThrow(() -> repository.delete(null));
    }

    @Test
    void testFullCrudCycle() {
        // create
        BudgetItem item = new BudgetItem(77, 2050, "Full", 111, true, List.of());
        Budget b = new Budget(List.of(item), 2050, 111, 0, 111);
        repository.save(b);
        assertTrue(repository.existsById(2050));

        // read
        Optional<Budget> read = repository.findById(2050);
        assertTrue(read.isPresent());
        assertEquals(1, read.get().getItems().size());

        // update (replace same year)
        Budget updated = new Budget(List.of(new BudgetItem(78, 2050, "Full2", 222, false, List.of())), 2050, 0, 222, -222);
        repository.save(updated);

        Optional<Budget> afterUpdate = repository.findById(2050);
        assertTrue(afterUpdate.isPresent());
        assertEquals(78, afterUpdate.get().getItems().get(0).getId());

        // delete
        repository.delete(afterUpdate.get());
        assertFalse(repository.existsById(2050));
    }

    @Test
    void testMultipleBudgets() {
        repository.save(new Budget(List.of(new BudgetItem(1, 2000, "A", 1, true, List.of())), 2000, 1, 0, 1));
        repository.save(new Budget(List.of(new BudgetItem(2, 2001, "B", 2, true, List.of())), 2001, 2, 0, 2));
        repository.save(new Budget(List.of(new BudgetItem(3, 2002, "C", 3, true, List.of())), 2002, 3, 0, 3));

        List<Budget> all = repository.load();
        assertEquals(3, all.size());

        // delete middle one and ensure others remain
        repository.delete(new Budget(List.of(), 2001, 0, 0, 0));
        all = repository.load();
        assertEquals(2, all.size());
        assertTrue(repository.existsById(2000));
        assertFalse(repository.existsById(2001));
        assertTrue(repository.existsById(2002));
    }

    @Test
    void testInvalidMinistryNamesIgnored() throws IOException {
        writeBudgetJson("""
            { "2025": { "esoda": [ { "ID": 1, "BILL": "Test", "VALUE": 100.0 } ] } }
            """);
        writeMinistryJson("""
            { "byId": { "1": ["FINANCE", "INVALID_MINISTRY", "FAKE"] }, "byName": { } }
            """);

        BudgetItem item = repository.load().get(0).getItems().get(0);

        assertEquals(1, item.getMinistries().size(), "Invalid ministries should be ignored");
        assertTrue(item.getMinistries().contains(Ministry.FINANCE));
    }

    @Test
    void testByIdPreferenceOverByName() throws IOException {
        writeBudgetJson("""
            { "2025": { "esoda": [ { "ID": 1, "BILL": "Item", "VALUE": 100.0 } ] } }
            """);
        writeMinistryJson("""
            {
              "byId": { "1": ["FINANCE"] },
              "byName": { "Item": ["HEALTH"] }
            }
            """);

        BudgetItem item = repository.load().get(0).getItems().get(0);

        assertTrue(item.getMinistries().contains(Ministry.FINANCE));
        assertFalse(item.getMinistries().contains(Ministry.HEALTH));
    }

    @Test
    void testMultipleMinistriesMapping() throws IOException {
        writeBudgetJson("""
            { "2025": { "esoda": [ { "ID": 1, "BILL": "Shared", "VALUE": 1000.0 } ] } }
            """);
        writeMinistryJson("""
            { "byId": { "1": ["FINANCE", "DEVELOPMENT", "INTERIOR"] }, "byName": { } }
            """);

        BudgetItem item = repository.load().get(0).getItems().get(0);

        assertEquals(3, item.getMinistries().size());
        assertTrue(item.getMinistries().contains(Ministry.FINANCE));
        assertTrue(item.getMinistries().contains(Ministry.DEVELOPMENT));
        assertTrue(item.getMinistries().contains(Ministry.INTERIOR));
    }
}


