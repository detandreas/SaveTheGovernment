package budget.backend.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import budget.backend.exceptions.ValidationException;
import budget.backend.model.domain.Budget;
import budget.backend.model.domain.BudgetItem;
import budget.backend.model.enums.Ministry;
import budget.backend.repository.BudgetRepository;
import budget.constants.Message;

public class TestBudgetValidationService {
    private BudgetRepository repo;
    private BudgetValidationService service;
    private static Budget budget;
    private static BudgetItem newItem;
    private String originalDataDir;
    private Path budgetJson;

    @BeforeEach
    void initTempDir(@TempDir Path tempDir) throws IOException {
        // backup and set data dir so PathsUtil resolves files from tempDir
        originalDataDir = System.getProperty("budget.data.dir");
        System.setProperty("budget.data.dir", tempDir.toString());

        budgetJson = tempDir.resolve("budget.json");
        // create minimal files (empty JSON structures) so repository.load doesn't
        // return early due to missing resources
        Files.writeString(budgetJson, "{}", StandardCharsets.UTF_8);

        repo = new BudgetRepository();
        service = new BudgetValidationService(repo);
    }

    @AfterEach
    void tearDown() {
        if (originalDataDir == null) {
            System.clearProperty("budget.data.dir");
        } else {
            System.setProperty("budget.data.dir", originalDataDir);
        }
    }

    @BeforeAll
    static void initStaticTestData() {
        newItem = new BudgetItem(1, 2025, "Infra", 10000.0, true, List.of(Ministry.HEALTH));
        budget = new Budget(new ArrayList<>(), 2025); //netResult = 0 : any change allowed
    }
    
    // TESTs for validateBudgetItemCreation()

    @Test
    void testValidBudgetItemCreation() {
        assertDoesNotThrow(() -> service.validateBudgetItemCreation(newItem, budget),
            "Failure - valid budget item creation should not throw");
    }

    @Test
    void testValidBudgetItemCreationWithNullItem() {
        ValidationException ex = assertThrows(ValidationException.class,
                            () -> service.validateBudgetItemCreation(null, budget),
                        "Failure - null Item should throw Exception");

        assertEquals("Budget item cannot be null", ex.getMessage(), "Failure - wrong message");
    }

    @Test
    void testValidBudgetItemCreationWithNullBudget() {
        ValidationException ex = assertThrows(ValidationException.class,
            () -> service.validateBudgetItemCreation(newItem, null),
        "Failure - null Budget should throw Exception");

        assertEquals("Budget cannot be null", ex.getMessage(), "Failure - wrong message");
    }

    @Test
    void testDuplicatedThrows() {
        budget.setItems(List.of(newItem));
        repo.save(budget);
        BudgetItem newItem2 = new BudgetItem(1,2025, "Infra", 10000.0, true, List.of(Ministry.HEALTH));

        ValidationException ex = assertThrows(ValidationException.class,
            () -> service.validateBudgetItemCreation(newItem2, budget),
                                "Failure - 2 Items with same ID should throw");
        assertEquals(Message.DUPLICATE_BUDGET_ITEM_ERROR, ex.getMessage(),
                                "Failure - wrong message");
    }

    @Test
    void testValidateUniqueName() {
        budget.setItems(List.of(newItem));
        repo.save(budget);
        BudgetItem newItem2 = new BudgetItem(2, 2025, "Infra", 10000.0, true, List.of(Ministry.HEALTH));

        ValidationException ex = assertThrows(ValidationException.class,
        () -> service.validateBudgetItemCreation(newItem2, budget),
                            "Failure - 2 Items with same name should throw");
        assertEquals(Message.DUPLICATE_BUDGET_ITEM_ERROR, ex.getMessage(),
                            "Failure - wrong message");
    }

    @Test
    void testNegativeAmountOnCreate() {
        BudgetItem newItem2 = new BudgetItem(3, 2025, "Road", -10000.0, true, List.of(Ministry.HEALTH));

        ValidationException ex = assertThrows(ValidationException.class,
        () -> service.validateBudgetItemCreation(newItem2, budget),
                            "Failure - Negative amount should throw");
        assertEquals(Message.NON_NEGATIVE_AMOUNT_ERROR, ex.getMessage(),
                            "Failure - wrong message");
    }

    @Test
    void testWithEmptyMinistries() {
        BudgetItem newItem2 = new BudgetItem(3, 2025, "Road", 10000.0, true,  new ArrayList<>());
        
        ValidationException ex = assertThrows(ValidationException.class,
        () -> service.validateBudgetItemCreation(newItem2, budget),
                            "Failure - empty ministries should throw");
        assertEquals("ministry field can't be empty", ex.getMessage(),
                            "Failure - wrong message");
    }

    @Test
    void testMinistryContainsNull() {
        List<Ministry> listWithNull = new ArrayList<>();
        listWithNull.add(null);

        BudgetItem newItem2 = new BudgetItem(3, 2025, "Road", 10000.0, true, listWithNull);

        ValidationException ex = assertThrows(ValidationException.class,
        () -> service.validateBudgetItemCreation(newItem2, budget),
                        "Failure - null ministry should throw");
        assertEquals("Cannot belong to null ministry", ex.getMessage(),
                        "Failure - wrong message");
    }

    @Test
    void testValidateBalanceChangeLimit() {
        Budget budget2 = new Budget(new ArrayList<>(), 2025);
        budget2.setItems(List.of(newItem));
        budget2.setNetResult(10000.0);
        BudgetItem newItem2 = new BudgetItem(7, 2025, "BigCut", 1000000.0, false,
        List.of(Ministry.HEALTH));

        ValidationException ex = assertThrows(ValidationException.class,
            () -> service.validateBudgetItemCreation(newItem2, budget2),
        "Failure - Exceeding change limit should throw");

        String msg = ex.getMessage();
        assertTrue(msg.contains("exceeds the allowed limit") ||
                   msg.contains("change the budget balance"),
                   "Expected message about exceeding allowed change limit, got: " + msg);
    }

    @Test
    void testValidChange() {
        Budget budget2 = new Budget(List.of(newItem), 2026);
        budget2.setNetResult(10000.0);
        BudgetItem newItem2 = new BudgetItem(7, 2026, "BigCut", 50.0, true,
        List.of(Ministry.HEALTH));

        assertDoesNotThrow(() -> service.validateBudgetItemCreation(newItem2, budget2),
                        "Failure - valid item should not throw");
    }

    // TESTs for validateBudgetItemDeletion()
    
    @Test
    void testDeleteProtectedItem() {
        budget.setItems(List.of(newItem));
        ValidationException ex = assertThrows(ValidationException.class,
            () -> service.validateBudgetItemDeletion(newItem, budget),
                            "Failure - deleting protected item should throw");
        String msg = ex.getMessage();
        assertTrue(msg.contains("protected BudgetItem cannot be deleted"),
                            "Failure - wrong message");
    }

   @Test
   void testDeleteNullItem() {
        ValidationException ex = assertThrows(ValidationException.class,
            () -> service.validateBudgetItemDeletion(null, budget),
                            "Failure - deleting null item should throw");
        String msg = ex.getMessage();
        assertTrue(msg.contains("item to delete cannot be null"),
                            "Failure - wrong message");
   }

   @Test
   void testDeleteNullBudget() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> service.validateBudgetItemDeletion(newItem, null),
                        "Failure - deleting with null Budget should throw");
        String msg = ex.getMessage();
        assertTrue(msg.contains("Budget cannot be null"),
                        "Failure - wrong message");
   }

   @Test
    void testDeleteExistingItemsNull() {
        ValidationException ex = assertThrows(ValidationException.class,
            () -> service.validateBudgetItemDeletion(newItem, budget));
        String msg = ex.getMessage();
        assertTrue(msg.contains("Cannot delete from empty Item list"),
                        "Failure - wrong message");
    }

    @Test
    void testDeleteItemValid() {
        BudgetItem newItem2 = new BudgetItem(20, 2025, "Roads", 1000.0, false,
            List.of(Ministry.AGRICULTURE));
        Budget budget2 = new Budget(List.of(newItem2), 2025);

        assertDoesNotThrow(() -> service.validateBudgetItemDeletion(newItem2, budget2),
                        "Failure - valid deletion should not throw");
    }

    // TESTs for validateBudgetItemUpdate()

    @Test
    void testUpdateValid() {
        BudgetItem original = new BudgetItem(20, 2025, "Roads", 1000.0, false,
            List.of(Ministry.HEALTH));
        BudgetItem updated = new BudgetItem(20, 2025, "Roads", 1100.0, false,
            List.of(Ministry.HEALTH));

        assertDoesNotThrow(() -> service.validateBudgetItemUpdate(original, updated),
                        "Valid small change should not throw");
    }

    @Test
    void testUpdateNoChange() {
        BudgetItem original = new BudgetItem(21, 2025, "Roads", 1000.0, false,
            List.of(Ministry.HEALTH));
        BudgetItem updated = new BudgetItem(21, 2025, "Roads", 1000.0, false,
            List.of(Ministry.HEALTH));

        ValidationException ex = assertThrows(ValidationException.class,
            () -> service.validateBudgetItemUpdate(original, updated),
                        "Failure - no update should throw");
        assertEquals("update doesn't change BudgetItem value", ex.getMessage());
    }

    @Test
    void testUpdateNegativeValue() {
        BudgetItem original = new BudgetItem(22, 2025, "Roads", 1000.0, false,
            List.of(Ministry.HEALTH));
        BudgetItem updated = new BudgetItem(22, 2025, "Roads", -10.0, false,
            List.of(Ministry.HEALTH));

        ValidationException ex = assertThrows(ValidationException.class,
        () -> service.validateBudgetItemUpdate(original, updated),
                                "Failure - negative amount should throw");
        assertEquals(Message.NON_NEGATIVE_AMOUNT_ERROR, ex.getMessage());
    }

    @Test
    void testUpdateZeroOriginalValue() {
        BudgetItem original = new BudgetItem(22, 2025, "Roads", 0.0, false,
            List.of(Ministry.HEALTH));
        BudgetItem updated = new BudgetItem(22, 2025, "Roads", 1000, false,
            List.of(Ministry.HEALTH));

        assertDoesNotThrow(() -> service.validateBudgetItemUpdate(original, updated),
                            "Failure - 0 original value should not throw");
    }

    @Test
    void testUpdateLimitExceeded() {
        BudgetItem original = new BudgetItem(23, 2025, "Roads", 1000.0, false,
            List.of(Ministry.HEALTH));
        BudgetItem updated = new BudgetItem(23, 2025, "Roads", 100000.0, false,
            List.of(Ministry.HEALTH));

        ValidationException ex = assertThrows(ValidationException.class,
            () -> service.validateBudgetItemUpdate(original, updated),
                            "Failure - exceeding limit should throw");
        String msg = ex.getMessage();
        assertTrue(msg.contains("Additional approval required") ||
                   msg.contains("exceeds the allowed limit"),
                   "Expected message about exceeding edit change limit, got: " + msg);
    }

    @Test
    void testUpdateOriginalNull() {
        BudgetItem updated = new BudgetItem(24, 2025, "Roads", 100.0, false,
            List.of(Ministry.HEALTH));
        
        ValidationException ex = assertThrows(ValidationException.class,
            () -> service.validateBudgetItemUpdate(null, updated),
                            "Failure - null item should throw");
        assertEquals("Original BudgetItem can't be null", ex.getMessage());
    }

    @Test
    void testUpdateUpdatedNull() {
        BudgetItem original = new BudgetItem(25, 2025, "Roads", 100.0, false,
            List.of(Ministry.HEALTH));

        ValidationException ex = assertThrows(ValidationException.class,
            () -> service.validateBudgetItemUpdate(original, null),
                                "Failure - null item should throw");
        assertEquals("updated BudgetItem can't be null", ex.getMessage());
    }

    @Test
    void testDataIntegrityValid() {
        assertDoesNotThrow(() -> service.validateDataIntegrity(newItem),
                                "Valid budget item should pass data integrity validation");
    }

    @Test
    void testDataIntegrityNullItem() {
        ValidationException ex = assertThrows(ValidationException.class,
            () -> service.validateDataIntegrity(null),
                                "Failure - null item should throw");
        assertEquals("BudgetItem can't be null", ex.getMessage());
    }

    @Test
    void testDataIntegrityPositiveId() {
        BudgetItem newItem2 = new BudgetItem(0, 2025, "Library", 500.0, false,
            List.of(Ministry.HEALTH));
        
        ValidationException ex = assertThrows(ValidationException.class,
            () -> service.validateDataIntegrity(newItem2),
                                "Failure - non postive ID should throw");
        assertEquals("BudgetItem id must be positive", ex.getMessage());
    }

    @Test
    void testDataIntegrityNotNullName() {
        BudgetItem newItem2 = new BudgetItem(27, 2025, null, 100.0, false,
            List.of(Ministry.HEALTH));

        ValidationException ex = assertThrows(ValidationException.class,
            () -> service.validateDataIntegrity(newItem2),
                                "Failure - null item should throw");
        assertEquals("BudgetItem name can't be null or empty", ex.getMessage());
    }

    @Test
    void testDataIntegrityYearAfter2000() {
        BudgetItem newItem2 = new BudgetItem(34, 1999, "Roads", 120.0, false,
            List.of(Ministry.HEALTH));

        ValidationException ex = assertThrows(ValidationException.class,
            () -> service.validateDataIntegrity(newItem2),
                                "Failure - year before 2000 should throw");
        assertEquals("BudgetItem year can't be lower than 2000", ex.getMessage());
    }

    @Test
    void testDataIntegrityMinistryNull() {
        BudgetItem newItem2 = new BudgetItem(59, 2025, "Roads", 140.0, false, List.of());

        ValidationException ex1 = assertThrows(ValidationException.class,
            () -> service.validateDataIntegrity(newItem2),
                                "Failure - empty ministries should throw");
        assertEquals("BudgetItem ministries can't be null or empty", ex1.getMessage());
    }

    @Test
    void testValidateNonNegativeAmount() {
        BudgetItem newItem2 = new BudgetItem(44, 2025, "Infra", -0.01, false,
            List.of(Ministry.HEALTH));


        ValidationException ex = assertThrows(ValidationException.class,
            () -> service.validateDataIntegrity(newItem2),
                                "Failure - negative amount should throw");
        assertEquals(Message.NON_NEGATIVE_AMOUNT_ERROR, ex.getMessage());
    }
}
