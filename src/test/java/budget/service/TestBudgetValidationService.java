package budget.service;

import budget.constants.Limits;
import budget.constants.Message;
import budget.exceptions.ValidationException;
import budget.model.domain.BudgetItem;
import budget.model.domain.Budget;
import budget.model.enums.Ministry;
import budget.repository.BudgetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.List;
import budget.model.enums.Ministry;

public class TestBudgetValidationService {
    private TestBudgetRepository repo;
    private BudgetValidationService service;

    @BeforeEach
    void setUp() {
        repo = new TestBudgetRepository();
        service = new BudgetValidationService(repo);
    }

    //Test Stub
    private static class TestBudgetRepository extends BudgetRepository {
        private boolean existsById = false;
        private boolean existsByName = false;

        void setExistsById(boolean x) {
            this.existsById = x;
        }
        void setExistsByName(boolean x) {
            this.existsByName = x;
        }

        @Override
        public boolean existsByItemId(int id, int year) {
            return existsById;
        }
        @Override
        public boolean existsByName(String name, int year) {
            return existsByName;
        }
    }
    
    @Test
    void TestValidBudgetItemCreation() {
        // Assuming Constructor: BudgetItem(int id, String name, double value,
        // boolean isRevenue, int Year, List<Ministry> ministries)
        BudgetItem newItem = new BudgetItem(1, 2025, "Infra", 10000.0, true, List.of(Ministry.HEALTH));
        Budget budget = new Budget(new ArrayList<>(), 0); //netResult = 0 : any change allowed

        assertDoesNotThrow(() -> service.validateBudgetItemCreation(newItem, budget),
            "Failure - valid budget item creation should not throw");
    }

    @Test
    void TestDuplicatedThrows() {
        repo.setExistsById(true);
        BudgetItem newItem = new BudgetItem(1,2025, "Infra", 10000.0, true, List.of(Ministry.HEALTH));
        Budget budget = new Budget(new ArrayList<>(), 0);

        ValidationException ex = assertThrows(ValidationException.class,
            () -> service.validateBudgetItemCreation(newItem, budget));
        assertEquals(Message.DUPLICATE_BUDGET_ITEM_ERROR, ex.getMessage());
    }

    @Test
    void TestValidateUniqueName() {
        repo.setExistsById(true);
        BudgetItem newItem = new BudgetItem(2, 2025, "Infra", 10000.0, true, List.of(Ministry.HEALTH));
        Budget budget = new Budget(new ArrayList<>(), 0);

        ValidationException ex = assertThrows(ValidationException.class,
        () -> service.validateBudgetItemCreation(newItem, budget));
        assertEquals(Message.DUPLICATE_BUDGET_ITEM_ERROR, ex.getMessage());
    }

    @Test
    void TestNegativeAmountOnCreate() {
        repo.setExistsByName(false);
        BudgetItem newItem = new BudgetItem(3, 2025, "Road", -10000.0, true, List.of(Ministry.HEALTH));
        Budget budget = new Budget(new ArrayList<>(), 0);

        ValidationException ex = assertThrows(ValidationException.class,
        () -> service.validateBudgetItemCreation(newItem, budget));
        assertEquals(Message.NON_NEGATIVE_AMOUNT_ERROR, ex.getMessage());
    }

    @Test
    void TestMinistryNullOrEmptyOnCreate() {
        BudgetItem newItem = new BudgetItem(3, 2025, "Road", -10000.0, true,  new ArrayList<>());
        Budget budget = new Budget(new ArrayList<>(), 0);
        
        ValidationException ex = assertThrows(ValidationException.class,
        () -> service.validateBudgetItemCreation(newItem, budget));
        assertEquals(Message.NON_NEGATIVE_AMOUNT_ERROR, ex.getMessage());
    }

    @Test
    void TestMinistryContainsNull() {
        List<Ministry> listWithNull = new ArrayList<>();
        listWithNull.add(null);

        BudgetItem newItem = new BudgetItem(3, 2025, "Road", 10000.0, true, listWithNull);
        Budget budget = new Budget(new ArrayList<>(), 0);

        ValidationException ex = assertThrows(ValidationException.class,
        () -> service.validateBudgetItemCreation(newItem, budget));
        assertEquals("Cannot belong to null ministry", ex.getMessage());
    }

    @Test
    void TestValidateBalanceChangeLimit() {
        BudgetItem newItem = new BudgetItem(7, 2025, "BigCut", 1000000.0, false,
        List.of(Ministry.HEALTH));
        Budget budget = new Budget(new ArrayList<>(), 2025);
        budget.setNetResult(1000.0);

        ValidationException ex = assertThrows(ValidationException.class,
            () -> service.validateBudgetItemCreation(newItem, budget));

        String msg = ex.getMessage();
        assertTrue(msg.contains("exceeds the allowed limit") ||
                   msg.contains("change the budget balance"),
                   "Expected message about exceeding allowed change limit, got: " + msg);
    }

    
    @Test
    void TestDeleteProtectedItem() {
        BudgetItem newItem = new BudgetItem(10, 2025, "Health", 5000.0, false,
            List.of(Ministry.HEALTH));
        Budget budget = new Budget(new ArrayList<>(), 2025);

        ValidationException ex = assertThrows(ValidationException.class,
            () -> service.validateBudgetItemDeletion(newItem, budget));
        String msg = ex.getMessage();
        assertTrue(msg.contains("protected BudgetItem cannot be deleted"));
    }

   @Test
   void TestDeteleNullItem() {
        Budget budget = new Budget(new ArrayList<>(), 2025);

        ValidationException ex = assertThrows(ValidationException.class,
            () -> service.validateBudgetItemDeletion(null, budget));
        String msg = ex.getMessage();
        assertTrue(msg.contains("item to delete cannot be null"));
   }

   @Test
   void TestDeleteNullBudget() {
        BudgetItem item = new BudgetItem(11, 2025, "Local", 200.0, false,
            List.of(Ministry.HEALTH));
        Budget budget = null;
 
        ValidationException ex = assertThrows(ValidationException.class,
                () -> service.validateBudgetItemDeletion(item, null));
        String msg = ex.getMessage();
        assertTrue(msg.contains("Budget cannot be null"));
   }

   @Test
    void TestDeleteExistingItemsNull() {
        BudgetItem newItem = new BudgetItem(12, 2025, "Local", 200.0, false,
            List.of(Ministry.HEALTH));

        Budget budget = new Budget(new ArrayList<>(), 2025) {
            @Override
            public List<BudgetItem> getItems() {
                return null;
            }
        };

        ValidationException ex = assertThrows(ValidationException.class,
            () -> service.validateBudgetItemDeletion(newItem, budget));
        String msg = ex.getMessage();
        assertTrue(msg.contains("Cannot delete:Existing items cannot be null"));
        }

    @Test
    void TestUpdateValid() {
        BudgetItem original = new BudgetItem(20, 2025, "Roads", 1000.0, false,
            List.of(Ministry.HEALTH));
        BudgetItem updated = new BudgetItem(20, 2025, "Roads", 1100.0, false,
            List.of(Ministry.HEALTH));

        assertDoesNotThrow(() -> service.validateBudgetItemUpdate(original, updated),
        "Valid small change should not throw");
    }

    @Test
    void TestUpdateNoChange() {
        BudgetItem original = new BudgetItem(21, 2025, "Roads", 1000.0, false,
            List.of(Ministry.HEALTH));
        BudgetItem updated = new BudgetItem(21, 2025, "Roads", 1000.0, false,
            List.of(Ministry.HEALTH));

        ValidationException ex = assertThrows(ValidationException.class,
            () -> service.validateBudgetItemUpdate(original, updated));
        String msg = ex.getMessage();
        assertEquals("update doesn't change BudgetItem value", ex.getMessage());
    }

    @Test
    void TestUpdateNegativeValue() {
        BudgetItem original = new BudgetItem(22, 2025, "Roads", 1000.0, false,
            List.of(Ministry.HEALTH));
        BudgetItem updated = new BudgetItem(22, 2025, "Roads", -10.0, false,
            List.of(Ministry.HEALTH));

        ValidationException ex = assertThrows(ValidationException.class,
        () -> service.validateBudgetItemUpdate(original, updated));
        String msg = ex.getMessage();
        assertEquals(Message.NON_NEGATIVE_AMOUNT_ERROR, ex.getMessage());
    }

    @Test
    void TestUpdateLimitExceeded() {
        BudgetItem original = new BudgetItem(23, 2025, "Roads", 1000.0, false,
            List.of(Ministry.HEALTH));
        BudgetItem updated = new BudgetItem(23, 2025, "Roads", 100000.0, false,
            List.of(Ministry.HEALTH));

        ValidationException ex = assertThrows(ValidationException.class,
            () -> service.validateBudgetItemUpdate(original, updated));
        String msg = ex.getMessage();
        assertTrue(msg.contains("Additional approval required") ||
                   msg.contains("exceeds the allowed limit"),
                   "Expected message about exceeding edit change limit, got: " + msg);
    }

    @Test
    void TestUpdateOriginalNull() {
        BudgetItem updated = new BudgetItem(24, 2025, "Roads", 100.0, false,
            List.of(Ministry.HEALTH));
        
        ValidationException ex = assertThrows(ValidationException.class,
            () -> service.validateBudgetItemUpdate(null, updated));
        String msg = ex.getMessage();
        assertEquals("Original BudgetItem can't be null", ex.getMessage());
    }

    @Test
    void TestUpdateUpdadetNull() {
        BudgetItem original = new BudgetItem(25, 2025, "Roads", 100.0, false,
            List.of(Ministry.HEALTH));

        ValidationException ex = assertThrows(ValidationException.class,
            () -> service.validateBudgetItemUpdate(original, null));
        assertEquals("updated BudgetItem can't be null", ex.getMessage());
    }

    @Test
    void TestDataIntegrityValid() {
        BudgetItem newItem = new BudgetItem(30, 2025, "Library", 500.0, false,
            List.of(Ministry.HEALTH));

        assertDoesNotThrow(() -> service.validateDataIntegrity(newItem),
            "Valid budget item should pass data integrity validation");
    }

    @Test
    void TestDataIntegrityNullItem() {
        ValidationException ex = assertThrows(ValidationException.class,
            () -> service.validateDataIntegrity(null));
        assertEquals("BudgetItem can't be null", ex.getMessage());
    }

    @Test
    void TestDataIntegrityPositiveId() {
        BudgetItem newItem = new BudgetItem(0, 2025, "Library", 500.0, false,
            List.of(Ministry.HEALTH));
        
        ValidationException ex = assertThrows(ValidationException.class,
            () -> service.validateDataIntegrity(newItem));
        assertEquals("BudgetItem id must be positive", ex.getMessage());
    }

    @Test
    void TestDataIntegrityNotNullName() {
        BudgetItem newItem = new BudgetItem(27, 2025, null, 100.0, false,
            List.of(Ministry.HEALTH));

        ValidationException ex = assertThrows(ValidationException.class,
            () -> service.validateDataIntegrity(newItem));
        assertEquals("BudgetItem name can't be null or empty", ex.getMessage());
    }

    @Test
    void TestDataIntegrityYearAfter2000() {
        BudgetItem newItem = new BudgetItem(34, 1999, "Roads", 120.0, false,
            List.of(Ministry.HEALTH));

        ValidationException ex = assertThrows(ValidationException.class,
            () -> service.validateDataIntegrity(newItem));
        assertEquals("BudgetItem year can't be lower than 2000", ex.getMessage());
    }

    @Test
    void TestDataIntegrityMinistryNull() {
        BudgetItem newItem1 = new BudgetItem(59, 2025, "Roads", 140.0, false, List.of());

        ValidationException ex1 = assertThrows(ValidationException.class,
            () -> service.validateDataIntegrity(newItem1));
        assertEquals("BudgetItem ministries can't be null or empty", ex1.getMessage());
    }

    @Test
    void TestValidateNonNegativeAmount() {
        BudgetItem newItem = new BudgetItem(44, 2025, "Infra", -0.01, false,
            List.of(Ministry.HEALTH));


        ValidationException ex = assertThrows(ValidationException.class,
            () -> service.validateDataIntegrity(newItem));
        assertEquals(Message.NON_NEGATIVE_AMOUNT_ERROR, ex.getMessage());
    }
}
